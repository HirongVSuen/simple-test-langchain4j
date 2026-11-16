export function scrollToBottom(el, force = false, threshold = 120) {
  if (!el) return;
  if (force || el.scrollHeight - el.scrollTop - el.clientHeight < threshold) {
    el.scrollTop = el.scrollHeight;
  }
}
