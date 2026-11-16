function createSessionId() {
  // crypto.randomUUID 只在安全上下文（HTTPS / localhost）可用；
  // 通过局域网 IP 访问时必须走降级逻辑，否则新会话按钮会静默失效。
  if (globalThis.crypto?.randomUUID) {
    return 's-' + globalThis.crypto.randomUUID();
  }
  return 's-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
}

export function sessionTabs() {
  const firstId = 's-default';
  return {
    sessions: [{ id: firstId, title: '新对话' }],
    activeId: firstId,
    counter: 1,

    addSession() {
      const id = createSessionId();
      this.counter++;
      this.sessions.push({ id, title: '新对话 ' + this.counter });
      this.activeId = id;

      // 新会话 DOM 由 x-for 克隆出来，需要让 htmx 重新扫描，
      // 否则内部的 hx-trigger="load" 不会发请求。
      this.$nextTick(() => setTimeout(() => window.htmx?.process(this.$root), 0));
    },

    closeSession(id) {
      if (this.sessions.length === 1) return;
      const idx = this.sessions.findIndex(s => s.id === id);
      if (idx === -1) return;
      this.sessions.splice(idx, 1);
      if (this.activeId === id) {
        this.activeId = this.sessions[Math.max(0, idx - 1)].id;
      }
    },

    isActive(id) { return this.activeId === id; }
  };
}
