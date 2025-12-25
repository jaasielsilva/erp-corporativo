(() => {
  const { createApp, ref, computed, watch, nextTick } = window.Vue;
  const { createPinia, defineStore } = window.Pinia;

  const api = async (url, opts = {}) => {
    const res = await fetch(url, { credentials: 'same-origin', ...opts });
    const contentType = res.headers.get('content-type') || '';
    if (res.ok) {
      if (contentType.includes('application/json')) return res.json();
      return null;
    }

    let message = 'HTTP ' + res.status;
    try {
      if (contentType.includes('application/json')) {
        const data = await res.json();
        message =
          (data && (data.message || data.error || data.erro)) ||
          (data ? JSON.stringify(data) : message);
      } else {
        const text = await res.text().catch(() => '');
        if (text) message = text;
      }
    } catch { }
    throw new Error(message);
  };

  const useWhaTchatStore = defineStore('whatchat', () => {
    const connected = ref(false);
    const client = ref(null);
    const rooms = ref([]);
    const activeId = ref(null);
    const messages = ref([]);
    const q = ref('');
    const loadingRooms = ref(false);
    const loadingMessages = ref(false);
    const lastError = ref(null);
    let subRooms = null;
    let subActive = null;

    const filteredRooms = computed(() => {
      const normalize = s => (s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
      const term = normalize(q.value);
      if (!term) return rooms.value;
      return rooms.value.filter(r => {
        const name = r.nomeContato || '';
        const wa = r.waId || '';
        const status = r.status || '';
        return normalize(name).includes(term) || normalize(wa).includes(term) || normalize(status).includes(term);
      });
    });

    const activeRoom = computed(() => rooms.value.find(r => r.id === activeId.value) || null);

    const connect = () => {
      const sock = new SockJS('/ws');
      const c = Stomp.over(sock);
      c.debug = () => { };
      c.connect({}, () => {
        connected.value = true;
        client.value = c;
        if (subRooms) subRooms.unsubscribe();
        subRooms = c.subscribe('/topic/whatchat/conversas', () => {
          loadRooms();
          if (activeId.value) loadMessages(activeId.value);
        });
        if (activeId.value) subscribeActive(activeId.value);
      }, () => {
        lastError.value = 'Conexão com o WhaTchat foi interrompida. Tentando reconectar...';
      });
      sock.onclose = () => {
        connected.value = false;
        lastError.value = 'Conexão com o WhaTchat foi interrompida. Tentando reconectar...';
        setTimeout(() => connect(), 2000);
      };
    };

    const subscribeActive = (id) => {
      if (!client.value) return;
      if (subActive) subActive.unsubscribe();
      subActive = client.value.subscribe('/topic/whatchat/conversas/' + id, () => loadMessages(id));
    };

    const loadRooms = async () => {
      loadingRooms.value = true;
      try {
        rooms.value = await api('/api/whatchat/conversas');
        lastError.value = null;
      } catch (e) {
        rooms.value = [];
        lastError.value = e && e.message ? e.message : 'Falha ao carregar conversas';
      } finally {
        loadingRooms.value = false;
      }
    };

    const openRoom = async (id) => {
      activeId.value = id;
      await loadMessages(id);
      subscribeActive(id);
      await nextTick();
      scrollToBottom();
    };

    const loadMessages = async (id) => {
      loadingMessages.value = true;
      try {
        messages.value = await api('/api/whatchat/conversas/' + id + '/mensagens');
        lastError.value = null;
        await nextTick();
        scrollToBottom();
      } catch (e) {
        messages.value = [];
        lastError.value = e && e.message ? e.message : 'Falha ao carregar mensagens';
      } finally {
        loadingMessages.value = false;
      }
    };

    const sendText = async (texto) => {
      const id = activeId.value;
      if (!id) return;
      const body = JSON.stringify({ texto: texto || '' });
      await api('/api/whatchat/conversas/' + id + '/mensagens/texto', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body
      });
      await loadMessages(id);
      await loadRooms();
    };

    const uploadFile = async (file, tipo) => {
      const id = activeId.value;
      if (!id) return;
      const form = new FormData();
      form.append('arquivo', file);
      form.append('tipo', tipo);
      await api('/api/whatchat/conversas/' + id + '/mensagens/arquivo', { method: 'POST', body: form });
      await loadMessages(id);
      await loadRooms();
    };

    const updateStatus = async (status) => {
      const id = activeId.value;
      if (!id) return;
      await api('/api/whatchat/conversas/' + id + '/status', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      await loadRooms();
    };

    const createProcesso = async () => {
      const id = activeId.value;
      if (!id) return;
      await api('/api/whatchat/conversas/' + id + '/criar-processo-previdenciario', { method: 'POST' });
      await loadRooms();
    };

    const scrollToBottom = () => {
      try {
        const el = document.querySelector('.chat-messages');
        if (!el) return;
        el.scrollTop = el.scrollHeight;
      } catch { }
    };

    watch(activeId, (id) => {
      if (!id) return;
      setTimeout(() => scrollToBottom(), 50);
    });

    return {
      connected,
      rooms,
      activeId,
      activeRoom,
      messages,
      q,
      filteredRooms,
      loadingRooms,
      loadingMessages,
      lastError,
      connect,
      loadRooms,
      openRoom,
      sendText,
      uploadFile,
      updateStatus,
      createProcesso
    };
  });

  const RoomList = {
    template: `
      <div class="whatchat-list">
        <div class="whatchat-header">
          <div class="whatchat-title">WhaTchat</div>
          <button class="btn btn-sm btn-outline-secondary" @click="reload" :disabled="loadingRooms" aria-label="Recarregar">
            <i class="fas fa-rotate"></i>
          </button>
        </div>
        <div class="whatchat-search">
          <input class="form-control form-control-sm" v-model="q" placeholder="Buscar por contato, número ou status" />
        </div>
        <div class="whatchat-items">
          <div v-if="loadingRooms" class="p-3 text-muted">Carregando...</div>
          <div v-else-if="rooms.length === 0" class="p-3 text-muted">Nenhuma conversa.</div>
          <div v-else>
            <div v-for="r in rooms" :key="r.id" class="whatchat-item" :class="{active: r.id === activeId}"
                 @click="open(r.id)">
              <div class="avatar">{{ avatarText(r) }}</div>
              <div class="item-main">
                <div class="item-top">
                  <div class="item-name">{{ r.nomeContato || ('Contato ' + (r.waId || '')) }}</div>
                  <div class="item-time" v-if="r.ultimaMensagemEm">{{ formatListTime(r.ultimaMensagemEm) }}</div>
                </div>
                <div class="item-meta">{{ r.waId }}</div>
                <div class="item-meta" v-if="r.clienteNome">Cliente: {{ r.clienteNome }}</div>
                <div class="item-meta" v-if="r.processoPrevidenciarioId">Processo: #{{ r.processoPrevidenciarioId }}</div>
              </div>
              <div class="item-badge">
                <span class="badge text-bg-light">{{ r.status }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    `,
    setup() {
      const store = useWhaTchatStore();
      const rooms = computed(() => store.filteredRooms);
      const q = computed({
        get: () => store.q,
        set: v => { store.q = v; }
      });
      const activeId = computed(() => store.activeId);
      const loadingRooms = computed(() => store.loadingRooms);
      const open = id => store.openRoom(id);
      const reload = () => store.loadRooms();
      const avatarText = (r) => {
        const name = (r && r.nomeContato) ? String(r.nomeContato).trim() : '';
        if (name) return name.substring(0, 1).toUpperCase();
        const wa = (r && r.waId) ? String(r.waId).trim() : '';
        return wa ? wa.substring(0, 1) : '?';
      };
      const formatListTime = (iso) => {
        try {
          const d = new Date(iso);
          const now = new Date();
          const sameDay = d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate();
          if (sameDay) return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
          return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
        } catch {
          return iso;
        }
      };
      return { rooms, q, activeId, open, reload, loadingRooms, avatarText, formatListTime };
    }
  };

  const ChatWindow = {
    template: `
      <div class="whatchat-chat">
        <div v-if="!activeRoom" class="chat-empty">
          Selecione uma conversa para visualizar as mensagens.
        </div>
        <template v-else>
          <div class="chat-top">
            <div class="avatar">{{ avatarText(activeRoom) }}</div>
            <div class="title">
              <div class="name">{{ activeRoom.nomeContato || activeRoom.waId }}</div>
              <div class="meta">{{ activeRoom.waId }}</div>
            </div>
            <div class="ms-auto d-flex gap-2 align-items-center flex-wrap">
              <select class="form-select form-select-sm" style="width: 190px;" v-model="status" @change="onStatus">
                <option value="ABERTA">ABERTA</option>
                <option value="EM_ATENDIMENTO">EM_ATENDIMENTO</option>
                <option value="ENCERRADA">ENCERRADA</option>
              </select>
              <button class="btn btn-sm btn-outline-primary" @click="createProcesso" :disabled="creating || !!activeRoom.processoPrevidenciarioId">
                Criar processo
              </button>
            </div>
          </div>
          <div class="chat-messages">
            <div v-if="loadingMessages" class="text-muted">Carregando...</div>
            <div v-else-if="messages.length === 0" class="text-muted">Sem mensagens.</div>
            <div v-else>
              <div v-for="m in messages" :key="m.id" class="message" :class="m.direcao === 'ENVIADA' ? 'outgoing' : 'incoming'">
                <div class="bubble">
                  <div v-if="m.texto">{{ m.texto }}</div>
                  <div v-if="m.mediaDownloadUrl">
                    <a :href="m.mediaDownloadUrl" target="_blank" rel="noopener">
                      {{ m.mediaFileName || 'arquivo' }}
                    </a>
                  </div>
                  <div class="sub">
                    <span v-if="m.enviadaPorNome">{{ m.enviadaPorNome }}</span>
                    <span v-if="m.dataMensagem">{{ formatDate(m.dataMensagem) }}</span>
                    <span v-if="m.tipo">{{ m.tipo }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="chat-composer">
            <input ref="fileInput" type="file" class="d-none" @change="onFile" />
            <button class="btn btn-light btn-icon" @click="pickFile" :disabled="sending" aria-label="Anexar">
              <i class="fa-solid fa-paperclip"></i>
            </button>
            <input class="form-control composer-input" v-model="text" placeholder="Digite sua mensagem" @keydown.enter.exact.prevent="submit" />
            <button class="btn btn-success btn-icon" @click="submit" :disabled="sending || !text.trim()" aria-label="Enviar">
              <i class="fa-solid fa-paper-plane"></i>
            </button>
          </div>
        </template>
      </div>
    `,
    setup() {
      const store = useWhaTchatStore();
      const activeRoom = computed(() => store.activeRoom);
      const messages = computed(() => store.messages);
      const loadingMessages = computed(() => store.loadingMessages);
      const text = ref('');
      const fileInput = ref(null);
      const sending = ref(false);
      const creating = ref(false);
      const status = ref('ABERTA');

      watch(activeRoom, (r) => {
        status.value = r && r.status ? r.status : 'ABERTA';
      }, { immediate: true });

      const avatarText = (r) => {
        const name = (r && r.nomeContato) ? String(r.nomeContato).trim() : '';
        if (name) return name.substring(0, 1).toUpperCase();
        const wa = (r && r.waId) ? String(r.waId).trim() : '';
        return wa ? wa.substring(0, 1) : '?';
      };

      const submit = async () => {
        if (!text.value.trim()) return;
        sending.value = true;
        try {
          await store.sendText(text.value.trim());
          text.value = '';
        } catch (e) {
          store.lastError = e && e.message ? e.message : 'Falha ao enviar mensagem';
        } finally {
          sending.value = false;
        }
      };

      const pickFile = () => {
        try {
          if (fileInput.value) fileInput.value.click();
        } catch { }
      };

      const onFile = async (ev) => {
        const file = ev && ev.target && ev.target.files ? ev.target.files[0] : null;
        if (!file) return;
        const isImage = (file.type || '').toLowerCase().startsWith('image/');
        const tipo = isImage ? 'IMAGEM' : 'DOCUMENTO';
        sending.value = true;
        try {
          await store.uploadFile(file, tipo);
        } catch (e) {
          store.lastError = e && e.message ? e.message : 'Falha ao enviar arquivo';
        } finally {
          try { ev.target.value = ''; } catch { }
          sending.value = false;
        }
      };

      const onStatus = async () => {
        try {
          await store.updateStatus(status.value);
        } catch (e) {
          store.lastError = e && e.message ? e.message : 'Falha ao atualizar status';
        }
      };

      const createProcesso = async () => {
        creating.value = true;
        try {
          await store.createProcesso();
        } catch (e) {
          store.lastError = e && e.message ? e.message : 'Falha ao criar processo';
        } finally {
          creating.value = false;
        }
      };

      const formatDate = (iso) => {
        try {
          const d = new Date(iso);
          return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
        } catch {
          return iso;
        }
      };

      return { activeRoom, messages, loadingMessages, text, submit, sending, onFile, formatDate, status, onStatus, createProcesso, creating, fileInput, pickFile, avatarText };
    }
  };

  const App = {
    components: { RoomList, ChatWindow },
    template: `
      <div class="whatchat-shell">
        <RoomList />
        <ChatWindow />
        <div v-if="lastError" class="position-fixed bottom-0 end-0 p-3" style="z-index: 1080; max-width: 420px;">
          <div class="alert alert-warning mb-0" role="alert">
            {{ lastError }}
          </div>
        </div>
      </div>
    `,
    setup() {
      const store = useWhaTchatStore();
      store.connect();
      store.loadRooms();
      const lastError = computed(() => store.lastError);
      return { lastError };
    }
  };

  window.addEventListener('load', () => {
    const pinia = createPinia();
    createApp(App).use(pinia).mount('#app');
  });
})();
