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
    let currentDepartamentoId = 1; // TODO: Obter dinamicamente do backend (e.g., do modelo Thymeleaf)
    let currentUsuarioId = 1;     // TODO: Obter dinamicamente do backend (e.g., do modelo Thymeleaf)
    let currentConversationId = null;
    let typingTimer = null;
    const TYPING_DELAY = 1000; // 1 segundo

    // Função para conectar ao WebSocket
    function connect() {
        connectionStatus.textContent = 'Conectando...';
        connectionStatus.style.backgroundColor = '#F59E0B'; // Amarelo para conectando

        const socket = new SockJS('/ws');
        stompClient = StompJs.Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        connectionStatus.textContent = 'Online';
        connectionStatus.style.backgroundColor = '#10B981'; // Verde para online

        // Inscrever-se em tópicos gerais
        stompClient.subscribe(`/topic/departamento/${currentDepartamentoId}/mensagens`, onMessageReceived);
        stompClient.subscribe(`/topic/departamento/${currentDepartamentoId}/digitacao`, onTypingNotification);
        stompClient.subscribe(`/topic/departamento/${currentDepartamentoId}/reacao`, onReactionNotification);

        // Inscrever-se em tópicos específicos do usuário (se houver)
        // stompClient.subscribe(`/user/${currentUsuarioId}/queue/mensagens`, onMessageReceived);

        // Carregar conversas iniciais
        loadConversations();
    }

    function onError(error) {
        connectionStatus.textContent = 'Offline';
        connectionStatus.style.backgroundColor = '#EF4444'; // Vermelho para offline
        console.error('Erro ao conectar ao WebSocket:', error);
        // Tentar reconectar após um tempo
        setTimeout(connect, 5000);
    }

    // Função para carregar conversas existentes
     function loadConversations() {
         fetch(`/chat-web/conversas/departamento/${currentDepartamentoId}`)
             .then(response => response.json())
             .then(conversas => {
                conversationsList.innerHTML = '';
                if (conversas.length === 0) {
                    conversationsList.innerHTML = '<p class="text-center text-muted mt-3">Nenhuma conversa encontrada.</p>';
                } else {
                    conversas.forEach(conversa => {
                        addConversationToList(conversa);
                    });
                    // Selecionar a primeira conversa por padrão, se houver
                    if (conversas.length > 0) {
                        selectConversation(conversas[0].id);
                    }
                }
            })
            .catch(error => console.error('Erro ao carregar conversas:', error));
    }

    // Adicionar uma conversa à lista na sidebar
    function addConversationToList(conversa) {
        const conversationItem = document.createElement('div');
        conversationItem.classList.add('conversation-item');
        conversationItem.dataset.conversationId = conversa.id;
        conversationItem.innerHTML = `
            <div class="conversation-avatar department-avatar">
                ${conversa.nomeDepartamento ? conversa.nomeDepartamento.charAt(0) : 'D'}
            </div>
            <div class="conversation-info">
                <div class="conversation-header">
                    <h5 class="conversation-name">${conversa.nomeDepartamento || 'Conversa do Departamento'}</h5>
                    <span class="conversation-time">${formatTime(conversa.ultimaMensagemData)}</span>
                </div>
                <p class="conversation-preview">${conversa.ultimaMensagemConteudo || 'Nenhuma mensagem'}</p>
                <div class="conversation-meta">
                    <span class="participants-count">${conversa.participantes ? conversa.participantes.length : 0} participantes</span>
                    <!-- <span class="unread-badge">3</span> -->
                </div>
            </div>
        `;
        conversationItem.addEventListener('click', () => selectConversation(conversa.id));
        conversationsList.appendChild(conversationItem);
    }

    // Selecionar uma conversa
    function selectConversation(conversationId) {
        if (currentConversationId === conversationId) return;

        // Remover seleção da conversa anterior
        const prevSelected = document.querySelector('.conversation-item.active');
        if (prevSelected) {
            prevSelected.classList.remove('active');
        }

        // Adicionar seleção à nova conversa
        const newSelected = document.querySelector(`.conversation-item[data-conversation-id="${conversationId}"]`);
        if (newSelected) {
            newSelected.classList.add('active');
        }

        currentConversationId = conversationId;
        emptyState.style.display = 'none';
        activeChat.style.display = 'flex';
        chatMessages.innerHTML = ''; // Limpar mensagens anteriores

        // TODO: Atualizar nome do contato e status no cabeçalho do chat
        // chatContactName.textContent = newSelected.querySelector('.conversation-name').textContent;
        // chatContactStatusText.textContent = 'Online'; // Ou buscar status real

        loadMessages(conversationId);
    }

    // Carregar mensagens de uma conversa
    function loadMessages(conversationId) {
        fetch(`/chat/mensagens/conversa/${conversationId}`)
            .then(response => response.json())
            .then(mensagens => {
                chatMessages.innerHTML = '';
                mensagens.forEach(mensagem => {
                    displayMessage(mensagem);
                });
                chatMessages.scrollTop = chatMessages.scrollHeight; // Scroll para o final
            })
            .catch(error => console.error('Erro ao carregar mensagens:', error));
    }

    // Exibir uma mensagem no chat
    function displayMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message');
        messageElement.classList.add(message.remetenteId === currentUsuarioId ? 'sent' : 'received');

        // TODO: Adicionar avatar e nome do remetente
        messageElement.innerHTML = `
            <div class="message-content">
                <div class="message-header">
                    <span class="sender-name">${message.remetenteNome || 'Desconhecido'}</span>
                    <!-- <span class="sender-role">Admin</span> -->
                </div>
                <div class="message-bubble">${message.conteudo}</div>
                <div class="message-time">
                    ${formatTime(message.dataEnvio)}
                    <!-- <span class="message-status">✓✓</span> -->
                </div>
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
            const chatMessage = {
                conteudo: messageContent,
                remetenteId: currentUsuarioId,
                conversaId: currentConversationId,
                dataEnvio: new Date().toISOString()
            };
            stompClient.send(`/app/chat.sendMessage/${currentConversationId}`, {}, JSON.stringify(chatMessage));
            messageInput.value = '';
            // Otimista: Adicionar a própria mensagem imediatamente
            displayMessage(chatMessage);
        }
    }

    // Receber mensagem via WebSocket
    function onMessageReceived(payload) {
        const message = JSON.parse(payload.body);
        if (message.conversaId === currentConversationId) {
            displayMessage(message);
        }
        // TODO: Atualizar preview da conversa na sidebar
    }

    // Notificação de digitação
    messageInput.addEventListener('input', function() {
        if (stompClient && currentConversationId) {
            stompClient.send(`/app/chat.typing/${currentConversationId}`, {}, JSON.stringify({
                remetenteId: currentUsuarioId,
                digitando: true
            }));
            clearTimeout(typingTimer);
            typingTimer = setTimeout(() => {
                stompClient.send(`/app/chat.typing/${currentConversationId}`, {}, JSON.stringify({
                    remetenteId: currentUsuarioId,
                    digitando: false
                }));
            }, TYPING_DELAY);
        }
    });

    function onTypingNotification(payload) {
        const notification = JSON.parse(payload.body);
        if (notification.conversaId === currentConversationId && notification.remetenteId !== currentUsuarioId) {
            if (notification.digitando) {
                typingUserName.textContent = notification.remetenteNome || 'Alguém'; // TODO: Obter nome real
                typingIndicator.style.display = 'flex';
            } else {
                typingIndicator.style.display = 'none';
            }
        }
    }

    // Notificação de reação (TODO: Implementar UI para reações)
    function onReactionNotification(payload) {
        const reaction = JSON.parse(payload.body);
        console.log('Reação recebida:', reaction);
        // TODO: Atualizar UI para mostrar a reação na mensagem correspondente
    }

    // Nova Conversa Modal
    newConversationBtn.addEventListener('click', function() {
        newConversationModal.show();
        loadUsersForNewConversation();
    });

    userSearchInput.addEventListener('input', function() {
        loadUsersForNewConversation(userSearchInput.value);
    });

    function loadUsersForNewConversation(searchTerm = '') {
        fetch(`/usuarios/search?term=${searchTerm}`) // TODO: Criar endpoint de busca de usuários
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
        // TODO: Implementar lógica para criar uma nova conversa no backend
        // Por enquanto, vamos simular a criação e selecionar a conversa
        console.log('Iniciando nova conversa com usuário:', targetUserId);
        newConversationModal.hide();
        // Após criar a conversa no backend, recarregar a lista de conversas e selecionar a nova
        // loadConversations();
        // selectConversation(novaConversaId);
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
