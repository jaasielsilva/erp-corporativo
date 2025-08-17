$(document).ready(function() {
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
        <ul class="notification-list"></ul>
    `);

    const $list = $center.find('.notification-list');
    const $badge = $('#notificationBadge');

    // Abrir/fechar central - usando delegação para cobrir qualquer clique
    $(document).on('click', '#notificationTrigger', function(e){
        e.preventDefault();
        e.stopPropagation(); // evita conflitos
        $center.toggleClass('show');
        $overlay.toggleClass('show');
        if ($center.hasClass('show')) loadNotifications();
    });

    // Fechar clicando no overlay
    $overlay.on('click', function(){
        $center.removeClass('show');
        $overlay.removeClass('show');
    });

    // Buscar notificações
    function loadNotifications(){
        $list.html('<div class="notification-loading"><i class="fas fa-spinner"></i> Carregando...</div>');
        $.getJSON('/api/notifications?filter=unread', function(data){
            $list.empty();
            if(data.notifications.length === 0){
                $list.append('<li class="no-notifications"><i class="fas fa-bell-slash"></i><p>Sem notificações</p></li>');
                $badge.hide();
                return;
            }
            data.notifications.forEach(n => {
                const unreadClass = n.read ? '' : 'unread';
                const priorityClass = n.priority.toLowerCase();
                const $item = $(`
                    <li class="notification-item ${unreadClass} priority-${priorityClass}">
                        <div class="notification-icon"><i class="fas fa-info-circle"></i></div>
                        <div class="notification-content">
                            <h4>${n.title}</h4>
                            <p>${n.message}</p>
                            <span class="notification-time">${new Date(n.timestamp).toLocaleString()}</span>
                        </div>
                    </li>
                `);
                $list.append($item);
            });

            // Atualiza badge
            $badge.text(data.notifications.length).show();
        });
    }

    // Marcar todas como lidas
    $(document).on('click', '#markAllReadBtn', function(){
        $.ajax({
            url: '/api/notifications/mark-all-read',
            type: 'PUT',
            success: function(){
                loadNotifications(); // recarrega lista
                $badge.hide();
            }
        });
    });

    // Inicializa badge
    $.getJSON('/api/notifications/unread-count', function(data){
        if(data.unreadCount > 0){
            $badge.text(data.unreadCount).addClass('has-unread').show();
        }
    });
});
