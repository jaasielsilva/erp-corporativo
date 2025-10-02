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
            <button class="filter-btn" data-filter="unread">Não lidas</button>
            <button class="filter-btn" data-filter="hr">RH</button>
            <button class="filter-btn" data-filter="high">Prioridade Alta</button>
            <button class="filter-btn" data-filter="recent">Últimas 24h</button>
        </div>
        <ul class="notification-list"></ul>
    `);

    const $list = $center.find('.notification-list');
    const $badges = $('.notification-badge');
    const $chatBadges = $('.chat-badge');
    let currentFilter = 'all';

    // Abrir/fechar central
    $(document).on('click', '.notification-trigger', function (e) {
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
        let apiUrl = '/api/notifications';
        const params = new URLSearchParams();
        
        if (currentFilter === 'unread') {
            params.set('filter', 'unread');
        } else if (currentFilter === 'hr') {
            params.set('category', 'hr_admission');
        } else if (currentFilter === 'high') {
            params.set('priority', 'high');
        } else if (currentFilter === 'recent') {
            params.set('filter', 'recent');
        }
        
        if (params.toString()) {
            apiUrl += '?' + params.toString();
        }

        const systemPromise = $.getJSON(apiUrl);
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
                            <div class="notification-actions">
                                <button class="mark-read-btn btn-text" data-id="${n.id}" data-type="${n.type}">Marcar como lida</button>
                                ${n.entityId ? `<a href="/rh/colaboradores/${n.entityId}" class="btn-text">Ir para detalhe</a>` : ''}
                                <button class="silence-btn btn-text" data-id="${n.id}" data-type="${n.type}">Silenciar</button>
                            </div>
                        </div>
                    </li>
                `);
                $list.append($item);
            });

            // Atualizar badges
            updateBadges(systemData.unreadCount || 0, chatData.filter(n => !n.lida).length);
        }).catch(error => {
            console.error('Erro ao carregar notificações:', error);
            $list.html('<div class="notification-error"><i class="fas fa-exclamation-triangle"></i> Erro ao carregar notificações</div>');
        });
    }

    // Função para reproduzir som suave de notificação
    function playNotificationSound() {
        try {
            // Criar contexto de áudio
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            const audioCtx = new AudioContext();
            
            // Criar oscilador para gerar o som
            const oscillator = audioCtx.createOscillator();
            const gainNode = audioCtx.createGain();
            
            // Configurar o som suave (frequência baixa e volume reduzido)
            oscillator.type = 'sine';
            oscillator.frequency.setValueAtTime(880, audioCtx.currentTime); // Nota A5
            
            // Configurar o volume (bem baixo para ser suave)
            gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.5);
            
            // Conectar os nós
            oscillator.connect(gainNode);
            gainNode.connect(audioCtx.destination);
            
            // Tocar o som por 500ms
            oscillator.start();
            oscillator.stop(audioCtx.currentTime + 0.5);
        } catch (error) {
            console.log('Não foi possível reproduzir o som de notificação:', error);
        }
    }

    // Função para atualizar badges
    function updateBadges(systemCount, chatCount) {
        // Verificar se há notificações não lidas (novas)
        const totalUnread = systemCount + chatCount;
        const hadUnreadBefore = ($badges.filter(':visible').length > 0) || ($chatBadges.filter(':visible').length > 0);
        
        if (systemCount > 0) {
            $badges.text(systemCount).show();
        } else {
            $badges.hide();
        }
        
        if (chatCount > 0) {
            $chatBadges.text(chatCount).show();
        } else {
            $chatBadges.hide();
        }
        
        // Tocar som apenas se há novas notificações não lidas
        const hasUnreadNow = systemCount > 0 || chatCount > 0;
        if (hasUnreadNow && !hadUnreadBefore) {
            playNotificationSound();
        }
    }

    // Marcar notificação como lida
    $(document).on('click', '.mark-read-btn', function (e) {
        e.stopPropagation();
        const id = $(this).data('id');
        const type = $(this).data('type');
        
        if (type === 'system') {
            $.ajax({
                url: `/api/notifications/${id}/mark-read`,
                type: 'PUT',
                success: function () {
                    loadNotifications(); // atualiza lista e badge
                }
            });
        } else {
            // Para notificações de chat, usar o endpoint existente
            $.ajax({
                url: `/api/notificacoes/${id}/marcar-lida`,
                type: 'POST',
                success: function () {
                    loadNotifications(); // atualiza lista e badge
                }
            });
        }
    });

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

    // Silenciar notificação
    $(document).on('click', '.silence-btn', function (e) {
        e.stopPropagation();
        const id = $(this).data('id');
        const type = $(this).data('type');
        
        if (type === 'system') {
            $.ajax({
                url: `/api/notifications/${id}`,
                type: 'DELETE',
                success: function () {
                    loadNotifications(); // atualiza lista e badge
                }
            });
        } else {
            // Para notificações de chat, marcar como lida (não há silenciar)
            $.ajax({
                url: `/api/notificacoes/${id}/marcar-lida`,
                type: 'POST',
                success: function () {
                    loadNotifications(); // atualiza lista e badge
                }
            });
        }
    });

});