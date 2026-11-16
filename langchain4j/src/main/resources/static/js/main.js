import { chat } from './components/chat.js';
import { sessionTabs } from './components/session-tabs.js';
import './components/chat-session.js';
import { enhance } from './lib/highlight.js';

document.addEventListener('alpine:init', () => {
  Alpine.data('chat', chat);
  Alpine.data('sessionTabs', sessionTabs);
});

/**
 * Alpine 的 x-for 会把 <template> 内容克隆进 DOM，htmx 不会自动感知这些新节点。
 * 这里在 Alpine 初始化完成后主动 process 一次，让 hx-trigger="load" 生效。
 */
function processHtmx(root = document.body) {
  window.htmx?.process(root);
}

document.addEventListener('alpine:initialized', () => {
  // 等 Alpine 的 x-for effect 完成后再交给 htmx
  setTimeout(() => processHtmx(document.body), 0);
});

document.body.addEventListener('htmx:afterSwap', (e) => {
  if (window.Alpine) Alpine.initTree(e.detail.target);
  enhance(e.detail.target);
});

document.body.addEventListener('htmx:responseError', (e) => {
  console.error('htmx error', e.detail);
});
