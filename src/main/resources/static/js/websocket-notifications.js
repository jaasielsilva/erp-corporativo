if (typeof window.RealtimeNotifications === 'undefined') {
class RealtimeNotifications {
    constructor() {
        this.socket = null;
        this.stompClient = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000; // 3 segundos

        this.init();
    }

    init() {
        this.connect();
        this.setupEventListeners();
    }

    connect() {
        const userEmail = this.getCurrentUserEmail();

        this.socket = new SockJS('/ws');
        this.stompClient = Stomp.over(this.socket);

        const headers = {};
        this.stompClient.connect(headers, (frame) => {
            console.log('🔗 Conectado ao WebSocket:', frame);
            this.isConnected = true;
            this.reconnectAttempts = 0;

            // Sempre inscreve no tópico global
            this.subscribeToGlobalNotifications();

            // Só inscreve em filas específicas se houver usuário
            if (userEmail) {
                this.subscribeToUserNotifications(userEmail);
                this.subscribeToUnreadCount(userEmail);
            } else {
                console.info('Sem usuário autenticado, assinando apenas notificações globais.');
            }
        }, (error) => {
            console.error('Erro na conexão WebSocket:', error);
            this.isConnected = false;
            this.handleReconnect();
        });
    }

    subscribeToUserNotifications(userEmail) {
        this.stompClient.subscribe(`/queue/notifications/${userEmail}`, (message) => {
            const notification = JSON.parse(message.body);
            this.handleNewNotification(notification);
        });
    }

    subscribeToGlobalNotifications() {
        this.stompClient.subscribe('/topic/notifications', (message) => {
            const notification = JSON.parse(message.body);
            this.handleNewNotification(notification);
        });
    }

    subscribeToUnreadCount(userEmail) {
        this.stompClient.subscribe(`/queue/notifications/count/${userEmail}`, (message) => {
            const data = JSON.parse(message.body);
            this.updateUnreadCount(data.count);
        });
    }

    handleNewNotification(notification) {
        console.log("Nova notificação recebida:", notification);

        showToast(notification); // MOSTRA POPUP COM SOM

        if (typeof loadNotifications === "function") {
            loadNotifications(); // Atualiza lista
        }
    }

    updateUnreadCount(count) {
        const badge = document.querySelector('.badge.bg-danger');
        if (badge) {
            badge.textContent = count > 0 ? count : '';
            badge.style.display = count > 0 ? 'inline-block' : 'none';
        }
    }

    updateNotificationBadge() {
        fetch('/api/notifications/unread-count')
            .then(response => response.json())
            .then(data => this.updateUnreadCount(data.count))
            .catch(error => console.error('Erro ao atualizar contador:', error));
    }

    getPriorityClass(priority) {
        switch (priority) {
            case 'HIGH': return 'danger';
            case 'MEDIUM': return 'warning';
            case 'LOW': return 'info';
            default: return 'primary';
        }
    }

    getCurrentUserEmail() {
        const el = document.querySelector('[data-user-email]');
        if (el) return el.getAttribute('data-user-email');

        const storedEmail = localStorage.getItem('userEmail');
        if (storedEmail) return storedEmail;

        this.fetchCurrentUserEmail();
        return null;
    }

    fetchCurrentUserEmail() {
        fetch('/api/user/current')
            .then(res => res.json())
            .then(data => {
                if (data.email) {
                    localStorage.setItem('userEmail', data.email);
                    if (!this.isConnected) this.connect();
                }
            })
            .catch(err => console.error('Erro ao obter email do usuário:', err));
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Tentativa de reconexão ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            setTimeout(() => this.connect(), this.reconnectInterval);
        } else {
            console.error('Máximo de tentativas de reconexão atingido');
        }
    }

    setupEventListeners() {
        window.addEventListener('focus', () => { if (!this.isConnected) this.connect(); });
        window.addEventListener('beforeunload', () => { if (this.stompClient && this.isConnected) this.stompClient.disconnect(); });
    }

    disconnect() {
        if (this.stompClient && this.isConnected) {
            this.stompClient.disconnect();
            this.isConnected = false;
        }
    }
}
window.RealtimeNotifications = RealtimeNotifications;

// Função de toast com som embutido
if (typeof window.showToast === 'undefined') {
function showToast(notification) {
    const priorityClass = notification.priority ? `priority-${notification.priority.toLowerCase()}` : "priority-medium";
    const iconClass = notification.type === "chat" ? "fas fa-comments" : "fas fa-info-circle";

    const toast = document.createElement('div');
    toast.className = `notification-toast ${priorityClass}`;

    const icon = document.createElement('div');
    icon.className = 'toast-icon';
    const i = document.createElement('i');
    i.className = iconClass;
    icon.appendChild(i);

    const content = document.createElement('div');
    content.className = 'toast-content';
    const h4 = document.createElement('h4');
    h4.textContent = notification.title;
    const p = document.createElement('p');
    p.textContent = notification.message;
    content.appendChild(h4);
    content.appendChild(p);
    if (notification.actionUrl) {
        const actions = document.createElement('div');
        actions.className = 'notification-actions';
        const link = document.createElement('a');
        link.className = 'btn-text';
        link.href = notification.actionUrl;
        link.textContent = notification.actionText || 'Abrir';
        link.addEventListener('click', () => {
            toast.classList.remove('show');
            toast.classList.add('hide');
            setTimeout(() => toast.remove(), 300);
        });
        actions.appendChild(link);
        content.appendChild(actions);
    }

    const closeBtn = document.createElement('button');
    closeBtn.className = 'toast-close';
    closeBtn.textContent = '×';
    closeBtn.addEventListener('click', () => {
        toast.classList.remove('show');
        toast.classList.add('hide');
        setTimeout(() => {
            toast.remove();
        }, 300);
    });

    toast.appendChild(icon);
    toast.appendChild(content);
    toast.appendChild(closeBtn);
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
        if (window.__notifSoundEnabled) { playNotificationSound(); }
    }, 10);

    const baseDurations = { low: 4000, medium: 5000, high: 7000 };
    const prio = (notification.priority || 'medium').toLowerCase();
    const defaultDuration = baseDurations[prio] || 5000;
    const durationMs = typeof notification.durationMs === 'number' ? notification.durationMs : defaultDuration;
    const sticky = !!notification.sticky;

    if (!sticky && durationMs > 0) {
        setTimeout(() => {
            toast.classList.remove('show');
            toast.classList.add('hide');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, durationMs);
    }
}
window.showToast = showToast;
}

// Função de som nativo
function playNotificationSound() {
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();

        oscillator.type = "square";
        oscillator.frequency.setValueAtTime(440, audioCtx.currentTime);
        gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime);

        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);

        oscillator.start();
        oscillator.stop(audioCtx.currentTime + 0.15);
    } catch (err) {
        console.error("Erro ao tocar som:", err);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.__notifSoundEnabled = false;
    const enable = () => { window.__notifSoundEnabled = true; document.removeEventListener('click', enable); document.removeEventListener('keydown', enable); document.removeEventListener('touchstart', enable); };
    document.addEventListener('click', enable);
    document.addEventListener('keydown', enable);
    document.addEventListener('touchstart', enable);
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        if (!window.realtimeNotifications) {
            window.realtimeNotifications = new RealtimeNotifications();
        }
    } else {
        const sockjsScript = document.createElement('script');
        sockjsScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1.5.2/dist/sockjs.min.js';
        sockjsScript.onerror = () => {
            // Fallback alternativo
            const altSock = document.createElement('script');
            altSock.src = 'https://unpkg.com/sockjs-client@1.5.2/dist/sockjs.min.js';
            altSock.onload = loadStomp;
            document.head.appendChild(altSock);
        };
        sockjsScript.onload = loadStomp;
        document.head.appendChild(sockjsScript);

        function loadStomp() {
            const stompScript = document.createElement('script');
            stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
            stompScript.onerror = () => {
                const altStomp = document.createElement('script');
                altStomp.src = 'https://unpkg.com/stompjs@2.3.3/lib/stomp.min.js';
                altStomp.onload = () => { window.realtimeNotifications = new RealtimeNotifications(); };
                document.head.appendChild(altStomp);
            };
            stompScript.onload = () => { if (!window.realtimeNotifications) { window.realtimeNotifications = new RealtimeNotifications(); } };
            document.head.appendChild(stompScript);
        }
    }
})}
