class ChatSession extends HTMLElement {
  connectedCallback() {
    this.activeAbort = null;
  }

  registerAbort(controller) {
    this.activeAbort = controller;
  }

  disconnectedCallback() {
    this.activeAbort?.abort();
    this.activeAbort = null;
  }
}

customElements.define('chat-session', ChatSession);
