package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.NotificationService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // 🔹 injetado

    /**
     * Endpoint para testar notificações em tempo real para um usuário específico
     */
    @PostMapping("/notification")
    public ResponseEntity<Map<String, Object>> createTestNotification(
            @RequestParam(defaultValue = "Teste") String title,
            @RequestParam(defaultValue = "Esta é uma notificação de teste") String message,
            @RequestParam(defaultValue = "info") String type,
            Authentication authentication) {

        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Notification.Priority priority = Notification.Priority.MEDIUM;
            if ("error".equals(type)) {
                priority = Notification.Priority.HIGH;
            } else if ("success".equals(type)) {
                priority = Notification.Priority.LOW;
            }

            Notification notification = notificationService.createNotification(
                    type, title, message, priority, user);

            // 🔹 Enviar notificação via WebSocket
            messagingTemplate.convertAndSend("/queue/notifications/" + user.getEmail(), notification);
            messagingTemplate.convertAndSend("/topic/notifications", notification);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notificação de teste criada com sucesso");
            response.put("notificationId", notification.getId());
            response.put("title", notification.getTitle());
            response.put("message", notification.getMessage());
            response.put("type", notification.getType());
            response.put("priority", notification.getPriority());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erro ao criar notificação de teste: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint para testar notificação global
     */
    @PostMapping("/notification/global")
    public ResponseEntity<Map<String, Object>> createTestGlobalNotification(
            @RequestParam(defaultValue = "Notificação Global") String title,
            @RequestParam(defaultValue = "Esta é uma notificação global de teste") String message) {

        try {
            Notification notification = notificationService.createGlobalNotification(
                    "info", title, message, Notification.Priority.MEDIUM);

            // 🔹 Enviar notificação global via WebSocket
            messagingTemplate.convertAndSend("/topic/notifications", notification);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notificação global de teste criada com sucesso");
            response.put("notificationId", notification.getId());
            response.put("title", notification.getTitle());
            response.put("message", notification.getMessage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erro ao criar notificação global de teste: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
