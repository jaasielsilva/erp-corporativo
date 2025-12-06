package com.jaasielsilva.portalceo.model.chat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_presence")
public class ChatPresence {
    @Id
    private Long userId;

    @Column(nullable = false, length = 8)
    private String status;

    @Column(nullable = false)
    private LocalDateTime lastHeartbeat = LocalDateTime.now();

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
}
