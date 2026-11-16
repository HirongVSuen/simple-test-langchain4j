const RENDER_INTERVAL = 80;

export async function streamChat({
  url, body, signal,
  onFirstToken, onToken, onDone, onAbort, onError
}) {
  let fullText = '';
  let timerId = null;
  let lastRenderAt = 0;
  let firstToken = false;
  let finalized = false;
  let done = false;

  const flush = () => { timerId = null; onToken(fullText); };
  const schedule = () => {
    if (timerId != null) return;
    const delay = Math.max(0, RENDER_INTERVAL - (performance.now() - lastRenderAt));
    timerId = setTimeout(() => { lastRenderAt = performance.now(); flush(); }, delay);
  };
  const finalize = () => {
    if (finalized) return;
    finalized = true;
    if (timerId != null) clearTimeout(timerId);
    onDone(fullText);
  };

  const handleEvent = (evt) => {
    const dataLines = [];
    for (const line of evt.split(/\r?\n/)) {
      if (line.startsWith('data:')) {
        let d = line.slice(5);
        if (d.startsWith(' ') && d.length > 1) d = d.slice(1);
        dataLines.push(d);
      }
    }
    if (!dataLines.length) return;

    const raw = dataLines.join('\n');
    if (raw === '[DONE]') {
      finalize();
      done = true;
      return;
    }

    fullText += raw;
    if (!firstToken) {
      firstToken = true;
      onFirstToken(fullText);
    } else {
      schedule();
    }
  };

  try {
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      signal
    });
    if (!resp.ok) throw new Error('HTTP ' + resp.status);
    if (!resp.body) throw new Error('响应没有可读取的 body');

    const reader = resp.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { done: streamDone, value } = await reader.read();
      if (streamDone) break;

      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split(/\r?\n\r?\n/);
      buffer = events.pop() ?? '';

      for (const evt of events) {
        handleEvent(evt);
        if (done) return;
      }
    }

    // flush 解码器里残留的多字节字符
    buffer += decoder.decode();
    if (buffer.trim()) handleEvent(buffer);
    if (!done) finalize();
  } catch (err) {
    if (err.name === 'AbortError') {
      onAbort?.();
      return;
    }
    if (!finalized) {
      finalized = true;
      if (timerId != null) clearTimeout(timerId);
      onError(err);
    }
  }
}
