package com.jaasielsilva.portalceo.model.automation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "automation_logs")
public class AutomationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "automation_id", nullable = false)
    private UserAutomation automation;

    private LocalDateTime timestamp;

    private String status; // SUCCESS, ERROR

    @Column(length = 500)
    private String message;

    public AutomationLog() {
    }

    public AutomationLog(UserAutomation automation, String status, String message) {
        this.automation = automation;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAutomation getAutomation() { return automation; }
    public void setAutomation(UserAutomation automation) { this.automation = automation; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
