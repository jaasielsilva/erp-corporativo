document.addEventListener('DOMContentLoaded', function() {
    const messageInput = document.getElementById('messageInput');
    const sendButton = document.getElementById('sendButton');
    const chatMessages = document.getElementById('messagesArea');
    const conversationsList = document.getElementById('conversationsList');
    const newConversationBtn = document.getElementById('newConversationBtn');
    const newConversationModal = new bootstrap.Modal(document.getElementById('newConversationModal'));
    const userSearchInput = document.getElementById('userSearch');
    const usersList = document.getElementById('usersList');
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

    // Função para conectar ao WebSocket
    function connect() {
        connectionStatus.textContent = 'Conectando...';
        connectionStatus.style.backgroundColor = '#F59E0B';

        const socket = new SockJS('/ws');
        stompClient = StompJs.Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
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

    function onError(error) {
        connectionStatus.textContent = 'Offline';
        connectionStatus.style.backgroundColor = '#EF4444';
        console.error('Erro ao conectar ao WebSocket:', error);
        setTimeout(connect, 5000);
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
            .then(response => response.json())
            .then(mensagens => {
                chatMessages.innerHTML = '';
                mensagens.forEach(displayMessage);
                chatMessages.scrollTop = chatMessages.scrollHeight;
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

        messageElement.innerHTML = `
            <div class="message-content">
                <div class="message-header">
                    <span class="sender-name">${senderName}</span>
                </div>
                <div class="message-bubble">${message.conteudo}</div>
                <div class="message-time">${timeText}</div>
            </div>
        `;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
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
            stompClient.send('/app/chat.enviarMensagem', {}, JSON.stringify(payload));
            messageInput.value = '';
        }
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
            stompClient.send('/app/chat.digitando', {}, JSON.stringify({ conversaId: currentConversationId }));
            clearTimeout(typingTimer);
            typingTimer = setTimeout(() => {
                stompClient.send('/app/chat.pararDigitar', {}, JSON.stringify({ conversaId: currentConversationId }));
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

    // Iniciar conexão WebSocket
    connect();
});
