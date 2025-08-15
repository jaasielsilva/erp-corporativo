package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.NotificationService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * Busca notificações paginadas para o usuário atual
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            Authentication authentication) {
        
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            Page<Notification> notificationsPage;
            
            // Aplicar filtros se necessário
            if ("unread".equals(filter)) {
                List<Notification> unreadList = notificationService.getUnreadNotificationsForUser(user);
                // Converter para formato de resposta
                List<Map<String, Object>> notifications = unreadList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
                
                Map<String, Object> response = new HashMap<>();
                response.put("notifications", notifications);
                response.put("totalElements", unreadList.size());
                response.put("totalPages", 1);
                response.put("currentPage", 0);
                response.put("pageSize", unreadList.size());
                
                return ResponseEntity.ok(response);
            } else if ("high".equals(filter)) {
                List<Notification> highPriorityList = notificationService.getNotificationsByPriority(user, Notification.Priority.HIGH);
                List<Map<String, Object>> notifications = highPriorityList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
                
                Map<String, Object> response = new HashMap<>();
                response.put("notifications", notifications);
                response.put("totalElements", highPriorityList.size());
                response.put("totalPages", 1);
                response.put("currentPage", 0);
                response.put("pageSize", highPriorityList.size());
                
                return ResponseEntity.ok(response);
            } else {
                notificationsPage = notificationService.getNotificationsForUser(user, pageable);
            }
            
            List<Map<String, Object>> notifications = notificationsPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("totalElements", notificationsPage.getTotalElements());
            response.put("totalPages", notificationsPage.getTotalPages());
            response.put("currentPage", notificationsPage.getNumber());
            response.put("pageSize", notificationsPage.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Conta notificações não lidas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Long unreadCount = notificationService.countUnreadNotificationsForUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Marca uma notificação como lida
     */
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id, 
            Authentication authentication) {
        
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean success = notificationService.markAsRead(id, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            int markedCount = notificationService.markAllAsRead(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("markedCount", markedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Remove uma notificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long id, 
            Authentication authentication) {
        
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean success = notificationService.deleteNotification(id, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cria uma nova notificação (para testes ou admin)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            String type = (String) request.get("type");
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String priorityStr = (String) request.getOrDefault("priority", "medium");
            
            Notification.Priority priority = Notification.Priority.fromString(priorityStr);
            
            Notification notification = notificationService.createNotification(type, title, message, priority, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notification", convertToResponse(notification));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Busca estatísticas de notificações
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats(Authentication authentication) {
        try {
            Usuario user = getCurrentUser(authentication);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Long totalCount = notificationService.getTotalNotificationsCount(user);
            Long unreadCount = notificationService.countUnreadNotificationsForUser(user);
            List<Notification> recentNotifications = notificationService.getRecentNotifications(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", totalCount);
            response.put("unreadCount", unreadCount);
            response.put("readCount", totalCount - unreadCount);
            response.put("recentCount", recentNotifications.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Converte uma notificação para o formato de resposta JSON
     */
    private Map<String, Object> convertToResponse(Notification notification) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", notification.getId());
        response.put("type", notification.getType());
        response.put("title", notification.getTitle());
        response.put("message", notification.getMessage());
        response.put("icon", notification.getIcon());
        response.put("priority", notification.getPriority().getValue());
        response.put("timestamp", notification.getTimestamp().toString());
        response.put("read", notification.getIsRead());
        response.put("actionUrl", notification.getActionUrl());
        response.put("entityType", notification.getEntityType());
        response.put("entityId", notification.getEntityId());
        
        return response;
    }
    
    /**
     * Obtém o usuário atual da autenticação
     */
    private Usuario getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String email = authentication.getName();
        return usuarioService.buscarPorEmail(email).orElse(null);
    }
}