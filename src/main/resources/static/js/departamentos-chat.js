// ===== Variáveis Globais =====
let stompClient = null;
let currentUser = null;
let selectedDepartment = null;
let departments = [];
let currentConversation = null;

// ===== Inicialização =====
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
});

function initializePage() {
    console.log('Inicializando página de conversas por departamento...');
    
    // Conectar WebSocket
    connectWebSocket();
    
    // Carregar dados do usuário
    loadCurrentUser();
    
    // Carregar departamentos
    loadDepartments();
    
    // Configurar event listeners
    setupEventListeners();
}

// ===== WebSocket Connection =====
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log('STOMP: ' + str),
        onConnect: onWebSocketConnected,
        onDisconnect: onWebSocketDisconnected,
        onStompError: onWebSocketError
    });
    
    stompClient.activate();
}

function onWebSocketConnected() {
    console.log('WebSocket conectado para departamentos');
    updateConnectionStatus('Conectado', 'success');
    
    // Subscrever a tópicos de departamentos quando conectado
    if (selectedDepartment) {
        subscribeToConversation(selectedDepartment.conversaId);
    }
}

function onWebSocketDisconnected() {
    console.log('WebSocket desconectado');
    updateConnectionStatus('Desconectado', 'danger');
}

function onWebSocketError(error) {
    console.error('Erro WebSocket:', error);
    updateConnectionStatus('Erro de conexão', 'danger');
}

function updateConnectionStatus(status, type) {
    const statusElement = document.getElementById('connectionStatus');
    statusElement.textContent = status;
    statusElement.className = `connection-status text-${type}`;
}

// ===== Carregar Dados do Usuário =====
async function loadCurrentUser() {
    try {
        const response = await fetch('/api/usuarios/me');
        if (response.ok) {
            currentUser = await response.json();
            console.log('Usuário atual:', currentUser);
        } else {
            console.error('Erro ao carregar dados do usuário');
        }
    } catch (error) {
        console.error('Erro ao carregar usuário:', error);
    }
}

// ===== Carregar Departamentos =====
async function loadDepartments() {
    try {
        showLoadingDepartments(true);
        
        const response = await fetch('/api/chat/departamentos');
        if (response.ok) {
            departments = await response.json();
            renderDepartments(departments);
        } else {
            console.error('Erro ao carregar departamentos');
            showEmptyDepartments();
        }
    } catch (error) {
        console.error('Erro ao carregar departamentos:', error);
        showEmptyDepartments();
    } finally {
        showLoadingDepartments(false);
    }
}

function renderDepartments(departmentsList) {
    const container = document.getElementById('departmentsList');
    
    if (!departmentsList || departmentsList.length === 0) {
        showEmptyDepartments();
        return;
    }
    
    const html = departmentsList.map(dept => `
        <div class="department-item" data-department-id="${dept.id}" onclick="selectDepartment(${dept.id})">
            <div class="department-icon">
                <i class="fas fa-${getDepartmentIcon(dept.nome)}"></i>
            </div>
            <div class="department-info">
                <div class="department-name">${dept.nome}</div>
                <div class="department-stats">
                    <span><i class="fas fa-users"></i> ${dept.participantes || 0}</span>
                    <span><i class="fas fa-comments"></i> ${dept.mensagens || 0}</span>
                </div>
            </div>
            ${dept.mensagensNaoLidas > 0 ? `<span class="department-badge">${dept.mensagensNaoLidas}</span>` : ''}
        </div>
    `).join('');
    
    container.innerHTML = html;
}

function getDepartmentIcon(departmentName) {
    const icons = {
        'TI': 'laptop-code',
        'Recursos Humanos': 'users',
        'RH': 'users',
        'Financeiro': 'chart-line',
        'Vendas': 'handshake',
        'Marketing': 'bullhorn',
        'Operações': 'cogs',
        'Jurídico': 'gavel',
        'Contabilidade': 'calculator',
        'Administrativo': 'clipboard-list',
        'Compras': 'shopping-cart',
        'Qualidade': 'award',
        'Logística': 'truck'
    };
    
    return icons[departmentName] || 'building';
}

function showLoadingDepartments(show) {
    const container = document.getElementById('departmentsList');
    if (show) {
        container.innerHTML = `
            <div class="loading-departments text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Carregando...</span>
                </div>
                <p class="mt-2 text-muted">Carregando departamentos...</p>
            </div>
        `;
    }
}

function showEmptyDepartments() {
    const container = document.getElementById('departmentsList');
    container.innerHTML = `
        <div class="empty-state">
            <i class="fas fa-building"></i>
            <h6>Nenhum departamento encontrado</h6>
            <p>Não há departamentos disponíveis no momento.</p>
        </div>
    `;
}

// ===== Seleção de Departamento =====
async function selectDepartment(departmentId) {
    const department = departments.find(d => d.id === departmentId);
    if (!department) return;
    
    // Atualizar UI
    updateSelectedDepartment(departmentId);
    selectedDepartment = department;
    
    // Mostrar área de chat
    showDepartmentChatArea(department);
    
    // Carregar ou criar conversa do departamento
    await loadDepartmentConversation(departmentId);
}

function updateSelectedDepartment(departmentId) {
    // Remover seleção anterior
    document.querySelectorAll('.department-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Adicionar seleção atual
    const selectedItem = document.querySelector(`[data-department-id="${departmentId}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
    }
}

function showDepartmentChatArea(department) {
    // Esconder estado inicial
    document.getElementById('noDepartmentSelected').style.display = 'none';
    
    // Mostrar área de chat
    const chatArea = document.getElementById('departmentChatArea');
    chatArea.classList.remove('d-none');
    
    // Atualizar header do departamento
    document.getElementById('departmentName').textContent = department.nome;
    document.getElementById('departmentStats').textContent = 
        `${department.participantes || 0} participantes • ${department.mensagens || 0} mensagens`;
}

// ===== Carregar Conversa do Departamento =====
async function loadDepartmentConversation(departmentId) {
    try {
        showLoadingMessages(true);
        
        // Buscar ou criar conversa do departamento
        const response = await fetch(`/api/chat/departamentos/${departmentId}/conversa`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            currentConversation = await response.json();
            console.log('Conversa do departamento:', currentConversation);
            
            // Subscrever à conversa
            subscribeToConversation(currentConversation.id);
            
            // Carregar mensagens
            await loadMessages(currentConversation.id);
        } else {
            console.error('Erro ao carregar conversa do departamento');
            showEmptyMessages();
        }
    } catch (error) {
        console.error('Erro ao carregar conversa:', error);
        showEmptyMessages();
    } finally {
        showLoadingMessages(false);
    }
}

// ===== Subscrição WebSocket =====
function subscribeToConversation(conversaId) {
    if (stompClient && stompClient.connected) {
        // Subscrever às mensagens da conversa
        stompClient.subscribe(`/topic/conversa.${conversaId}`, (message) => {
            const mensagem = JSON.parse(message.body);
            displayMessage(mensagem);
        });
        
        console.log(`Subscrito à conversa ${conversaId}`);
    }
}

// ===== Carregar e Exibir Mensagens =====
async function loadMessages(conversaId) {
    try {
        const response = await fetch(`/api/chat/conversas/${conversaId}/mensagens`);
        if (response.ok) {
            const text = await response.text();
            try {
                const messages = JSON.parse(text);
                renderMessages(messages);
            } catch (err) {
                console.error('Erro ao carregar mensagens: resposta não é JSON válido', {
                    erro: err,
                    amostra: text ? text.substring(0, 1000) : ''
                });
                showEmptyMessages();
            }
        } else {
            console.error('Erro ao carregar mensagens');
            showEmptyMessages();
        }
    } catch (error) {
        console.error('Erro ao carregar mensagens:', error);
        showEmptyMessages();
    }
}

function renderMessages(messages) {
    const container = document.getElementById('messagesList');
    
    if (!messages || messages.length === 0) {
        showEmptyMessages();
        return;
    }
    
    const html = messages.map(message => createMessageHTML(message)).join('');
    container.innerHTML = html;
    
    // Scroll para o final
    scrollToBottom();
}

function createMessageHTML(message) {
    const isOwnMessage = currentUser && message.remetente.id === currentUser.id;
    const messageClass = isOwnMessage ? 'message-item own-message' : 'message-item';
    const avatarText = message.remetente.nome.charAt(0).toUpperCase();
    
    return `
        <div class="${messageClass}">
            <div class="message-avatar">${avatarText}</div>
            <div class="message-content">
                <div class="message-header">
                    <span class="message-sender">${message.remetente.nome}</span>
                    <span class="message-time">${formatTime(message.timestamp)}</span>
                </div>
                <div class="message-body">
                    ${renderMessageContent(message)}
                </div>
            </div>
        </div>
    `;
}

function renderMessageContent(message) {
    switch (message.tipo) {
        case 'IMAGEM':
            return `
                <div class="message-image">
                    <img src="/api/chat/arquivos/${message.nomeArquivo}" 
                         alt="Imagem" onclick="openImageModal(this.src)">
                    ${message.conteudo ? `<p>${message.conteudo}</p>` : ''}
                </div>
            `;
        case 'ARQUIVO':
            return `
                <div class="message-file">
                    <div class="file-info">
                        <i class="fas fa-file file-icon"></i>
                        <div class="file-details">
                            <a href="/api/chat/arquivos/${message.nomeArquivo}" 
                               class="file-name" download="${message.nomeArquivo}">
                                ${message.nomeArquivo}
                            </a>
                            <span class="file-size">${formatFileSize(message.tamanhoArquivo)}</span>
                        </div>
                    </div>
                    ${message.conteudo ? `<p>${message.conteudo}</p>` : ''}
                </div>
            `;
        default:
            return `<p class="message-text">${message.conteudo}</p>`;
    }
}

function displayMessage(message) {
    const container = document.getElementById('messagesList');
    const messageHTML = createMessageHTML(message);
    container.insertAdjacentHTML('beforeend', messageHTML);
    scrollToBottom();
}

function showLoadingMessages(show) {
    const container = document.getElementById('messagesList');
    if (show) {
        container.innerHTML = `
            <div class="loading-messages">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Carregando mensagens...</span>
                </div>
                <p class="mt-2">Carregando mensagens...</p>
            </div>
        `;
    }
}

function showEmptyMessages() {
    const container = document.getElementById('messagesList');
    container.innerHTML = `
        <div class="empty-state">
            <i class="fas fa-comments"></i>
            <h6>Nenhuma mensagem ainda</h6>
            <p>Seja o primeiro a enviar uma mensagem neste departamento!</p>
        </div>
    `;
}

function scrollToBottom() {
    const container = document.getElementById('messagesContainer');
    container.scrollTop = container.scrollHeight;
}

// ===== Event Listeners =====
function setupEventListeners() {
    // Busca de departamentos
    const searchInput = document.getElementById('searchDepartments');
    searchInput.addEventListener('input', filterDepartments);
    
    // Envio de mensagem
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    
    sendBtn.addEventListener('click', sendMessage);
    
    // Anexos
    const attachmentBtn = document.getElementById('attachmentBtn');
    const fileInput = document.getElementById('fileInput');
    
    attachmentBtn.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', handleFileSelection);
    
    // Emoji (placeholder)
    const emojiBtn = document.getElementById('emojiBtn');
    emojiBtn.addEventListener('click', () => {
        console.log('Emoji picker - funcionalidade futura');
    });
}

// ===== Filtro de Departamentos =====
function filterDepartments() {
    const searchTerm = document.getElementById('searchDepartments').value.toLowerCase();
    const filteredDepartments = departments.filter(dept => 
        dept.nome.toLowerCase().includes(searchTerm)
    );
    renderDepartments(filteredDepartments);
}

// ===== Envio de Mensagens =====
async function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();
    
    if (!content || !currentConversation) return;
    
    try {
        const response = await fetch(`/api/chat/conversas/${currentConversation.id}/mensagens`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({ conteudo: content }).toString()
        });
        
        if (response.ok) {
            messageInput.value = '';
            // A mensagem será exibida via WebSocket
        } else {
            console.error('Erro ao enviar mensagem');
        }
    } catch (error) {
        console.error('Erro ao enviar mensagem:', error);
    }
}

// ===== Upload de Arquivos =====
function handleFileSelection(event) {
    const files = event.target.files;
    if (files.length > 0 && currentConversation) {
        Array.from(files).forEach(file => uploadFile(file));
    }
}

async function uploadFile(file) {
    if (!currentConversation) return;
    
    const formData = new FormData();
    formData.append('arquivo', file);
    formData.append('conteudo', ''); // Conteúdo opcional
    
    try {
        const response = await fetch(`/api/chat/conversas/${currentConversation.id}/mensagens/upload`, {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            console.log('Arquivo enviado com sucesso');
            // A mensagem será exibida via WebSocket
        } else {
            console.error('Erro ao enviar arquivo');
        }
    } catch (error) {
        console.error('Erro ao enviar arquivo:', error);
    }
}

// ===== Funções Auxiliares =====
function formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function openImageModal(imageSrc) {
    // Reutilizar a função do chat principal
    const modal = document.createElement('div');
    modal.className = 'image-modal';
    modal.innerHTML = `
        <div class="image-modal-content">
            <span class="image-modal-close">&times;</span>
            <img src="${imageSrc}" alt="Imagem ampliada">
        </div>
    `;
    
    document.body.appendChild(modal);
    
    const closeBtn = modal.querySelector('.image-modal-close');
    const closeModal = () => document.body.removeChild(modal);
    
    closeBtn.addEventListener('click', closeModal);
    modal.addEventListener('click', function(e) {
        if (e.target === modal) closeModal();
    });
    
    const handleKeydown = (e) => {
        if (e.key === 'Escape') {
            closeModal();
            document.removeEventListener('keydown', handleKeydown);
        }
    };
    document.addEventListener('keydown', handleKeydown);
}