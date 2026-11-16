export function autoGrow(el, maxHeight = 200) {
  if (!el) return;
  el.style.height = 'auto';
  el.style.height = Math.min(el.scrollHeight, maxHeight) + 'px';
}

export function isSubmitKey(e) {
  return e.key === 'Enter' && !e.shiftKey && !e.isComposing;
}
