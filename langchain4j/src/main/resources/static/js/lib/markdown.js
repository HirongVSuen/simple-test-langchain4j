if (window.marked?.setOptions) {
  window.marked.setOptions({ gfm: true, breaks: true });
}

export function fixMarkdown(text) {
  if (!text) return text;
  return text
    .replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
    .replace(/^(\s*[-*+])([^\s])/gm, '$1 $2')
    .replace(/^(\s*\d+\.)([^\s])/gm, '$1 $2');
}

export function renderMarkdown(text) {
  const raw = window.marked.parse(fixMarkdown(text || ''));
  return window.DOMPurify.sanitize(raw);
}
