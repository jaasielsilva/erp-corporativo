package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.NotificationService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Obtém notificações do usuário logado com filtros
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            Authentication authentication) {
        
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
            Page<Notification> notificationsPage;

            // Obter todas as notificações primeiro
            Page<Notification> allNotifications = notificationService.getNotificationsForUser(user, pageable);

            // Aplicar filtros
            if ("unread".equals(filter)) {
                List<Notification> unreadNotifications = allNotifications.getContent().stream()
                        .filter(n -> !n.getIsRead())
                        .collect(Collectors.toList());
                notificationsPage = new PageImpl<>(unreadNotifications, pageable, unreadNotifications.size());
            } else if ("recent".equals(filter)) {
                // Últimas 24 horas
                List<Notification> recentNotifications = notificationService.getRecentNotifications(user);
                notificationsPage = new PageImpl<>(recentNotifications, pageable, recentNotifications.size());
            } else if (priority != null && !priority.isEmpty()) {
                Notification.Priority priorityEnum = Notification.Priority.fromString(priority);
                List<Notification> priorityNotifications = notificationService.getNotificationsByPriority(
                        user, priorityEnum);
                notificationsPage = new PageImpl<>(priorityNotifications, pageable, priorityNotifications.size());
            } else {
                notificationsPage = allNotifications;
            }

            // Filtrar por categoria se especificada
            List<Notification> filteredContent = notificationsPage.getContent();
            if (category != null && !category.isEmpty()) {
                filteredContent = filteredContent.stream()
                        .filter(n -> category.equals(n.getType()))
                        .collect(Collectors.toList());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", filteredContent);
            response.put("currentPage", notificationsPage.getNumber());
            response.put("totalPages", notificationsPage.getTotalPages());
            response.put("totalElements", notificationsPage.getTotalElements());
            response.put("unreadCount", notificationService.countUnreadNotificationsForUser(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marca uma notificação como lida
     */
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            boolean success = notificationService.markAsRead(id, user);
            
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Notificação marcada como lida");
            } else {
                response.put("status", "error");
                response.put("message", "Erro ao marcar notificação como lida");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marca todas as notificações como lidas
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            int updated = notificationService.markAllAsRead(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", updated + " notificações marcadas como lidas");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remove (desativa) uma notificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            boolean success = notificationService.deleteNotification(id, user);
            
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Notificação removida");
            } else {
                response.put("status", "error");
                response.put("message", "Erro ao remover notificação");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém o contador de notificações não lidas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Long count = notificationService.countUnreadNotificationsForUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("hasUnread", count > 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINTS DE COMPATIBILIDADE COM NotificacaoController =====

    /**
     * Obtém todas as notificações do usuário (compatibilidade com /api/notificacoes)
     */
    @GetMapping("/legacy")
    public ResponseEntity<List<Map<String, Object>>> obterNotificacoesLegacy(Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications = notificationService.getNotificationsForUser(user, 
                PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();
            
            List<Map<String, Object>> response = notifications.stream()
                .map(this::convertToLegacyFormat)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém apenas as notificações não lidas (compatibilidade com /api/notificacoes/nao-lidas)
     */
    @GetMapping("/legacy/nao-lidas")
    public ResponseEntity<List<Map<String, Object>>> obterNotificacaoesNaoLidasLegacy(Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications = notificationService.getUnreadNotificationsForUser(user);
            
            List<Map<String, Object>> response = notifications.stream()
                .map(this::convertToLegacyFormat)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém o contador de notificações não lidas (compatibilidade com /api/notificacoes/contador)
     */
    @GetMapping("/legacy/contador")
    public ResponseEntity<Map<String, Object>> obterContadorNotificacoesLegacy(Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            long contador = notificationService.countUnreadNotificationsForUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("contador", contador);
            response.put("temNovas", contador > 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marca uma notificação específica como lida (compatibilidade com /api/notificacoes/{id}/marcar-lida)
     */
    @PostMapping("/legacy/{notificacaoId}/marcar-lida")
    public ResponseEntity<Map<String, String>> marcarComoLidaLegacy(
            @PathVariable String notificacaoId,
            Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            boolean success = notificationService.markAsRead(Long.parseLong(notificacaoId), user);
            
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Notificação marcada como lida");
            } else {
                response.put("status", "error");
                response.put("message", "Erro ao marcar notificação como lida");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marca todas as notificações como lidas (compatibilidade com /api/notificacoes/marcar-todas-lidas)
     */
    @PostMapping("/legacy/marcar-todas-lidas")
    public ResponseEntity<Map<String, String>> marcarTodasComoLidasLegacy(Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            int updated = notificationService.markAllAsRead(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", updated + " notificações marcadas como lidas");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Converte Notification para formato legacy (compatibilidade com NotificacaoService)
     */
    private Map<String, Object> convertToLegacyFormat(Notification notification) {
        Map<String, Object> legacy = new HashMap<>();
        legacy.put("id", notification.getId().toString());
        legacy.put("titulo", notification.getTitle());
        legacy.put("mensagem", notification.getMessage());
        legacy.put("tipo", notification.getType());
        legacy.put("url", notification.getActionUrl());
        legacy.put("dataHora", notification.getTimestamp());
        legacy.put("lida", notification.getIsRead());
        return legacy;
    }
}