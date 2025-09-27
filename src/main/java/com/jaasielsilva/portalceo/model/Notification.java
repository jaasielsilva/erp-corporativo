package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(length = 100)
    private String icon;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    // Usuário destinatário (null = notificação global)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Usuario user;
    
    // Perfis relevantes para a notificação
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notification_profiles", 
                    joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "profile_name")
    private Set<String> relevantProfiles;
    
    // Dados adicionais em JSON (opcional)
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    // Data de expiração (opcional)
    @Column
    private LocalDateTime expiresAt;
    
    // Ação relacionada (URL, endpoint, etc.)
    @Column(length = 500)
    private String actionUrl;
    
    // Referência a entidade relacionada
    @Column
    private String entityType;
    
    @Column
    private Long entityId;
    
    public enum Priority {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high");
        
        private final String value;
        
        Priority(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static Priority fromString(String value) {
            for (Priority priority : Priority.values()) {
                if (priority.value.equalsIgnoreCase(value)) {
                    return priority;
                }
            }
            return MEDIUM;
        }
    }
    
    // Métodos de conveniência
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isRelevantForProfile(String profileName) {
        return relevantProfiles == null || 
               relevantProfiles.isEmpty() || 
               relevantProfiles.contains(profileName);
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public void markAsUnread() {
        this.isRead = false;
    }
    
    // Construtor para notificações simples
    public Notification(String type, String title, String message, Priority priority) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.active = true;
    }
    
    // Construtor para notificações direcionadas
    public Notification(String type, String title, String message, Priority priority, Usuario user) {
        this(type, title, message, priority);
        this.user = user;
    }
}