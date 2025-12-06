(() => {
  const { createApp, ref, computed, watch } = window.Vue;
  const { createPinia, defineStore } = window.Pinia;

  const UI_TEXT = {
    conversationsTitle: 'Conversas',
    conversationsTitleTooltip: 'Lista de diálogos anteriores que você pode retomar',
    newButtonLabel: 'Nova',
    newButtonTooltip: 'Iniciar uma nova conversa com um ou mais colegas',
    searchPlaceholder: 'Buscar conversas específicas',
    chatNoActivePlaceholder: 'Selecione uma conversa para visualizar as mensagens ou inicie uma nova conversa',
    messageInputPlaceholder: 'Digite sua mensagem',
    helpTitle: 'Como usar:',
    helpSteps: [
      'Selecione uma conversa na lista para continuar o diálogo',
      "Clique em 'Nova' para começar uma conversa diferente",
      'Use a barra de pesquisa para encontrar conversas específicas'
    ],
    searchNoResults: 'Nenhuma conversa corresponde ao termo digitado',
    errorSend: 'Não foi possível enviar sua mensagem. Tente novamente.',
    errorConnect: 'Conexão com o chat foi interrompida. Tentando reconectar...'
  };

  const ANALYTICS_ENDPOINT = window.ANALYTICS_ENDPOINT || null;
  function track(event, payload) {
    if (!ANALYTICS_ENDPOINT) return;
    try {
      fetch(ANALYTICS_ENDPOINT, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ event, payload, ts: Date.now() })
      });
    } catch {}
  }

  const useChatStore = defineStore('chat', {
    state: () => ({
      connected: false,
      presence: [],
      conversations: [],
      activeConversationId: null,
      messages: {},
      client: null,
      subscriptions: {},
      lastError: null,
      rooms: [],
      activeRoomId: null,
      roomMessages: {},
      roomSubscriptions: {}
    }),
    actions: {
      connect() {
        const sock = new SockJS('/ws');
        const client = Stomp.over(sock);
        client.debug = () => {};
        client.connect({}, () => {
          this.connected = true;
          this.client = client;
          client.subscribe('/topic/presence', msg => {});
          if (this.activeConversationId) this.subscribeConversation(this.activeConversationId);
          client.send('/app/presence/join', {}, 'JOIN');
        }, () => {
          this.lastError = UI_TEXT.errorConnect;
        });
        sock.onclose = () => {
          this.connected = false;
          this.lastError = UI_TEXT.errorConnect;
          setTimeout(() => this.connect(), 2000);
        };
      },
      async loadRooms() {
        try {
          const res = await fetch('/api/chat/rooms');
          const data = await res.json();
          this.rooms = Array.isArray(data) ? data.map(r => ({ id: r.id, name: r.name })) : [];
        } catch {}
      },
      openRoom(id) {
        this.activeRoomId = id;
        this.subscribeRoom(id);
        this.fetchRoomHistory(id);
        track('chat_conversation_opened', { roomId: id });
        try { window.dispatchEvent(new CustomEvent('chat:clear-unread')); } catch {}
      },
      subscribeRoom(id) {
        if (!this.client) return;
        if (this.roomSubscriptions[id]) return;
        this.roomSubscriptions[id] = this.client.subscribe('/topic/chat.room.' + id, msg => {
          const data = JSON.parse(msg.body);
          if (!this.roomMessages[id]) this.roomMessages[id] = [];
          this.roomMessages[id].unshift(data);
          try { window.dispatchEvent(new CustomEvent('chat:new-message', { detail: { roomId: id } })); } catch {}
        });
      },
      async fetchRoomHistory(id) {
        try {
          const res = await fetch(`/api/chat/rooms/${id}/messages`);
          const data = await res.json();
          this.roomMessages[id] = Array.isArray(data) ? data : [];
        } catch {}
      },
      async createDirectRoom(userId) {
        try {
          const res = await fetch(`/api/chat/rooms/direct?userId=${encodeURIComponent(userId)}`, { method: 'POST' });
          const data = await res.json();
          if (data && data.id) {
            const room = { id: data.id, name: data.name };
            this.rooms.unshift(room);
            this.openRoom(room.id);
            track('chat_conversation_created', { roomId: room.id });
          }
        } catch {}
      },
      async sendRoomMessage(text) {
        if (!this.activeRoomId) return;
        try {
          await fetch(`/api/chat/rooms/${this.activeRoomId}/messages`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: new URLSearchParams({ content: text }) });
          track('chat_room_message_sent', { roomId: this.activeRoomId, length: text.length });
        } catch {
          this.lastError = UI_TEXT.errorSend;
        }
      },
      subscribeConversation(id) {
        if (!this.client) return;
        if (this.subscriptions[id]) return;
        this.subscriptions[id] = this.client.subscribe('/topic/chat/' + id, msg => {
          const data = JSON.parse(msg.body);
          if (!this.messages[id]) this.messages[id] = [];
          this.messages[id].unshift(data);
          try { window.dispatchEvent(new CustomEvent('chat:new-message', { detail: { conversationId: id } })); } catch {}
        });
      },
      sendMessage(text) {
        if (!this.activeConversationId || !this.client) return;
        try {
          const payload = { conversationId: this.activeConversationId, senderId: 0, ciphertext: new TextEncoder().encode(text), iv: new Uint8Array(12), aad: null };
          this.client.send('/app/chat/send', {}, JSON.stringify(payload));
          track('chat_message_sent', { conversationId: this.activeConversationId, length: text.length });
        } catch {
          this.lastError = UI_TEXT.errorSend;
        }
      }
    }
  });

  const ChatWindow = {
    template: `
      <div class="card h-100">
        <div class="card-header">
          <strong v-if="activeRoom">{{ activeRoom.name || ('Sala #' + activeRoom.id) }}</strong>
          <span v-else class="text-muted">{{ ui.chatNoActivePlaceholder }}</span>
        </div>
        <div class="card-body p-2">
          <div class="chat-messages" v-if="activeRoomId || activeConversationId">
            <div v-if="activeRoomId">
              <div v-for="m in roomMsgs" :key="m.id" class="message" :class="bubbleAlign(m)">
                <div class="chat-bubble" :class="bubbleType(m)">
                  <div class="sender" v-if="!isMine(m)">{{ m.senderName }}</div>
                  <div>{{ m.content }}</div>
                </div>
              </div>
            </div>
            <div v-else>
              <div v-for="m in msgs" :key="m.id" class="message incoming">
                <div class="chat-bubble incoming">
                  <div>{{ decode(m.ciphertext) }}</div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="text-muted">{{ ui.chatNoActivePlaceholder }}</div>
        </div>
        <div class="card-footer">
          <div class="d-flex gap-2">
            <input v-model="text" class="form-control" :placeholder="ui.messageInputPlaceholder" @keydown.enter="send" aria-label="Mensagem" />
            <button class="btn btn-primary" @click="send" aria-label="Enviar"><i class="fas fa-paper-plane"></i></button>
          </div>
        </div>
      </div>
    `,
    setup() {
      const store = useChatStore();
      const text = ref('');
      const activeConversationId = computed(() => store.activeConversationId);
      const activeRoomId = computed(() => store.activeRoomId);
      const msgs = computed(() => store.messages[store.activeConversationId] || []);
      const roomMsgs = computed(() => store.roomMessages[store.activeRoomId] || []);
      const activeRoom = computed(() => store.rooms.find(r => r.id === store.activeRoomId) || null);
      const meId = window.CHAT_USER_ID || null;
      const decode = bytes => { try { return new TextDecoder().decode(bytes); } catch { return '[mensagem]'; } };
      const send = () => {
        const content = text.value.trim();
        if (!content.length) return;
        if (activeRoomId.value) { store.sendRoomMessage(content); text.value=''; return; }
        if (activeConversationId.value) { store.sendMessage(content); text.value=''; return; }
      };
      const errorText = computed(() => store.lastError);
      const ui = UI_TEXT;
      const isMine = m => meId && m && m.senderId && Number(m.senderId) === Number(meId);
      const bubbleAlign = m => isMine(m) ? 'outgoing' : 'incoming';
      const bubbleType = m => isMine(m) ? 'outgoing' : 'incoming';
      return { text, send, msgs, roomMsgs, activeConversationId, activeRoomId, decode, errorText, ui, activeRoom, bubbleAlign, bubbleType, isMine };
    }
  };

  const ConversationList = {
    template: `
      <div class="card mb-3"><div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <h6 class="mb-0" :title="ui.conversationsTitleTooltip"><i class="fas fa-list"></i> {{ ui.conversationsTitle }}</h6>
          <button class="btn btn-sm btn-outline-secondary" @click="openDirect" :title="ui.newButtonTooltip" :aria-label="ui.newButtonLabel"><i class="fas fa-plus"></i> {{ ui.newButtonLabel }}</button>
        </div>
        <div class="mb-2">
          <input v-model="q" class="form-control" :placeholder="ui.searchPlaceholder" aria-label="Buscar" />
        </div>
        <ul class="list-group" role="list">
          <li v-for="c in roomsFiltered" :key="c.id" class="list-group-item d-flex justify-content-between align-items-center" @click="openRoom(c.id)" style="cursor:pointer" role="listitem" tabindex="0" @keydown.enter="openRoom(c.id)">
            <span>{{ c.name || ('Sala #' + c.id) }}</span>
            <i class="fas fa-chevron-right"></i>
          </li>
        </ul>
        <div v-if="roomsFiltered.length === 0" class="text-muted">{{ ui.searchNoResults }}</div>
      </div></div>
    `,
    setup() {
      const store = useChatStore();
      const rooms = computed(() => store.rooms);
      const q = ref('');
      const roomsFiltered = computed(() => {
        const normalize = s => (s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
        const term = normalize(q.value);
        if (!term) return rooms.value;
        return rooms.value.filter(c => normalize(c.name || ('Sala #' + c.id)).includes(term));
      });
      const openRoom = id => { store.openRoom(id); };
      const openDirect = () => { try { window.dispatchEvent(new CustomEvent('chat:open-direct')); } catch {} };
      const ui = UI_TEXT;
      return { roomsFiltered, openRoom, openDirect, q, ui };
    }
  };

  const DirectModal = {
    emits: ['close','start'],
    template: `
      <div class="direct-modal" style="position:fixed;inset:0;z-index:1040;" v-if="visible">
        <div class="direct-overlay" style="position:absolute;inset:0;background:rgba(0,0,0,0.4)" @click="close"></div>
        <div class="direct-dialog" role="dialog" aria-modal="true" style="position:relative;max-width:640px;margin:10vh auto;background:#fff;border-radius:8px;box-shadow:0 8px 24px rgba(0,0,0,0.2)">
          <div class="card mb-0"><div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h6 class="mb-0"><i class="fas fa-user-plus"></i> Nova conversa direta</h6>
              <button class="btn btn-sm btn-outline-secondary" @click="close" aria-label="Fechar">Fechar</button>
            </div>
            <input v-model="qUser" class="form-control mb-2" placeholder="Buscar usuários" aria-label="Buscar usuários" />
            <div v-if="loading" class="text-muted"><i class="fas fa-spinner"></i> Carregando...</div>
            <div v-else>
              <div class="list-group">
                <button v-for="u in usersShown" :key="u.id" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" @click="start(u.id)">
                  <span>{{ u.nome }} <small class="text-muted">{{ u.departamento || '' }}</small></span>
                  <span :class="u.online ? 'badge bg-success' : 'badge bg-secondary'">{{ u.online ? 'Online' : 'Offline' }}</span>
                </button>
              </div>
              <div class="mt-2 d-flex justify-content-between">
                <span class="text-muted">{{ usersShown.length }} de {{ users.length }} resultados</span>
                <button v-if="usersShown.length < users.length" class="btn btn-sm btn-outline-primary" @click="loadMore">Carregar mais</button>
              </div>
            </div>
          </div></div>
        </div>
      </div>
    `,
    setup(props, { emit }) {
      const visible = ref(true);
      const qUser = ref('');
      const users = ref([]);
      const loading = ref(false);
      const pageSize = ref(10);
      const showCount = ref(10);
      const usersShown = computed(() => users.value.slice(0, showCount.value));
      let debounceId = null;
      const fetchUsers = async () => {
        loading.value = true;
        try { const res = await fetch(`/api/usuarios/busca?q=${encodeURIComponent(qUser.value)}`); users.value = await res.json(); showCount.value = pageSize.value; } catch { users.value = []; }
        loading.value = false;
      };
      watch(qUser, () => { clearTimeout(debounceId); debounceId = setTimeout(fetchUsers, 300); });
      const loadMore = () => { showCount.value = Math.min(showCount.value + pageSize.value, users.value.length); };
      const close = () => { visible.value = false; emit('close'); };
      const onKey = e => { if (e.key === 'Escape') close(); };
      window.addEventListener('keydown', onKey);
      fetchUsers();
      const start = id => { emit('start', id); };
      return { visible, qUser, users, usersShown, loading, loadMore, close, start };
    }
  };

  const App = {
    components: { ConversationList, ChatWindow, DirectModal },
    template: `
      <div>
        <div class="d-flex justify-content-end mb-2">
          <button class="btn btn-sm btn-outline-info" @click="toggleHelp" :aria-expanded="showHelp.toString()" aria-label="Ajuda"><i class="fas fa-question"></i></button>
          <button class="btn btn-sm btn-primary ms-2" @click="openDirect" aria-label="Nova"><i class="fas fa-plus"></i> Nova</button>
        </div>
        <div v-if="showHelp" class="card mb-3" aria-live="polite"><div class="card-body">
          <strong>{{ ui.helpTitle }}</strong>
          <ol class="mb-0">
            <li v-for="s in ui.helpSteps">{{ s }}</li>
          </ol>
        </div></div>
        <div class="row g-3">
          <div class="col-lg-4"><ConversationList /></div>
          <div class="col-lg-8"><ChatWindow /></div>
        </div>
        <DirectModal v-if="showDirect" @close="closeDirect" @start="startDirect" />
      </div>
    `,
    setup() {
      const store = useChatStore();
      store.connect();
      store.loadRooms();
      const showHelp = ref(false);
      const toggleHelp = () => { const opening = !showHelp.value; showHelp.value = opening; if (opening) track('chat_help_opened', {}); };
      const onKey = e => { if (e.key === 'Escape' && showHelp.value) { showHelp.value = false; } };
      window.addEventListener('keydown', onKey);
      const ui = UI_TEXT;
      const showDirect = ref(false);
      const openDirectListener = () => { showDirect.value = true; };
      window.addEventListener('chat:open-direct', openDirectListener);
      const closeDirect = () => { showDirect.value = false; };
      const startDirect = id => { store.createDirectRoom(id); showDirect.value = false; };
      const openDirect = () => { try { window.dispatchEvent(new CustomEvent('chat:open-direct')); } catch {} };
      return { showHelp, toggleHelp, ui, showDirect, closeDirect, startDirect, openDirect };
    }
  };

  window.addEventListener('load', () => {
    const pinia = createPinia();
    createApp(App).use(pinia).mount('#app');
  });
})();
