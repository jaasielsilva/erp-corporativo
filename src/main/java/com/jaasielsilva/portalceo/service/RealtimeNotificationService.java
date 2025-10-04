package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealtimeNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    /**
     * Envia notificação em tempo real para um usuário específico
     */
    public void sendNotificationToUser(Usuario user, Notification notification) {
        if (user != null && user.getEmail() != null) {
            String destination = "/queue/notifications/" + user.getEmail();
            Map<String, Object> payload = createNotificationPayload(notification);
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    /**
     * Envia notificação em tempo real para todos os usuários (notificação global)
     */
    public void sendGlobalNotification(Notification notification) {
        String destination = "/topic/notifications";
        Map<String, Object> payload = createNotificationPayload(notification);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Envia notificação para usuários com perfis específicos
     */
    public void sendNotificationToProfiles(Notification notification, java.util.Set<String> profiles) {
        String destination = "/topic/notifications/profiles";
        Map<String, Object> payload = createNotificationPayload(notification);
        payload.put("targetProfiles", profiles);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Envia atualização do contador de notificações não lidas
     */
    public void sendUnreadCountUpdate(Usuario user) {
        if (user != null && user.getEmail() != null) {
            Long unreadCount = notificationService.countUnreadNotificationsForUser(user);
            String destination = "/queue/notifications/count/" + user.getEmail();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "unread_count");
            payload.put("count", unreadCount);
            payload.put("hasUnread", unreadCount > 0);
            
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    /**
     * Envia notificação de que uma notificação foi marcada como lida
     */
    public void sendNotificationReadUpdate(Usuario user, Long notificationId) {
        if (user != null && user.getEmail() != null) {
            String destination = "/queue/notifications/read/" + user.getEmail();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "notification_read");
            payload.put("notificationId", notificationId);
            payload.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    /**
     * Cria o payload da notificação para envio via WebSocket
     */
    private Map<String, Object> createNotificationPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notification.getId());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("type", notification.getType());
        payload.put("priority", notification.getPriority().toString());
        payload.put("actionUrl", notification.getActionUrl());
        payload.put("timestamp", notification.getTimestamp());
        payload.put("isRead", notification.getIsRead());
        payload.put("entityType", notification.getEntityType());
        payload.put("entityId", notification.getEntityId());
        return payload;
    }
}
