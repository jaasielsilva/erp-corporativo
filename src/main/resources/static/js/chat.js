document.addEventListener('DOMContentLoaded', function() {
    const messageInput = document.getElementById('messageInput');
    const sendButton = document.getElementById('sendButton');
    const chatMessages = document.getElementById('messagesArea');
    const conversationsList = document.getElementById('conversationsList');
    const newConversationBtn = document.getElementById('newConversationBtn');
    const newConversationModal = new bootstrap.Modal(document.getElementById('newConversationModal'));
    const userSearchInput = document.getElementById('userSearch');
    const usersList = document.getElementById('usersList');
    const searchInput = document.getElementById('searchInput');
    const newGroupConversationBtn = document.getElementById('newGroupConversationBtn');
    const newGroupConversationModalEl = document.getElementById('newGroupConversationModal');
    const newGroupConversationModal = newGroupConversationModalEl ? new bootstrap.Modal(newGroupConversationModalEl) : null;
    const groupTitleInput = document.getElementById('groupTitleInput');
    const groupUserSearch = document.getElementById('groupUserSearch');
    const groupUsersList = document.getElementById('groupUsersList');
    const createGroupConversationBtn = document.getElementById('createGroupConversationBtn');
    const attachmentBtn = document.getElementById('attachmentBtn');
    const emojiBtn = document.getElementById('emojiBtn');
    const attachmentInput = document.getElementById('attachmentInput');
    const emptyState = document.getElementById('emptyState');
    const activeChat = document.getElementById('activeChat');
    const chatContactName = document.getElementById('chatContactName');
    const chatContactStatusText = document.getElementById('chatContactStatusText');
    const typingIndicator = document.getElementById('typingIndicator');
    const typingUserName = document.getElementById('typingUserName');
    const connectionStatus = document.getElementById('connectionStatus');

    let stompClient = null;
    const CHAT_CONTEXT = window.CHAT_CONTEXT || {};
    const currentUsuarioId = CHAT_CONTEXT.usuarioId;
    const currentUsuarioNome = CHAT_CONTEXT.usuarioNome;
    let currentConversationId = null;
    let conversationSubscription = null;
    let typingSubscription = null;
    let typingTimer = null;
    const TYPING_DELAY = 1000; // 1 segundo

    // Função para conectar ao WebSocket (com auto-reconexão)
    function connect() {
        connectionStatus.textContent = 'Conectando...';
        connectionStatus.style.backgroundColor = '#F59E0B';

        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            reconnectDelay: 5000, // tenta reconectar após 5s
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => console.log(str),
            onConnect: onConnected,
            onStompError: onStompError
        });
        stompClient.activate();
    }

    function onConnected() {
        connectionStatus.textContent = 'Online';
        connectionStatus.style.backgroundColor = '#10B981';

        // Tópico privado de mensagens para o usuário
        stompClient.subscribe('/user/queue/mensagens', onPrivateMessage);
        // Tópico de erros para o usuário
        stompClient.subscribe('/user/queue/errors', onUserError);
        // Status de usuários (online/offline)
        stompClient.subscribe('/topic/usuarios.status', onUserStatus);

        // Carregar conversas iniciais
        loadConversations();
    }

    function onStompError(frame) {
        connectionStatus.textContent = 'Offline';
        connectionStatus.style.backgroundColor = '#EF4444';
        console.error('Erro STOMP:', frame);
        // auto-reconexão já está habilitada via reconnectDelay
    }

    // Carregar conversas existentes via REST oficial
    function loadConversations() {
        fetch('/api/chat/conversas')
            .then(response => response.json())
            .then(conversas => {
                conversationsList.innerHTML = '';
                if (!conversas || conversas.length === 0) {
                    conversationsList.innerHTML = '<p class="text-center text-muted mt-3">Nenhuma conversa encontrada.</p>';
                } else {
                    conversas.forEach(addConversationToList);
                    // Selecionar a primeira conversa por padrão
                    selectConversation(conversas[0].id);
                    // Aplicar filtro atual, se houver
                    if (searchInput && searchInput.value) filterConversations(searchInput.value);
                }
            })
            .catch(error => console.error('Erro ao carregar conversas:', error));
    }

    // Renderizar item de conversa (usa ConversaDTO)
    function addConversationToList(conversa) {
        const conversationItem = document.createElement('div');
        conversationItem.classList.add('conversation-item');
        conversationItem.dataset.conversationId = conversa.id;

        const avatarInitial = (conversa.outroUsuarioNome || 'U').charAt(0);
        conversationItem.innerHTML = `
            <div class="conversation-avatar department-avatar">${avatarInitial}</div>
            <div class="conversation-info">
                <div class="conversation-header">
                    <h5 class="conversation-name">${conversa.outroUsuarioNome || 'Conversa'}</h5>
                    <span class="conversation-time">${conversa.ultimaAtividadeFormatada || ''}</span>
                </div>
                <p class="conversation-preview">${conversa.ultimaMensagem || 'Nenhuma mensagem'}</p>
                <div class="conversation-meta">
                    ${conversa.mensagensNaoLidas > 0 ? `<span class="unread-badge">${conversa.mensagensNaoLidas}</span>` : ''}
                </div>
            </div>
        `;
        conversationItem.addEventListener('click', () => selectConversation(conversa.id));
        conversationsList.appendChild(conversationItem);
    }

    // Filtro de conversas da sidebar
    function filterConversations(term) {
        const normalized = (term || '').toLowerCase();
        const items = conversationsList.querySelectorAll('.conversation-item');
        items.forEach(item => {
            const name = item.querySelector('.conversation-name')?.textContent?.toLowerCase() || '';
            const preview = item.querySelector('.conversation-preview')?.textContent?.toLowerCase() || '';
            const match = !normalized || name.includes(normalized) || preview.includes(normalized);
            item.style.display = match ? '' : 'none';
        });
    }

    if (searchInput) {
        searchInput.addEventListener('input', () => filterConversations(searchInput.value));
    }

    // Selecionar uma conversa
    function selectConversation(conversationId) {
        if (currentConversationId === conversationId) return;

        // Remover seleção anterior
        const prevSelected = document.querySelector('.conversation-item.active');
        if (prevSelected) prevSelected.classList.remove('active');

        // Marcar nova seleção
        const newSelected = document.querySelector(`.conversation-item[data-conversation-id="${conversationId}"]`);
        if (newSelected) newSelected.classList.add('active');

        // Cancelar subscrições anteriores
        if (conversationSubscription) conversationSubscription.unsubscribe();
        if (typingSubscription) typingSubscription.unsubscribe();

        currentConversationId = conversationId;
        emptyState.style.display = 'none';
        activeChat.style.display = 'flex';
        chatMessages.innerHTML = '';

        // Atualizar cabeçalho (nome)
        if (newSelected) {
            const nome = newSelected.querySelector('.conversation-name').textContent;
            chatContactName.textContent = nome;
        }

        // Subscrições de eventos da conversa
        conversationSubscription = stompClient.subscribe(`/topic/conversa.${conversationId}`, onConversaEvent);
        typingSubscription = stompClient.subscribe(`/topic/chat.conversa.${conversationId}.digitando`, onTypingTopic);

        // Carregar mensagens
        loadMessages(conversationId);
    }

    // Carregar mensagens da conversa
    function loadMessages(conversationId) {
        fetch(`/api/chat/conversas/${conversationId}/mensagens`)
            .then(async response => {
                const contentType = response.headers.get('content-type') || '';
                if (!response.ok) {
                    const sample = await response.text();
                    console.error('Erro ao carregar mensagens:', response.status, sample ? sample.substring(0, 1000) : '');
                    return;
                }
                if (contentType.includes('application/json')) {
                    const mensagens = await response.json();
                    chatMessages.innerHTML = '';
                    (mensagens || []).forEach(displayMessage);
                    chatMessages.scrollTop = chatMessages.scrollHeight;
                } else {
                    const sample = await response.text();
                    console.error('Erro ao carregar mensagens: resposta não é JSON válido', {
                        amostra: sample ? sample.substring(0, 1000) : ''
                    });
                }
            })
            .catch(error => console.error('Erro ao carregar mensagens:', error));
    }

    // Renderizar uma mensagem
    function displayMessage(message) {
        const messageElement = document.createElement('div');
        const isMine = message.remetente && message.remetente.id === currentUsuarioId;
        messageElement.classList.add('message', isMine ? 'sent' : 'received');

        const senderName = message.remetente ? message.remetente.nome : (message.remetenteNome || 'Desconhecido');
        const timeText = formatTime(message.dataEnvio || message.timestamp);
        const mensagemId = message.id || message.mensagemId;

        // Renderizar conteúdo baseado no tipo da mensagem
        let messageContent = '';
        if (message.tipo === 'IMAGEM' && message.arquivoUrl) {
            messageContent = `
                <div class="message-image">
                    <img src="/${message.arquivoUrl}" alt="${message.arquivoNome || 'Imagem'}" 
                         onclick="openImageModal(this.src)" style="max-width: 300px; max-height: 200px; border-radius: 8px; cursor: pointer;">
                    ${message.conteudo ? `<p>${message.conteudo}</p>` : ''}
                </div>
            `;
        } else if (message.tipo === 'ARQUIVO' && message.arquivoUrl) {
            const fileSize = message.arquivoTamanho ? formatFileSize(message.arquivoTamanho) : '';
            messageContent = `
                <div class="message-file">
                    <div class="file-info">
                        <span class="file-icon">📎</span>
                        <div class="file-details">
                            <a href="/${message.arquivoUrl}" target="_blank" class="file-name">${message.arquivoNome || 'Arquivo'}</a>
                            ${fileSize ? `<span class="file-size">${fileSize}</span>` : ''}
                        </div>
                    </div>
                    ${message.conteudo && !message.conteudo.startsWith('📎') ? `<p>${message.conteudo}</p>` : ''}
                </div>
            `;
        } else {
            messageContent = `<div class="message-bubble">${message.conteudo}</div>`;
        }

        messageElement.innerHTML = `
            <div class="message-content">
                <div class="message-header">
                    <span class="sender-name">${senderName}</span>
                </div>
                ${messageContent}
                <div class="message-time">${timeText}</div>
                <div class="message-reactions" data-mensagem-id="${mensagemId || ''}">
                    <button class="reaction-btn" data-emoji="👍" title="Reagir com 👍">👍</button>
                    <button class="reaction-btn" data-emoji="❤️" title="Reagir com ❤️">❤️</button>
                    <button class="reaction-btn" data-emoji="🎉" title="Reagir com 🎉">🎉</button>
                    <button class="reaction-btn" data-emoji="🔥" title="Reagir com 🔥">🔥</button>
                    <span class="reaction-status text-muted"></span>
                </div>
            </div>
        `;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        // Eventos de reação
        const reactionContainer = messageElement.querySelector('.message-reactions');
        if (reactionContainer && mensagemId) {
            reactionContainer.querySelectorAll('.reaction-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    const emoji = this.dataset.emoji;
                    toggleReaction(mensagemId, emoji, reactionContainer);
                });
            });
            // Carregar reações atuais (opcional)
            loadReactions(mensagemId, reactionContainer);
        }
    }

    // Enviar mensagem
    if (sendButton) {
        sendButton.addEventListener('click', sendMessage);
    } else {
        console.error('Elemento com ID "sendButton" não encontrado no DOM');
    }
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    function sendMessage() {
        const messageContent = messageInput.value.trim();
        if (messageContent && stompClient && currentConversationId) {
            const payload = {
                conversaId: currentConversationId,
                conteudo: messageContent
            };
            stompClient.publish({ destination: '/app/chat.enviarMensagem', body: JSON.stringify(payload) });
            messageInput.value = '';
        }
    }

    // Reações de mensagens
    function toggleReaction(mensagemId, emoji, container) {
        fetch(`/api/chat/reacoes/${mensagemId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ emoji })
        })
        .then(async response => {
            const text = await response.text();
            const statusEl = container.querySelector('.reaction-status');
            if (response.ok) {
                statusEl.textContent = 'Reação atualizada';
                setTimeout(() => statusEl.textContent = '', 1500);
                loadReactions(mensagemId, container);
            } else {
                statusEl.textContent = text || 'Erro ao reagir';
                setTimeout(() => statusEl.textContent = '', 2500);
            }
        })
        .catch(err => {
            const statusEl = container.querySelector('.reaction-status');
            statusEl.textContent = 'Falha na requisição';
            setTimeout(() => statusEl.textContent = '', 2500);
            console.error('Erro ao enviar reação:', err);
        });
    }

    function loadReactions(mensagemId, container) {
        fetch(`/api/chat/reacoes/${mensagemId}`)
            .then(response => response.json())
            .then(reacoes => {
                // Renderizar contagem simples por emoji
                const counts = {};
                (reacoes || []).forEach(r => {
                    counts[r.emoji] = (counts[r.emoji] || 0) + 1;
                });
                // Atualizar labels nos botões
                container.querySelectorAll('.reaction-btn').forEach(btn => {
                    const emoji = btn.dataset.emoji;
                    const count = counts[emoji] || 0;
                    btn.textContent = count > 0 ? `${emoji} ${count}` : emoji;
                });
            })
            .catch(err => console.error('Erro ao buscar reações:', err));
    }

    // Mensagens privadas recebidas (para outros participantes)
    function onPrivateMessage(frame) {
        try {
            const message = JSON.parse(frame.body);
            if (message.conversaId === currentConversationId) {
                displayMessage(message);
            } else {
                // Atualizar preview/unread em lista (opcional)
            }
        } catch (e) {
            console.warn('Payload privado não parseável', e);
        }
    }

    // Eventos genéricos da conversa (lida, digitando via controller, etc.)
    function onConversaEvent(frame) {
        try {
            const event = JSON.parse(frame.body);
            if (event.tipo === 'USUARIO_DIGITANDO') {
                if (event.usuarioId !== currentUsuarioId) {
                    typingUserName.textContent = event.usuario || 'Alguém';
                    typingIndicator.style.display = 'flex';
                }
            } else if (event.tipo === 'USUARIO_PAROU_DIGITAR') {
                if (event.usuarioId !== currentUsuarioId) {
                    typingIndicator.style.display = 'none';
                }
            } else if (event.tipo === 'MENSAGEM_LIDA') {
                // Pode marcar UI de mensagem como lida
            } else {
                // Se vierem mensagens diretamente aqui
                if (event.conteudo) displayMessage(event);
            }
        } catch (e) {
            console.warn('Evento de conversa não parseável', e);
        }
    }

    // Evento de digitação via tópico dedicado
    function onTypingTopic(frame) {
        try {
            const notification = JSON.parse(frame.body);
            if (notification.usuarioId !== currentUsuarioId) {
                if (notification.digitando) {
                    typingUserName.textContent = 'Digitando...';
                    typingIndicator.style.display = 'flex';
                } else {
                    typingIndicator.style.display = 'none';
                }
            }
        } catch (e) {
            console.warn('Notificação de digitação não parseável', e);
        }
    }

    // Enviar indicações de digitação via controller
    messageInput.addEventListener('input', function() {
        if (stompClient && currentConversationId) {
            stompClient.publish({ destination: '/app/chat.digitando', body: JSON.stringify({ conversaId: currentConversationId }) });
            clearTimeout(typingTimer);
            typingTimer = setTimeout(() => {
                stompClient.publish({ destination: '/app/chat.pararDigitar', body: JSON.stringify({ conversaId: currentConversationId }) });
            }, TYPING_DELAY);
        }
    });

    // Erros direcionados ao usuário
    function onUserError(frame) {
        try {
            const err = JSON.parse(frame.body);
            console.error('Erro do servidor:', err.error || err);
        } catch (e) {
            console.error('Erro do servidor (texto):', frame.body);
        }
    }

    // Status de usuários (online/offline)
    function onUserStatus(frame) {
        try {
            const status = JSON.parse(frame.body);
            if (status.tipo === 'USUARIO_ONLINE' || status.tipo === 'USUARIO_OFFLINE') {
                // Atualize UI de presença conforme necessário
            }
        } catch (e) {
            console.warn('Status de usuário não parseável', e);
        }
    }

    // Modal Nova Conversa
    newConversationBtn.addEventListener('click', function() {
        newConversationModal.show();
        loadUsersForNewConversation();
    });

    userSearchInput.addEventListener('input', function() {
        loadUsersForNewConversation(userSearchInput.value);
    });

    function loadUsersForNewConversation(searchTerm = '') {
        const q = encodeURIComponent(searchTerm || '');
        fetch(`/api/usuarios/busca?q=${q}`)
            .then(response => response.json())
            .then(users => {
                usersList.innerHTML = '';
                if (users.length === 0) {
                    usersList.innerHTML = '<p class="text-center text-muted mt-3">Nenhum usuário encontrado.</p>';
                } else {
                    users.forEach(user => {
                        const userItem = document.createElement('div');
                        userItem.classList.add('list-group-item', 'list-group-item-action', 'd-flex', 'justify-content-between', 'align-items-center', 'user-item');
                        userItem.innerHTML = `
                            <div>
                                <h6 class="mb-1">${user.nome}</h6>
                                <small class="text-muted">${user.departamento || 'Sem Departamento'}</small>
                            </div>
                            <button class="btn btn-primary btn-sm start-conversation-btn" data-user-id="${user.id}">Iniciar Conversa</button>
                        `;
                        usersList.appendChild(userItem);
                    });
                    document.querySelectorAll('.start-conversation-btn').forEach(button => {
                        button.addEventListener('click', function() {
                            const targetUserId = this.dataset.userId;
                            startNewConversation(targetUserId);
                        });
                    });
                }
            })
            .catch(error => console.error('Erro ao buscar usuários:', error));
    }

    // ===== Grupo: carregar e criar =====
    let selectedGroupParticipants = new Set();

    function loadUsersForNewGroup(searchTerm = '') {
        const q = encodeURIComponent(searchTerm || '');
        fetch(`/api/usuarios/busca?q=${q}`)
            .then(response => response.json())
            .then(users => {
                groupUsersList.innerHTML = '';
                if (!users || users.length === 0) {
                    groupUsersList.innerHTML = '<p class="text-center text-muted mt-3">Nenhum usuário encontrado.</p>';
                    return;
                }
                users.forEach(user => {
                    const row = document.createElement('div');
                    row.classList.add('list-group-item', 'd-flex', 'align-items-center', 'justify-content-between', 'user-item');
                    row.innerHTML = `
                        <div class="d-flex align-items-center" style="gap: 10px;">
                            <input type="checkbox" class="form-check-input" data-user-id="${user.id}" ${selectedGroupParticipants.has(user.id) ? 'checked' : ''} />
                            <div>
                                <h6 class="mb-0">${user.nome}</h6>
                                <small class="text-muted">${user.departamento || 'Sem Departamento'}</small>
                            </div>
                        </div>
                    `;
                    const checkbox = row.querySelector('input[type="checkbox"]');
                    checkbox.addEventListener('change', function() {
                        const uid = parseInt(this.dataset.userId);
                        if (this.checked) {
                            selectedGroupParticipants.add(uid);
                        } else {
                            selectedGroupParticipants.delete(uid);
                        }
                    });
                    groupUsersList.appendChild(row);
                });
            })
            .catch(err => console.error('Erro ao buscar usuários (grupo):', err));
    }

    function startNewGroupConversation() {
        const titulo = (groupTitleInput?.value || '').trim();
        if (!titulo) {
            alert('Informe um título para o grupo.');
            return;
        }
        const participantes = Array.from(selectedGroupParticipants);
        if (participantes.length === 0) {
            alert('Selecione pelo menos um participante.');
            return;
        }
        fetch('/api/chat/conversas/grupo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ titulo, participantes })
        })
        .then(response => {
            if (!response.ok) throw new Error('Falha ao criar grupo');
            return response.json();
        })
        .then(conversa => {
            if (newGroupConversationModal) newGroupConversationModal.hide();
            selectedGroupParticipants.clear();
            groupTitleInput.value = '';
            groupUsersList.innerHTML = '';
            loadConversations();
            if (conversa && conversa.id) selectConversation(conversa.id);
        })
        .catch(err => console.error('Erro ao criar grupo:', err));
    }

    if (newGroupConversationBtn && newGroupConversationModal) {
        newGroupConversationBtn.addEventListener('click', function() {
            selectedGroupParticipants.clear();
            if (groupTitleInput) groupTitleInput.value = '';
            newGroupConversationModal.show();
            loadUsersForNewGroup();
        });
    }

    if (groupUserSearch) {
        groupUserSearch.addEventListener('input', function() {
            loadUsersForNewGroup(groupUserSearch.value);
        });
    }

    if (createGroupConversationBtn) {
        createGroupConversationBtn.addEventListener('click', startNewGroupConversation);
    }

    // ===== Anexos e Emojis (UI) =====
    if (attachmentBtn && attachmentInput) {
        attachmentBtn.addEventListener('click', () => attachmentInput.click());
        attachmentInput.addEventListener('change', function() {
            const file = this.files && this.files[0];
            if (!file || !currentConversationId) return;
            
            // Upload real do arquivo
            uploadFile(file, currentConversationId);
            this.value = '';
        });
    }

    if (emojiBtn && messageInput) {
        emojiBtn.addEventListener('click', function() {
            const emojis = ['🙂','😊','👍','🔥','🚀','🎉','❤️'];
            const chosen = emojis[Math.floor(Math.random() * emojis.length)];
            const start = messageInput.selectionStart || messageInput.value.length;
            const end = messageInput.selectionEnd || messageInput.value.length;
            messageInput.value = messageInput.value.substring(0, start) + chosen + messageInput.value.substring(end);
            messageInput.focus();
        });
    }

    function startNewConversation(targetUserId) {
        if (!targetUserId) return;
        fetch(`/api/chat/conversas/individual?destinatarioId=${encodeURIComponent(targetUserId)}`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) throw new Error('Falha ao criar conversa');
            return response.json();
        })
        .then(conversa => {
            newConversationModal.hide();
            // Recarrega lista e seleciona a nova conversa
            loadConversations();
            if (conversa && conversa.id) selectConversation(conversa.id);
        })
        .catch(err => {
            console.error('Erro ao criar conversa:', err);
        });
    }


    // Funções utilitárias
    function formatTime(isoString) {
        if (!isoString) return '';
        const date = new Date(isoString);
        return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    }

    // ===== Funções auxiliares para arquivos =====
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function openImageModal(imageSrc) {
        // Criar modal simples para visualizar imagem
        const modal = document.createElement('div');
        modal.className = 'image-modal';
        modal.innerHTML = `
            <div class="image-modal-content">
                <span class="image-modal-close">&times;</span>
                <img src="${imageSrc}" alt="Imagem ampliada">
            </div>
        `;
        
        document.body.appendChild(modal);
        
        // Fechar modal
        const closeBtn = modal.querySelector('.image-modal-close');
        const closeModal = () => {
            document.body.removeChild(modal);
        };
        
        closeBtn.addEventListener('click', closeModal);
        modal.addEventListener('click', function(e) {
            if (e.target === modal) closeModal();
        });
        
        // Fechar com ESC
        const handleKeydown = (e) => {
            if (e.key === 'Escape') {
                closeModal();
                document.removeEventListener('keydown', handleKeydown);
            }
        };
        document.addEventListener('keydown', handleKeydown);
    }

    // ===== Upload de Arquivos =====
    function uploadFile(file, conversaId) {
        const formData = new FormData();
        formData.append('arquivo', file);
        formData.append('conteudo', ''); // Conteúdo opcional, será gerado automaticamente se vazio

        // Mostrar indicador de upload
        const uploadIndicator = document.createElement('div');
        uploadIndicator.className = 'upload-indicator';
        uploadIndicator.innerHTML = `
            <div class="upload-progress">
                <span>📤 Enviando ${file.name}...</span>
                <div class="progress-bar">
                    <div class="progress-fill"></div>
                </div>
            </div>
        `;
        messagesContainer.appendChild(uploadIndicator);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        fetch(`/api/chat/conversas/${conversaId}/mensagens/upload`, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erro no upload: ${response.status}`);
            }
            return response.json();
        })
        .then(mensagem => {
            // Remover indicador de upload
            uploadIndicator.remove();
            
            // Adicionar mensagem à interface
            displayMessage(mensagem);
            
            // Notificar outros participantes via WebSocket
            if (stompClient && stompClient.connected) {
                const notification = {
                    conversaId: conversaId,
                    mensagemId: mensagem.id,
                    remetenteId: mensagem.remetente.id,
                    tipo: 'NOVA_MENSAGEM'
                };
                stompClient.publish({ destination: '/app/chat.notificarMensagem', body: JSON.stringify(notification) });
            }
        })
        .catch(error => {
            console.error('Erro no upload:', error);
            uploadIndicator.innerHTML = `
                <div class="upload-error">
                    ❌ Erro ao enviar ${file.name}
                    <button onclick="this.parentElement.parentElement.remove()">×</button>
                </div>
            `;
        });
    }

    // Iniciar conexão WebSocket
    connect();
});
