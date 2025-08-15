// Sistema de Notificações Avançado

class NotificationSystem {
    constructor() {
        this.notifications = [];
        this.baseUrl = '/api/notifications';
        this.unreadCount = 0;
        this.isConnected = false;
        this.userProfile = null;
        
        // Configurações de performance
        this.maxNotifications = 100;
        this.maxAge = 7 * 24 * 60 * 60 * 1000; // 7 dias em millisegundos
        this.pageSize = 5; // Notificações por página (6ª notificação cria nova página)
        this.currentPage = 1;
        
        this.init();
    }

    async init() {
        this.setupApiConnection();
        this.createNotificationCenter();
        this.setupEventListeners();
        await this.loadInitialNotifications();
        this.startPeriodicUpdates();
    }

    // Configuração de conexão com API
    setupApiConnection() {
        setTimeout(() => {
            this.isConnected = true;
            this.updateConnectionStatus(true);
        }, 1000);
    }

    // Método removido - não precisamos mais de simulação
    simulateIncomingNotifications() {
        // Removido - agora usamos dados reais da API
    }

    receiveNotification(notificationData) {
        if (this.isRelevantForCurrentProfile(notificationData)) {
            const notification = {
                id: Date.now() + Math.random(),
                ...notificationData,
                timestamp: new Date(),
                read: false
            };
            
            this.notifications.unshift(notification);
            this.unreadCount++;
            
            // Aplica limpeza automática
            this.cleanupNotifications();
            
            this.updateBadges();
            this.updateNotificationCenter();
            this.showToast(notification);
            this.playNotificationSound(notification.priority);
            
            // Nova notificação recebida
        }
    }

    // Adiciona uma nova notificação
    addNotification(notification) {
        // Verifica limite máximo para performance
        if (this.notifications.length >= this.maxNotifications) {
            this.cleanupNotifications();
        }
        
        // Adiciona timestamp se não existir
        if (!notification.timestamp) {
            notification.timestamp = new Date();
        }
        
        // Adiciona no início da lista (mais recente primeiro)
        this.notifications.unshift(notification);
        
        // Atualiza contador se não foi lida
        if (!notification.read) {
            this.unreadCount++;
        }
        
        this.updateBadges();
        this.updateNotificationCenter();
        
        // Mostra toast e toca som para notificações não lidas
        if (!notification.read) {
            this.showToast(notification);
            this.playNotificationSound(notification.priority);
        }
        
        // Nova notificação adicionada
    }
    
    // Cria uma nova notificação via API
    async createNotification(type, title, message, priority = 'medium') {
        try {
            const response = await fetch(this.baseUrl, {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    type: type,
                    title: title,
                    message: message,
                    priority: priority
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.success && data.notification) {
                    // Adicionar ao cache local
                    this.addNotification(data.notification);
                    // Notificação criada com sucesso
                    return data.notification;
                }
            }
            return null;
        } catch (error) {
            console.error('Erro ao criar notificação:', error);
            return null;
        }
    }

    isRelevantForCurrentProfile(notification) {
        const currentProfile = this.getCurrentProfile();
        
        const isRelevant = notification.relevantProfiles.includes('all') || 
                          notification.relevantProfiles.includes(currentProfile);
        
        return isRelevant;
    }

    cleanupNotifications() {
        const now = Date.now();
        const initialCount = this.notifications.length;
        
        // Remove notificações antigas (mais de 7 dias)
        this.notifications = this.notifications.filter(notification => {
            const age = now - new Date(notification.timestamp).getTime();
            return age <= this.maxAge;
        });
        
        // Aplica limite máximo de notificações
        if (this.notifications.length > this.maxNotifications) {
            const removedNotifications = this.notifications.splice(this.maxNotifications);
            // Ajusta contador de não lidas
            const removedUnread = removedNotifications.filter(n => !n.read).length;
            this.unreadCount = Math.max(0, this.unreadCount - removedUnread);
        }
        
        // Recalcula contador de não lidas para garantir consistência
        this.unreadCount = this.notifications.filter(n => !n.read).length;
        
        const finalCount = this.notifications.length;
        // Limpeza automática executada se necessário
    }

    getCurrentProfile() {
        // Detecta o perfil baseado na URL ou elemento da página
        if (window.location.pathname.includes('estagiario') || 
            document.querySelector('.profile-estagiario')) {
            return 'estagiario';
        } else if (window.location.pathname.includes('gerente') || 
                   document.querySelector('.profile-gerente')) {
            return 'gerente';
        } else {
            return 'funcionario';
        }
    }

    async loadInitialNotifications() {
        try {
            const response = await fetch(`${this.baseUrl}?page=0&size=${this.pageSize}`, {
                method: 'GET',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                
                // Mapeia as notificações do formato da API para o formato esperado
                this.notifications = (data.notifications || []).map(notification => ({
                    id: notification.id,
                    type: notification.type || 'info',
                    title: notification.title,
                    message: notification.message,
                    icon: this.getIconForType(notification.type),
                    priority: notification.priority ? notification.priority.toLowerCase() : 'medium',
                    timestamp: new Date(notification.timestamp),
                    read: notification.read,
                    relevantProfiles: ['all'] // Por enquanto, todas as notificações são relevantes
                }));
                
                this.unreadCount = this.notifications.filter(n => !n.read).length;
                
                // Também busca o contador de não lidas da API
                const unreadResponse = await fetch(`${this.baseUrl}/unread-count`, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                if (unreadResponse.ok) {
                    const unreadData = await unreadResponse.json();
                    this.unreadCount = unreadData.unreadCount || this.unreadCount;
                } else {
                    console.warn('Erro ao buscar contador de notificações:', unreadResponse.status, unreadResponse.statusText);
                }
            } else {
                console.warn('Erro ao carregar notificações:', response.status, response.statusText);
                // Erro na API, usando dados de fallback
                this.notifications = [];
                this.unreadCount = 0;
            }
        } catch (error) {
            console.error('❌ Erro ao carregar notificações iniciais:', error);
            this.notifications = [];
            this.unreadCount = 0;
        }
        
        // Notificações carregadas e contador atualizado
        
        this.updateBadges();
    }
    
    getIconForType(type) {
        const iconMap = {
            'user_created_by_admin': 'fas fa-user-plus',
            'user_account_created': 'fas fa-user-check',
            'nova_solicitacao': 'fas fa-file-alt',
            'aprovacao': 'fas fa-check-circle',
            'rejeicao': 'fas fa-times-circle',
            'sistema': 'fas fa-cog',
            'info': 'fas fa-info-circle'
        };
        return iconMap[type] || 'fas fa-bell';
    }

    createNotificationCenter() {
        
        const notificationHTML = `
            <div id="notification-center" class="notification-center hidden">
                <div class="notification-header">
                    <h3><i class="fas fa-bell"></i> Central de Notificações</h3>
                    <div class="notification-actions">
                        <button id="mark-all-read" class="btn-text">Marcar todas como lidas</button>
                        <button id="close-notifications" class="btn-icon"><i class="fas fa-times"></i></button>
                    </div>
                </div>
                <div class="notification-filters">
                    <button class="filter-btn active" data-filter="all">Todas</button>
                    <button class="filter-btn" data-filter="unread">Não lidas</button>
                    <button class="filter-btn" data-filter="high">Urgentes</button>
                </div>
                <div class="notification-stats">
                    <span id="notification-count">0 notificações</span>
                    <span id="page-info">Página 1 de 1</span>
                </div>
                <div id="notification-list" class="notification-list">
                    <!-- Notificações serão inseridas aqui -->
                </div>
                <div class="notification-pagination">
                    <button id="prev-page" class="pagination-btn" disabled>
                        <i class="fas fa-chevron-left"></i> Anterior
                    </button>
                    <span id="pagination-info">1 / 1</span>
                    <button id="next-page" class="pagination-btn" disabled>
                        Próxima <i class="fas fa-chevron-right"></i>
                    </button>
                </div>
                <div class="notification-footer">
                    <div class="connection-status">
                        <span id="connection-indicator" class="status-dot"></span>
                        <span id="connection-text">Conectando...</span>
                    </div>
                </div>
            </div>
            <div id="notification-overlay" class="notification-overlay hidden"></div>
        `;

        document.body.insertAdjacentHTML('beforeend', notificationHTML);
        
        // Verificar se os elementos foram criados
        const center = document.getElementById('notification-center');
        const overlay = document.getElementById('notification-overlay');
        
        if (!center || !overlay) {
            console.error('❌ Erro ao criar elementos da central!');
        }
    }

    setupEventListeners() {
        // Toggle da central de notificações
        const notificationTrigger = document.querySelector('.notification-trigger');
        
        if (notificationTrigger) {
            notificationTrigger.addEventListener('click', (e) => {
                e.preventDefault();
                this.toggleNotificationCenter();
            });
        } else {
            console.error('❌ Elemento .notification-trigger não encontrado!');
        }

        // Fechar central
        document.getElementById('close-notifications').addEventListener('click', () => {
            this.hideNotificationCenter();
        });

        // Overlay para fechar
        document.getElementById('notification-overlay').addEventListener('click', () => {
            this.hideNotificationCenter();
        });

        // Marcar todas como lidas
        document.getElementById('mark-all-read').addEventListener('click', () => {
            this.markAllAsRead();
        });

        // Filtros
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.filterNotifications(btn.dataset.filter);
            });
        });

        // ESC para fechar
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideNotificationCenter();
            }
        });

        // Paginação
        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        
        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    this.updateNotificationCenter();
                }
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                const totalPages = this.getTotalPages();
                if (this.currentPage < totalPages) {
                    this.currentPage++;
                    this.updateNotificationCenter();
                }
            });
        }
    }

    toggleNotificationCenter() {
        const center = document.getElementById('notification-center');
        
        if (!center) {
            console.error('❌ Elemento notification-center não encontrado!');
            return;
        }
        
        if (center.classList.contains('hidden')) {
            this.showNotificationCenter();
        } else {
            this.hideNotificationCenter();
        }
    }

    showNotificationCenter() {
        const center = document.getElementById('notification-center');
        const overlay = document.getElementById('notification-overlay');
        
        if (!center || !overlay) {
            console.error('❌ Elementos da central não encontrados!');
            return;
        }
        
        center.classList.remove('hidden');
        overlay.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
        
        this.updateNotificationCenter();
        
        // Animação de entrada
        setTimeout(() => {
            center.classList.add('show');
            overlay.classList.add('show');
        }, 10);
    }

    hideNotificationCenter() {
        const center = document.getElementById('notification-center');
        const overlay = document.getElementById('notification-overlay');
        
        if (center && overlay) {
            center.classList.remove('show');
            overlay.classList.remove('show');
            
            setTimeout(() => {
                center.classList.add('hidden');
                overlay.classList.add('hidden');
                document.body.style.overflow = '';
            }, 300);
        }
    }

    getTotalPages(filteredNotifications = null) {
        const notifications = filteredNotifications || this.getFilteredNotifications();
        return Math.ceil(notifications.length / this.pageSize) || 1;
    }

    getFilteredNotifications() {
        const activeFilter = document.querySelector('.filter-btn.active')?.dataset.filter || 'all';
        
        if (activeFilter === 'unread') {
            return this.notifications.filter(n => !n.read);
        } else if (activeFilter === 'high') {
            return this.notifications.filter(n => n.priority === 'high');
        }
        
        return this.notifications;
    }

    getPaginatedNotifications(filteredNotifications) {
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        return filteredNotifications.slice(startIndex, endIndex);
    }

    updateNotificationCenter() {
        const list = document.getElementById('notification-list');
        const filteredNotifications = this.getFilteredNotifications();
        const totalPages = this.getTotalPages(filteredNotifications);
        
        // Ajusta página atual se necessário
        if (this.currentPage > totalPages) {
            this.currentPage = Math.max(1, totalPages);
        }
        
        // Atualiza estatísticas
        this.updateNotificationStats(filteredNotifications.length, totalPages);
        
        // Atualiza controles de paginação
        this.updatePaginationControls(totalPages);

        if (filteredNotifications.length === 0) {
            list.innerHTML = '<div class="no-notifications"><i class="fas fa-bell-slash"></i><p>Nenhuma notificação encontrada</p></div>';
            return;
        }

        const paginatedNotifications = this.getPaginatedNotifications(filteredNotifications);
        
        list.innerHTML = paginatedNotifications.map(notification => `
            <div class="notification-item ${notification.read ? 'read' : 'unread'} priority-${notification.priority}" data-id="${notification.id}">
                <div class="notification-icon">
                    <i class="${notification.icon}"></i>
                </div>
                <div class="notification-content">
                    <h4>${notification.title}</h4>
                    <p>${notification.message}</p>
                    <span class="notification-time">${this.formatTime(notification.timestamp)}</span>
                </div>
                <div class="notification-actions">
                    ${!notification.read ? '<button class="mark-read-btn" title="Marcar como lida"><i class="fas fa-check"></i></button>' : ''}
                    <button class="delete-notification-btn" title="Remover"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `).join('');

        // Event listeners para ações das notificações
        list.querySelectorAll('.mark-read-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = Number(e.target.closest('.notification-item').dataset.id);
                this.markAsRead(id);
            });
        });

        list.querySelectorAll('.delete-notification-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = Number(e.target.closest('.notification-item').dataset.id);
                this.deleteNotification(id);
            });
        });

        list.querySelectorAll('.notification-item').forEach(item => {
            item.addEventListener('click', () => {
                const id = Number(item.dataset.id);
                this.markAsRead(id);
            });
        });
    }

    updateNotificationStats(totalCount, totalPages) {
        const countElement = document.getElementById('notification-count');
        const pageInfoElement = document.getElementById('page-info');
        
        if (countElement) {
            countElement.textContent = `${totalCount} notificação${totalCount !== 1 ? 'ões' : ''}`;
        }
        if (pageInfoElement) {
            pageInfoElement.textContent = `Página ${this.currentPage} de ${totalPages}`;
        }
    }

    updatePaginationControls(totalPages) {
        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        const paginationInfo = document.getElementById('pagination-info');
        
        if (prevBtn) {
            prevBtn.disabled = this.currentPage <= 1;
        }
        if (nextBtn) {
            nextBtn.disabled = this.currentPage >= totalPages;
        }
        if (paginationInfo) {
            paginationInfo.textContent = `${this.currentPage} / ${totalPages}`;
        }
        
        // Oculta paginação se houver apenas uma página
        const paginationContainer = document.querySelector('.notification-pagination');
        if (paginationContainer) {
            if (totalPages <= 1) {
                paginationContainer.style.display = 'none';
            } else {
                paginationContainer.style.display = 'flex';
            }
        }
    }

    filterNotifications(filter) {
        this.currentPage = 1; // Reset para primeira página ao filtrar
        this.updateNotificationCenter();
    }

    async markAsRead(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}/mark-read`, {
                method: 'PUT',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    // Atualizar cache local
                    const notification = this.notifications.find(n => n.id === id);
                    if (notification && !notification.read) {
                        notification.read = true;
                        this.unreadCount = Math.max(0, this.unreadCount - 1);
                        this.updateBadges();
                        this.updateNotificationCenter();
                    }
                    // Notificação marcada como lida
                    return true;
                }
            }
            return false;
        } catch (error) {
            console.error('Erro ao marcar notificação como lida:', error);
            return false;
        }
    }

    async markAllAsRead() {
        try {
            const response = await fetch(`${this.baseUrl}/mark-all-read`, {
                method: 'PUT',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    // Atualizar cache local
                    this.notifications.forEach(n => n.read = true);
                    this.unreadCount = 0;
                    this.updateBadges();
                    this.updateNotificationCenter();
                    // Notificações marcadas como lidas
                    return data.markedCount || this.notifications.length;
                }
            }
            return 0;
        } catch (error) {
            console.error('Erro ao marcar todas as notificações como lidas:', error);
            return 0;
        }
    }

    async deleteNotification(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`, {
                method: 'DELETE',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    // Atualizar cache local
                    const index = this.notifications.findIndex(n => n.id === id);
                    if (index !== -1) {
                        const notification = this.notifications[index];
                        if (!notification.read) {
                            this.unreadCount = Math.max(0, this.unreadCount - 1);
                        }
                        this.notifications.splice(index, 1);
                        this.updateBadges();
                        this.updateNotificationCenter();
                    }
                    // Notificação removida
                    return true;
                }
            }
            return false;
        } catch (error) {
            console.error('Erro ao remover notificação:', error);
            return false;
        }
    }

    updateBadges() {
        const badge = document.querySelector('.notification-badge');
        const bellIcon = document.querySelector('.notification-trigger');
        
        if (badge && this.unreadCount > 0) {
            badge.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
            badge.style.display = 'block';
            if (bellIcon) {
                bellIcon.classList.add('has-notifications');
                
                // Animação do sininho
                bellIcon.classList.add('shake');
                setTimeout(() => {
                    bellIcon.classList.remove('shake');
                }, 1000);
            }
        } else if (badge) {
            badge.style.display = 'none';
            if (bellIcon) {
                bellIcon.classList.remove('has-notifications');
            }
        }
    }

    updateConnectionStatus(connected) {
        const indicator = document.getElementById('connection-indicator');
        const text = document.getElementById('connection-text');
        
        if (connected) {
            indicator.className = 'status-dot connected';
            text.textContent = 'Conectado';
        } else {
            indicator.className = 'status-dot disconnected';
            text.textContent = 'Desconectado';
        }
    }

    showToast(notification) {
        const toast = document.createElement('div');
        toast.className = `notification-toast priority-${notification.priority}`;
        toast.innerHTML = `
            <div class="toast-icon">
                <i class="${notification.icon}"></i>
            </div>
            <div class="toast-content">
                <h4>${notification.title}</h4>
                <p>${notification.message}</p>
            </div>
            <button class="toast-close">
                <i class="fas fa-times"></i>
            </button>
        `;

        document.body.appendChild(toast);

        // Animação de entrada
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

        // Event listener para fechar
        toast.querySelector('.toast-close').addEventListener('click', () => {
            this.hideToast(toast);
        });

        // Auto-hide baseado na prioridade
        const autoHideDelay = {
            'low': 3000,
            'medium': 5000,
            'high': 8000
        };

        setTimeout(() => {
            this.hideToast(toast);
        }, autoHideDelay[notification.priority] || 5000);
    }

    async playNotificationSound(priority) {
        try {
            // Verifica se o áudio está disponível
            if (!window.AudioContext && !window.webkitAudioContext) {
                return;
            }
            
            // Cria contexto de áudio
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            
            // Verifica se o contexto precisa ser desbloqueado (política de autoplay)
            if (audioContext.state === 'suspended') {
                await audioContext.resume();
            }
            
            // Frequências diferentes para cada prioridade
            const frequencies = {
                'low': [400, 300],
                'medium': [600, 400],
                'high': [800, 600, 400]
            };
            
            const freqs = frequencies[priority] || frequencies['medium'];
            
            // Tocando som de notificação
            
            freqs.forEach((freq, index) => {
                setTimeout(() => {
                    try {
                        const oscillator = audioContext.createOscillator();
                        const gainNode = audioContext.createGain();
                        
                        oscillator.connect(gainNode);
                        gainNode.connect(audioContext.destination);
                        
                        oscillator.frequency.setValueAtTime(freq, audioContext.currentTime);
                        oscillator.type = 'sine';
                        
                        gainNode.gain.setValueAtTime(0, audioContext.currentTime);
                        gainNode.gain.linearRampToValueAtTime(0.1, audioContext.currentTime + 0.01);
                        gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.3);
                        
                        oscillator.start(audioContext.currentTime);
                        oscillator.stop(audioContext.currentTime + 0.3);
                    } catch (error) {
                        // Erro ao tocar frequência
                    }
                }, index * 150);
            });
        } catch (error) {
            // Erro ao tocar som de notificação, usando fallback
            this.playFallbackSound();
        }
    }
    
    playFallbackSound() {
        try {
            // Cria um beep simples como fallback
            const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIG2m98OScTgwOUarm7blmGgU7k9n1unEiBC13yO/eizEIHWq+8+OWT');
            audio.volume = 0.1;
            audio.play().catch(() => {
                // Não foi possível tocar o som de notificação
            });
        } catch (error) {
            // Fallback de som também falhou
        }
    }

    hideToast(toast) {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }

    formatTime(timestamp) {
        const now = new Date();
        const time = new Date(timestamp);
        const diff = now - time;
        
        if (diff < 60000) { // menos de 1 minuto
            return 'Agora';
        } else if (diff < 3600000) { // menos de 1 hora
            const minutes = Math.floor(diff / 60000);
            return `${minutes}min atrás`;
        } else if (diff < 86400000) { // menos de 1 dia
            const hours = Math.floor(diff / 3600000);
            return `${hours}h atrás`;
        } else {
            return time.toLocaleDateString('pt-BR');
        }
    }

    startPeriodicUpdates() {
        setInterval(async () => {
            await this.loadInitialNotifications();
            this.updateNotificationCenter();
        }, 30000); // Atualiza a cada 30 segundos
    }
}

// Inicialização
document.addEventListener('DOMContentLoaded', async () => {
    try {
        window.notificationSystem = new NotificationSystem();
    } catch (error) {
        console.error('❌ Erro ao inicializar sistema de notificações:', error);
    }
});