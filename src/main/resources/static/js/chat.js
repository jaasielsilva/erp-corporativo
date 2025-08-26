/**
 * ==================== CHAT INTERNO - ERP ====================
 * Sistema de chat em tempo real com WebSocket
 * Funcionalidades: envio de mensagens, notificações, indicador de digitação
 */

// ==================== VARIÁVEIS GLOBAIS ====================
let stompClient = null;
let currentUser = null;
let currentConversation = null;
let isConnected = false;
let typingTimeout = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 3000;

// ==================== INICIALIZAÇÃO ====================
$(document).ready(function() {
    initializeChat();
});

/**
 * Inicializa o sistema de chat
 */
function initializeChat() {
    console.log('Inicializando sistema de chat...');
    
    // Carrega dados do usuário logado
    loadCurrentUser();
    
    // Conecta ao WebSocket
    connectWebSocket();
    
    // Carrega conversas
    loadConversations();
    
    // Configura eventos da interface
    setupEventListeners();
    
    // Auto-refresh das conversas a cada 30 segundos
    setInterval(loadConversations, 30000);
}

/**
 * Carrega dados do usuário atual
 */
function loadCurrentUser() {
    $.ajax({
        url: '/api/chat/usuario-atual',
        method: 'GET',
        success: function(user) {
            currentUser = user;
            console.log('Usuário carregado:', user.nome);
            updateConnectionStatus('Conectado');
        },
        error: function(xhr) {
            console.error('Erro ao carregar usuário:', xhr.responseText);
            showNotification('Erro ao carregar dados do usuário', 'error');
        }
    });
}

// ==================== WEBSOCKET ====================

/**
 * Conecta ao WebSocket usando SockJS e STOMP
 */
function connectWebSocket() {
    if (isConnected) {
        console.log('WebSocket já está conectado');
        return;
    }
    
    console.log('Conectando ao WebSocket...');
    updateConnectionStatus('Conectando...');
    
    const socket = new SockJS('/ws');
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        debug: function(str) {
            console.log('STOMP: ' + str);
        }
    });
    
    stompClient.onConnect = function(frame) {
        console.log('Conectado ao WebSocket: ' + frame);
        isConnected = true;
        reconnectAttempts = 0;
        updateConnectionStatus('Conectado');
            
        // Subscreve aos tópicos
        subscribeToTopics();
    };
    
    stompClient.onStompError = function(frame) {
        console.error('Erro na conexão WebSocket:', frame.headers['message']);
        isConnected = false;
        updateConnectionStatus('Desconectado');
        
        // Tenta reconectar
        scheduleReconnect();
    };
    
    stompClient.activate();
}

/**
 * Subscreve aos tópicos do WebSocket
 */
function subscribeToTopics() {
    if (!stompClient || !isConnected || !currentUser) {
        console.warn('Não é possível subscrever: WebSocket não conectado ou usuário não carregado');
        return;
    }
    
    // Mensagens pessoais
    stompClient.subscribe('/user/queue/messages', function(message) {
        const messageData = JSON.parse(message.body);
        handleIncomingMessage(messageData);
    });
    
    // Status de usuários
    stompClient.subscribe('/topic/user-status', function(message) {
        const statusData = JSON.parse(message.body);
        updateUserStatus(statusData);
    });
    
    // Indicador de digitação
    stompClient.subscribe('/topic/typing', function(message) {
        const typingData = JSON.parse(message.body);
        handleTypingIndicator(typingData);
    });
    
    console.log('Subscrito aos tópicos do WebSocket');
}

/**
 * Agenda uma tentativa de reconexão
 */
function scheduleReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        console.error('Máximo de tentativas de reconexão atingido');
        updateConnectionStatus('Falha na conexão');
        showNotification('Não foi possível conectar ao servidor. Recarregue a página.', 'error');
        return;
    }
    
    reconnectAttempts++;
    console.log(`Tentativa de reconexão ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS} em ${RECONNECT_DELAY}ms`);
    
    setTimeout(() => {
        if (!isConnected) {
            connectWebSocket();
        }
    }, RECONNECT_DELAY);
}

/**
 * Desconecta do WebSocket
 */
function disconnectWebSocket() {
    if (stompClient && isConnected) {
        stompClient.deactivate();
        console.log('Desconectado do WebSocket');
        isConnected = false;
        updateConnectionStatus('Desconectado');
    }
}

// ==================== CONVERSAS ====================

/**
 * Carrega lista de conversas
 */
function loadConversations() {
    $.ajax({
        url: '/api/chat/conversas',
        method: 'GET',
        success: function(conversations) {
            renderConversations(conversations);
        },
        error: function(xhr) {
            console.error('Erro ao carregar conversas:', xhr.responseText);
            showNotification('Erro ao carregar conversas', 'error');
        }
    });
}

/**
 * Renderiza lista de conversas na sidebar
 */
function renderConversations(conversations) {
    const conversationsList = $('.conversations-list');
    conversationsList.empty();
    
    if (!conversations || conversations.length === 0) {
        conversationsList.append(`
            <div class="text-center text-muted py-4">
                <i class="fas fa-comments fa-2x mb-2"></i>
                <p>Nenhuma conversa encontrada</p>
            </div>
        `);
        return;
    }
    
    conversations.forEach(conversation => {
        const conversationHtml = createConversationItem(conversation);
        conversationsList.append(conversationHtml);
    });
}

/**
 * Cria HTML para item de conversa
 */
function createConversationItem(conversation) {
    const isActive = currentConversation && currentConversation.id === conversation.id;
    const unreadBadge = conversation.mensagensNaoLidas > 0 ? 
        `<span class="unread-badge">${conversation.mensagensNaoLidas}</span>` : '';
    
    // Usar dados do DTO
    const avatar = conversation.outroUsuarioFoto ? 
        `<img src="${conversation.outroUsuarioFoto}" alt="${conversation.outroUsuarioNome}" class="avatar-img">` :
        `<img src="/img/default-avatar.svg" alt="${conversation.outroUsuarioNome || 'Usuário'}" class="avatar-img">`;
    
    const statusIndicator = conversation.outroUsuarioId ? 
        `<div class="status-indicator ${conversation.outroUsuarioOnline ? 'status-online' : 'status-offline'}"></div>` : '';
    
    const conversationType = '';
    
    return `
        <div class="conversation-item ${isActive ? 'active' : ''}" data-conversation-id="${conversation.id}">
            <div class="conversation-avatar">
                ${avatar}
                ${statusIndicator}
            </div>
            <div class="conversation-info">
                <div class="conversation-header">
                    <h6 class="conversation-name">${conversation.outroUsuarioNome || 'Usuário'}</h6>
                    <span class="conversation-time">${conversation.ultimaAtividadeFormatada || ''}</span>
                </div>
                <p class="conversation-preview">${conversation.ultimaMensagem || 'Nenhuma mensagem'}</p>
                <div class="conversation-meta">
                    <span class="participants-count">2 participantes</span>
                    ${conversationType}
                    ${unreadBadge}
                </div>
            </div>
        </div>
    `;
}

/**
 * Seleciona uma conversa
 */
function selectConversation(conversationId) {
    // Remove seleção anterior
    $('.conversation-item').removeClass('active');
    
    // Adiciona seleção atual
    $(`.conversation-item[data-conversation-id="${conversationId}"]`).addClass('active');
    
    // Carrega dados da conversa
    loadConversationData(conversationId);
    
    // Marca mensagens como lidas
    markMessagesAsRead(conversationId);
}

/**
 * Carrega dados de uma conversa específica
 */
function loadConversationData(conversationId) {
    $.ajax({
        url: `/api/chat/conversas/${conversationId}`,
        method: 'GET',
        success: function(conversation) {
            currentConversation = conversation;
            showChatArea();
            updateChatHeader(conversation);
            loadMessages(conversationId);
        },
        error: function(xhr) {
            console.error('Erro ao carregar conversa:', xhr.responseText);
            showNotification('Erro ao carregar conversa', 'error');
        }
    });
}

// ==================== MENSAGENS ====================

/**
 * Carrega mensagens de uma conversa
 */
function loadMessages(conversationId) {
    $.ajax({
        url: `/api/chat/conversas/${conversationId}/mensagens`,
        method: 'GET',
        success: function(messages) {
            renderMessages(messages);
            scrollToBottom();
        },
        error: function(xhr) {
            console.error('Erro ao carregar mensagens:', xhr.responseText);
            showNotification('Erro ao carregar mensagens', 'error');
        }
    });
}

/**
 * Renderiza mensagens na área de chat
 */
function renderMessages(messages) {
    const messagesArea = $('.messages-area');
    messagesArea.empty();
    
    if (!messages || messages.length === 0) {
        messagesArea.append(`
            <div class="text-center text-muted py-4">
                <i class="fas fa-comment fa-2x mb-2"></i>
                <p>Nenhuma mensagem ainda. Seja o primeiro a enviar!</p>
            </div>
        `);
        return;
    }
    
    messages.forEach(message => {
        const messageHtml = createMessageElement(message);
        messagesArea.append(messageHtml);
    });
}

/**
 * Cria elemento HTML para uma mensagem
 */
function createMessageElement(message) {
    const isOwn = message.senderId === currentUser.id;
    const messageClass = isOwn ? 'sent' : 'received';
    
    const avatar = !isOwn ? `
        <div class="message-avatar">
            <img src="${message.senderAvatar || '/img/default-avatar.png'}" alt="${message.senderName}" class="avatar-img">
        </div>
    ` : '';
    
    const messageHeader = !isOwn ? `
        <div class="message-header">
            <span class="sender-name">${message.senderName}</span>
            <span class="sender-role">${message.senderRole || 'Usuário'}</span>
        </div>
    ` : '';
    
    const readStatus = isOwn && message.isRead ? 
        `<i class="fas fa-check-double message-status read" title="Lida"></i>` :
        isOwn ? `<i class="fas fa-check message-status" title="Enviada"></i>` : '';
    
    return `
        <div class="message ${messageClass}">
            ${avatar}
            <div class="message-content">
                ${messageHeader}
                <div class="message-bubble">
                    ${escapeHtml(message.content)}
                </div>
                <div class="message-time">
                    ${formatTime(message.timestamp)}
                    ${readStatus}
                </div>
            </div>
        </div>
    `;
}

/**
 * Envia uma mensagem
 */
function sendMessage() {
    const messageInput = $('#messageInput');
    const content = messageInput.val().trim();
    
    if (!content || !currentConversation || !isConnected) {
        return;
    }
    
    const messageData = {
        conversationId: currentConversation.id,
        content: content,
        timestamp: new Date().toISOString()
    };
    
    // Envia via WebSocket
    stompClient.send('/app/chat.enviarMensagem', {}, JSON.stringify(messageData));
    
    // Limpa input
    messageInput.val('');
    
    // Para indicador de digitação
    stopTyping();
    
    // Atualiza UI imediatamente (otimistic update)
    addMessageToUI({
        ...messageData,
        senderId: currentUser.id,
        senderName: currentUser.nome,
        senderAvatar: currentUser.avatar,
        senderRole: currentUser.cargo,
        isRead: false
    });
    
    console.log('Mensagem enviada:', content);
}

/**
 * Adiciona mensagem à interface imediatamente
 */
function addMessageToUI(message) {
    const messagesArea = $('.messages-area');
    const messageHtml = createMessageElement(message);
    messagesArea.append(messageHtml);
    scrollToBottom();
}

/**
 * Manipula mensagens recebidas via WebSocket
 */
function handleIncomingMessage(messageData) {
    console.log('Mensagem recebida:', messageData);
    
    // Se é da conversa atual, adiciona à UI
    if (currentConversation && messageData.conversationId === currentConversation.id) {
        addMessageToUI(messageData);
        
        // Marca como lida automaticamente
        markMessageAsRead(messageData.id);
    } else {
        // Atualiza badge de não lidas
        updateUnreadBadge(messageData.conversationId);
        
        // Mostra notificação
        showNotification(`Nova mensagem de ${messageData.senderName}`, 'info');
    }
    
    // Atualiza lista de conversas
    loadConversations();
}

/**
 * Marca mensagem como lida
 */
function markMessageAsRead(messageId) {
    $.ajax({
        url: `/api/chat/mensagens/${messageId}/marcar-lida`,
        method: 'POST',
        error: function(xhr) {
            console.error('Erro ao marcar mensagem como lida:', xhr.responseText);
        }
    });
}

/**
 * Marca todas as mensagens de uma conversa como lidas
 */
function markMessagesAsRead(conversationId) {
    $.ajax({
        url: `/api/chat/conversas/${conversationId}/marcar-lidas`,
        method: 'POST',
        success: function() {
            // Remove badge de não lidas
            $(`.conversation-item[data-conversation-id="${conversationId}"] .unread-badge`).remove();
        },
        error: function(xhr) {
            console.error('Erro ao marcar mensagens como lidas:', xhr.responseText);
        }
    });
}

// ==================== INDICADOR DE DIGITAÇÃO ====================

/**
 * Inicia indicador de digitação
 */
function startTyping() {
    if (!currentConversation || !isConnected) return;
    
    const typingData = {
        conversationId: currentConversation.id,
        userId: currentUser.id,
        userName: currentUser.nome,
        isTyping: true
    };
    
    stompClient.send('/app/chat.typing', {}, JSON.stringify(typingData));
}

/**
 * Para indicador de digitação
 */
function stopTyping() {
    if (!currentConversation || !isConnected) return;
    
    const typingData = {
        conversationId: currentConversation.id,
        userId: currentUser.id,
        userName: currentUser.nome,
        isTyping: false
    };
    
    stompClient.send('/app/chat.typing', {}, JSON.stringify(typingData));
}

/**
 * Manipula indicador de digitação recebido
 */
function handleTypingIndicator(typingData) {
    if (!currentConversation || typingData.conversationId !== currentConversation.id) {
        return;
    }
    
    // Não mostra próprio indicador
    if (typingData.userId === currentUser.id) {
        return;
    }
    
    const typingIndicator = $('.typing-indicator');
    
    if (typingData.isTyping) {
        typingIndicator.find('.typing-text').text(`${typingData.userName} está digitando`);
        typingIndicator.show();
    } else {
        typingIndicator.hide();
    }
}

// ==================== EVENTOS DA INTERFACE ====================

/**
 * Configura todos os event listeners
 */
function setupEventListeners() {
    // Clique em conversa
    $(document).on('click', '.conversation-item', function() {
        const conversationId = $(this).data('conversation-id');
        selectConversation(conversationId);
    });
    
    // Envio de mensagem
    $('#sendButton').on('click', function() {
        sendMessage();
    });
    
    // Enter para enviar, Shift+Enter para quebra de linha
    $('#messageInput').on('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    
    // Indicador de digitação
    $('#messageInput').on('input', function() {
        startTyping();
        
        // Para de digitar após 2 segundos de inatividade
        clearTimeout(typingTimeout);
        typingTimeout = setTimeout(stopTyping, 2000);
    });
    
    // Busca de conversas
    $('#searchConversations').on('input', function() {
        const searchTerm = $(this).val().toLowerCase();
        filterConversations(searchTerm);
    });
    
    // Nova conversa
    $('#newConversationBtn').on('click', function() {
        showNewConversationModal();
    });
    
    // Botões de anexo e emoji (placeholder)
    $('#attachmentBtn').on('click', function() {
        showNotification('Funcionalidade de anexos em desenvolvimento', 'info');
    });
    
    $('#emojiBtn').on('click', function() {
        showNotification('Seletor de emojis em desenvolvimento', 'info');
    });
    
    // Desconecta ao fechar página
    $(window).on('beforeunload', function() {
        disconnectWebSocket();
    });
}

// ==================== FUNÇÕES AUXILIARES ====================

/**
 * Filtra conversas por termo de busca
 */
function filterConversations(searchTerm) {
    $('.conversation-item').each(function() {
        const conversationName = $(this).find('.conversation-name').text().toLowerCase();
        const conversationPreview = $(this).find('.conversation-preview').text().toLowerCase();
        
        if (conversationName.includes(searchTerm) || conversationPreview.includes(searchTerm)) {
            $(this).show();
        } else {
            $(this).hide();
        }
    });
}

/**
 * Mostra área de chat ativa
 */
function showChatArea() {
    $('.empty-state').hide();
    $('.active-chat').show();
}

/**
 * Atualiza cabeçalho do chat
 */
function updateChatHeader(conversation) {
    $('.chat-contact-info h5').text(conversation.name);
    
    const statusText = conversation.type === 'individual' ? 
        (conversation.isOnline ? 'Online' : 'Offline') :
        `${conversation.participantCount} participantes`;
    
    $('.chat-contact-status').text(statusText);
    
    // Atualiza avatar
    const avatar = conversation.type === 'group' ? 
        `<div class="group-avatar">G</div>` :
        conversation.type === 'department' ?
        `<div class="department-avatar">D</div>` :
        `<img src="${conversation.avatar || '/img/default-avatar.png'}" alt="${conversation.name}" class="avatar-img">`;
    
    $('.chat-contact-avatar').html(avatar);
}

/**
 * Atualiza status de conexão
 */
function updateConnectionStatus(status) {
    $('.connection-status').text(status);
    
    // Atualiza cor baseada no status
    $('.connection-status').removeClass('bg-success bg-warning bg-danger')
        .addClass(
            status === 'Conectado' ? 'bg-success' :
            status === 'Conectando...' ? 'bg-warning' : 'bg-danger'
        );
}

/**
 * Atualiza status de usuário
 */
function updateUserStatus(statusData) {
    // Atualiza indicadores de status nas conversas
    $(`.conversation-item[data-user-id="${statusData.userId}"] .status-indicator`)
        .removeClass('status-online status-offline')
        .addClass(statusData.isOnline ? 'status-online' : 'status-offline');
}

/**
 * Atualiza badge de mensagens não lidas
 */
function updateUnreadBadge(conversationId) {
    // Recarrega conversas para atualizar badges
    loadConversations();
}

/**
 * Rola para o final da área de mensagens
 */
function scrollToBottom() {
    const messagesArea = $('.messages-area');
    messagesArea.scrollTop(messagesArea[0].scrollHeight);
}

/**
 * Formata timestamp para exibição
 */
function formatTime(timestamp) {
    if (!timestamp) return '';
    
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Agora';
    if (diffMins < 60) return `${diffMins}m`;
    if (diffHours < 24) return `${diffHours}h`;
    if (diffDays < 7) return `${diffDays}d`;
    
    return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
}

/**
 * Escapa HTML para prevenir XSS
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Mostra notificação
 */
function showNotification(message, type = 'info') {
    // Implementação básica - pode ser melhorada com biblioteca de notificações
    const alertClass = type === 'error' ? 'alert-danger' : 
                      type === 'success' ? 'alert-success' : 'alert-info';
    
    const notification = $(`
        <div class="alert ${alertClass} alert-dismissible fade show position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `);
    
    $('body').append(notification);
    
    // Remove automaticamente após 5 segundos
    setTimeout(() => {
        notification.alert('close');
    }, 5000);
}

/**
 * Mostra modal de nova conversa
 */
function showNewConversationModal() {
    // Implementação básica - pode ser expandida
    showNotification('Modal de nova conversa em desenvolvimento', 'info');
}

// ==================== FUNÇÕES PÚBLICAS ====================

/**
 * Função pública para enviar mensagem (pode ser chamada externamente)
 */
window.chatSendMessage = sendMessage;

/**
 * Função pública para conectar WebSocket (pode ser chamada externamente)
 */
window.chatConnect = connectWebSocket;

/**
 * Função pública para desconectar WebSocket (pode ser chamada externamente)
 */
window.chatDisconnect = disconnectWebSocket;

console.log('Chat.js carregado com sucesso!');
