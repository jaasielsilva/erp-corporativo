$(document).ready(function () {
    const $center = $('<div class="notification-center"></div>').appendTo('body');
    const $overlay = $('<div class="notification-overlay"></div>').appendTo('body');

    // Header da central
    $center.append(`
        <div class="notification-header">
            <h3><i class="fas fa-bell"></i> Notificações</h3>
            <div class="notification-actions">
                <button id="markAllReadBtn" class="btn-text">Marcar todas como lidas</button>
            </div>
        </div>
        <div class="notification-filters">
            <button class="filter-btn active" data-filter="all">Todas</button>
            <button class="filter-btn" data-filter="system">Sistema</button>
            <button class="filter-btn" data-filter="chat">Chat</button>
        </div>
        <ul class="notification-list"></ul>
    `);

    const $list = $center.find('.notification-list');
    const $badge = $('#notificationBadge');
    const $chatBadge = $('#chatBadge');
    let currentFilter = 'all';

    // Abrir/fechar central
    $(document).on('click', '#notificationTrigger', function (e) {
        e.preventDefault();
        e.stopPropagation();
        $center.toggleClass('show');
        $overlay.toggleClass('show');
        if ($center.hasClass('show')) loadNotifications();
    });

    // Fechar clicando no overlay
    $overlay.on('click', function () {
        $center.removeClass('show');
        $overlay.removeClass('show');
    });

    // Filtros de notificação
    $(document).on('click', '.filter-btn', function() {
        $('.filter-btn').removeClass('active');
        $(this).addClass('active');
        currentFilter = $(this).data('filter');
        loadNotifications();
    });

    // Função para carregar notificações
    function loadNotifications() {
        $list.html('<div class="notification-loading"><i class="fas fa-spinner"></i> Carregando...</div>');

        // Carregar notificações do sistema
        const systemPromise = $.getJSON('/api/notifications?filter=unread');
        // Carregar notificações de chat
        const chatPromise = $.getJSON('/api/notificacoes?apenasNaoLidas=true');

        Promise.all([systemPromise, chatPromise]).then(([systemData, chatData]) => {
            $list.empty();
            
            let allNotifications = [];
            let systemNotifications = systemData.notifications || [];
            let chatNotifications = chatData || [];

            // Processar notificações do sistema
            systemNotifications.forEach(n => {
                allNotifications.push({
                    ...n,
                    type: 'system',
                    timestamp: new Date(n.timestamp)
                });
            });

            // Processar notificações de chat
            chatNotifications.forEach(n => {
                allNotifications.push({
                    id: n.id,
                    title: n.titulo,
                    message: n.conteudo,
                    read: n.lida,
                    priority: n.prioridade?.toLowerCase() || 'medium',
                    type: 'chat',
                    timestamp: new Date(n.dataCriacao),
                    remetenteNome: n.remetenteNome
                });
            });

            // Filtrar notificações
            if (currentFilter !== 'all') {
                allNotifications = allNotifications.filter(n => n.type === currentFilter);
            }

            // Ordenar por timestamp
            allNotifications.sort((a, b) => b.timestamp - a.timestamp);

            if (allNotifications.length === 0) {
                $list.append('<li class="no-notifications"><i class="fas fa-bell-slash"></i><p>Sem notificações</p></li>');
                updateBadges(0, 0);
                return;
            }

            allNotifications.forEach(n => {
                const unreadClass = n.read ? '' : 'unread';
                const priorityClass = n.priority;
                const typeClass = n.type;
                const icon = n.type === 'chat' ? 'fas fa-comments' : 'fas fa-info-circle';
                const subtitle = n.type === 'chat' ? `De: ${n.remetenteNome}` : '';
                
                const $item = $(`
                    <li class="notification-item ${unreadClass} priority-${priorityClass} type-${typeClass}" data-id="${n.id}" data-type="${n.type}">
                        <div class="notification-icon"><i class="${icon}"></i></div>
                        <div class="notification-content">
                            <h4>${n.title}</h4>
                            ${subtitle ? `<p class="notification-subtitle">${subtitle}</p>` : ''}
                            <p>${n.message}</p>
                            <span class="notification-time">${n.timestamp.toLocaleString()}</span>
                            <button class="mark-read-btn btn-text">Marcar como lida</button>
                        </div>
                    </li>
                `);
                $list.append($item);
            });

            // Atualizar badges
            const unreadSystem = systemNotifications.filter(n => !n.read).length;
            const unreadChat = chatNotifications.filter(n => !n.lida).length;
            updateBadges(unreadSystem, unreadChat);
        }).catch(error => {
            console.error('Erro ao carregar notificações:', error);
            $list.html('<div class="notification-error"><i class="fas fa-exclamation-triangle"></i> Erro ao carregar notificações</div>');
        });
    }

    // Função para atualizar badges
    function updateBadges(systemCount, chatCount) {
        if (systemCount > 0) {
            $badge.text(systemCount).show();
        } else {
            $badge.hide();
        }
        
        if (chatCount > 0) {
            $chatBadge.text(chatCount).show();
        } else {
            $chatBadge.hide();
        }
    }

    // Marcar todas como lidas
    $(document).on('click', '#markAllReadBtn', function () {
        $.ajax({
            url: '/api/notifications/mark-all-read',
            type: 'PUT',
            success: function () {
                loadNotifications(); // atualiza lista e badge
            }
        });
    });

    // Marcar individual como lida pelo botão
    $(document).on('click', '.mark-read-btn', function (e) {
        e.stopPropagation();
        const $item = $(this).closest('.notification-item');
        const id = $item.data('id');
        const type = $item.data('type');

        const url = type === 'chat' ? 
            `/api/notificacoes/${id}/marcar-lida` : 
            `/api/notifications/${id}/mark-read`;

        $.ajax({
            url: url,
            type: 'PUT',
            success: function () {
                $item.removeClass('unread');
                loadNotifications();
            }
        });
    });

    // Marcar individual clicando no card
    $(document).on('click', '.notification-item', function () {
        const $item = $(this);
        const id = $item.data('id');
        const type = $item.data('type');

        if ($item.hasClass('unread')) {
            const url = type === 'chat' ? 
                `/api/notificacoes/${id}/marcar-lida` : 
                `/api/notifications/${id}/mark-read`;

            $.ajax({
                url: url,
                type: 'PUT',
                success: function () {
                    $item.removeClass('unread');
                    loadNotifications();
                }
            });
        }
        
        // Se for notificação de chat, redirecionar para o chat
        if (type === 'chat') {
            window.location.href = '/chat';
        }
    });

    // Inicializa badges de notificações não lidas
    function initializeBadges() {
        const systemPromise = $.getJSON('/api/notifications/unread-count');
        const chatPromise = $.getJSON('/api/notificacoes/count');
        
        Promise.all([systemPromise, chatPromise]).then(([systemData, chatCount]) => {
            const systemCount = systemData.unreadCount || 0;
            updateBadges(systemCount, chatCount);
        }).catch(error => {
            console.error('Erro ao inicializar badges:', error);
        });
    }
    
    initializeBadges();
    
    // Atualizar badges periodicamente
    setInterval(initializeBadges, 30000); // A cada 30 segundos
});
