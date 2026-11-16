import { scrollToBottom } from '../utils/scroll.js';
import { renderMarkdown } from '../lib/markdown.js';
import { enhance } from '../lib/highlight.js';
import { streamChat } from '../lib/stream.js';
import { autoGrow, isSubmitKey } from './composer.js';

export function chat() {
  return {
    messages: [],
    input: '',
    memoryId: 'default',
    busy: false,
    host: null,

    init() {
      this.host = this.$el.closest('chat-session');
      this.memoryId = this.host?.dataset.memoryId || 'default';
      this.$nextTick(() => {
        if (this.$el.offsetParent !== null) this.$refs.input?.focus();
      });
    },

    onEnter(e) {
      if (!isSubmitKey(e)) return;
      e.preventDefault();
      this.send();
    },
    autoGrow() { autoGrow(this.$refs.input); },

    async send() {
      const message = this.input.trim();
      if (!message || this.busy) return;

      this.input = '';
      this.$nextTick(() => this.autoGrow());
      this.busy = true;

      this.messages.push({
        role: 'user', text: message,
        html: renderMarkdown(message), streaming: false, error: false
      });

      const aiIndex = this.messages.length;
      this.messages.push({
        role: 'assistant', text: '', html: '', streaming: true, error: false
      });
      this.$nextTick(() => scrollToBottom(this.$refs.messages, true));

      const controller = new AbortController();
      this.host?.registerAbort(controller);

      await streamChat({
        url: '/api/chat/stream',
        body: { memoryId: this.memoryId, message },
        signal: controller.signal,
        onFirstToken: (full) => {
          this.messages[aiIndex].text = full;
          scrollToBottom(this.$refs.messages);
        },
        onToken: (full) => {
          this.messages[aiIndex].text = full;
          scrollToBottom(this.$refs.messages);
        },
        onDone: (full) => {
          const m = this.messages[aiIndex];
          m.streaming = false;
          m.text = full || '(空响应)';
          m.html = renderMarkdown(m.text);
          this.$nextTick(() => enhance(this.$refs.messages));
        },
        onAbort: () => { /* 标签页已关闭，静默丢弃 */ },
        onError: (err) => {
          const m = this.messages[aiIndex];
          m.streaming = false;
          m.error = true;
          m.text = '⚠️ ' + err.message;
          m.html = renderMarkdown(m.text);
        }
      });

      this.host?.registerAbort(null);
      this.busy = false;
      this.$nextTick(() => {
        if (this.$el.offsetParent !== null) this.$refs.input?.focus();
      });
    }
  };
}
