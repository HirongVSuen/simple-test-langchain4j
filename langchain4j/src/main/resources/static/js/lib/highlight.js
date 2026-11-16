export function enhance(container) {
  if (!container) return;

  container.querySelectorAll('pre code').forEach(code => {
    if (code.dataset.highlighted) return;
    try { window.hljs?.highlightElement(code); } catch { /* ignore */ }
    code.dataset.highlighted = '1';
  });

  container.querySelectorAll('pre').forEach(pre => {
    if (pre.querySelector('.copy-btn')) return;
    const btn = document.createElement('button');
    btn.className = 'copy-btn btn btn-xs btn-ghost absolute top-2 right-2';
    btn.textContent = '复制';
    btn.addEventListener('click', async () => {
      try {
        await navigator.clipboard.writeText(pre.querySelector('code')?.innerText ?? pre.innerText);
        btn.textContent = '已复制';
      } catch {
        btn.textContent = '失败';
      }
      setTimeout(() => (btn.textContent = '复制'), 1200);
    });
    pre.appendChild(btn);
  });
}
