if (typeof window.jQuery === 'undefined') {
    console && console.warn && console.warn('Notifications desabilitadas: jQuery não carregado.');
} else {
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
    let chatUnreadCount = 0;

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

        // Carregar notificações do sistema unificado
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
        // Carregar notificações legacy (compatibilidade)
        const legacyPromise = $.getJSON('/api/notifications/legacy/nao-lidas');

        Promise.all([systemPromise, legacyPromise]).then(([systemData, legacyData]) => {
            $list.empty();
            const systemRaw = Array.isArray(systemData.notifications) ? systemData.notifications : [];
            const legacyRaw = Array.isArray(legacyData) ? legacyData : [];

            const normalizedSystem = systemRaw.map(n => {
                const ts = new Date(n.timestamp);
                const safeTs = isNaN(ts.getTime()) ? null : ts;
                return {
                    id: n.id,
                    type: 'system',
                    title: n.title || 'Notificação',
                    message: n.message || '',
                    timestamp: safeTs,
                    remetenteNome: n.user && n.user.nome ? n.user.nome : 'Sistema',
                    actionUrl: n.actionUrl || null,
                    metadata: n.metadata || null,
                    read: !!n.isRead || !!n.read,
                    priority: (n.priority && typeof n.priority === 'string') ? n.priority.toLowerCase() : 'medium',
                    entityId: n.entityId || null
                };
            }).filter(n => n.title && n.message && n.timestamp);

            const normalizedLegacy = legacyRaw.map(n => {
                const ts = n.dataHora ? new Date(n.dataHora) : null;
                const safeTs = ts && !isNaN(ts.getTime()) ? ts : null;
                return {
                    id: n.id,
                    type: 'legacy',
                    title: n.titulo || 'Notificação',
                    message: n.mensagem || '',
                    timestamp: safeTs,
                    remetenteNome: 'Sistema',
                    actionUrl: null,
                    metadata: null,
                    read: !!n.lida,
                    priority: 'medium',
                    entityId: null
                };
            }).filter(n => n.title && n.message && n.timestamp);

            const byProtocol = new Map();
            const byId = new Map();
            normalizedSystem.forEach(n => {
                let meta = null;
                try { meta = n.metadata ? JSON.parse(n.metadata) : null; } catch {}
                const protocol = meta && meta.protocol ? meta.protocol : null;
                const jobId = meta && meta.jobId ? meta.jobId : null;
                const folhaId = meta && meta.folhaId ? meta.folhaId : null;
                const ref = (meta && meta.mes && meta.ano) ? `${meta.mes}-${meta.ano}` : null;
                // Preferir chave de payroll
                let key = null;
                if (jobId) key = `payroll:job:${jobId}`;
                else if (folhaId) key = `payroll:folha:${folhaId}`;
                else if (ref) key = `payroll:ref:${ref}`;
                else if (protocol) key = `${n.type}:${protocol}`;

                // Colapsar global vs usuário: preferir notificação com remetenteNome diferente de 'Sistema'
                const chooseNewer = (existing, cand) => {
                    if (!existing) return cand;
                    const exTs = existing.timestamp ? existing.timestamp.getTime() : 0;
                    const cTs = cand.timestamp ? cand.timestamp.getTime() : 0;
                    if (cTs > exTs) return cand;
                    // Se timestamps iguais/menores, preferir não-global
                    const exIsUser = existing.remetenteNome && existing.remetenteNome !== 'Sistema';
                    const cIsUser = cand.remetenteNome && cand.remetenteNome !== 'Sistema';
                    if (!exIsUser && cIsUser) return cand;
                    return existing;
                };

                if (key) {
                    const existing = byProtocol.get(key);
                    byProtocol.set(key, chooseNewer(existing, n));
                } else if (n.id != null) {
                    const existing = byId.get(n.id);
                    byId.set(n.id, chooseNewer(existing, n));
                } else {
                    const k = `${n.title}|${n.message}`;
                    const existing = byId.get(k);
                    byId.set(k, chooseNewer(existing, n));
                }
            });
            normalizedLegacy.forEach(n => {
                if (n.id != null) {
                    if (!byId.has(n.id)) byId.set(n.id, n);
                } else {
                    const key = `${n.title}|${n.message}`;
                    if (!byId.has(key)) byId.set(key, n);
                }
            });

            const finalNotifications = [];
            byProtocol.forEach(v => finalNotifications.push(v));
            byId.forEach(v => finalNotifications.push(v));
            finalNotifications.sort((a, b) => (b.timestamp ? b.timestamp.getTime() : 0) - (a.timestamp ? a.timestamp.getTime() : 0));

            if (finalNotifications.length === 0) {
                $list.append('<li class="no-notifications"><i class="fas fa-bell-slash"></i><p>Sem notificações</p></li>');
                updateBadges(0, 0);
                return;
            }

            finalNotifications.forEach(n => {
                const unreadClass = n.read ? '' : 'unread';
                const priorityClass = n.priority || 'medium';
                const typeClass = n.type;
                const icon = n.type === 'chat' ? 'fas fa-comments' : 'fas fa-info-circle';
                const subtitle = `De: ${n.remetenteNome || 'Sistema'}`;
                const timeText = (n.timestamp && !isNaN(n.timestamp.getTime())) ? n.timestamp.toLocaleString('pt-BR') : '';
                const actionLink = n.actionUrl ? `<a href="${n.actionUrl}" class="btn-text">Abrir</a>` : '';
                const $item = $(`
                    <li class="notification-item ${unreadClass} priority-${priorityClass} type-${typeClass}" data-id="${n.id}" data-type="${n.type}">
                        <div class="notification-icon"><i class="${icon}"></i></div>
                        <div class="notification-content">
                            <h4>${n.title}</h4>
                            ${subtitle ? `<p class="notification-subtitle">${subtitle}</p>` : ''}
                            <p>${n.message}</p>
                            <span class="notification-time">${timeText || ''}</span>
                            <div class="notification-actions">
                                <button class="mark-read-btn btn-text" data-id="${n.id}" data-type="${n.type}">Marcar como lida</button>
                                ${n.entityId ? `<a href="/rh/colaboradores/${n.entityId}" class="btn-text">Ir para detalhe</a>` : ''}
                                ${actionLink}
                                <button class="silence-btn btn-text" data-id="${n.id}" data-type="${n.type}">Silenciar</button>
                            </div>
                        </div>
                    </li>
                `);
                $list.append($item);
            });

            const totalUnread = finalNotifications.filter(n => !n.read).length;
            updateBadges(totalUnread, chatUnreadCount);
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
            // Para notificações de chat, usar endpoint legacy
            $.ajax({
                url: `/api/notifications/legacy/${id}/marcar-lida`,
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
                url: `/api/notifications/legacy/${id}/marcar-lida`,
                type: 'POST',
                success: function () {
                    loadNotifications(); // atualiza lista e badge
                }
            });
        }
    });

    window.addEventListener('chat:new-message', function () {
        chatUnreadCount += 1;
        const systemCount = parseInt($badges.text()) || 0;
        updateBadges(systemCount, chatUnreadCount);
    });

    window.addEventListener('chat:clear-unread', function () {
        chatUnreadCount = 0;
        const systemCount = parseInt($badges.text()) || 0;
        updateBadges(systemCount, chatUnreadCount);
    });

});
}
