package com.jaasielsilva.portalceo.model.automation;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "user_automations")
public class UserAutomation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutomationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutomationActionType actionType;

    private boolean active = true;

    @Column(name = "execution_time")
    private LocalTime executionTime = LocalTime.of(8, 0); // Default 08:00

    public UserAutomation() {}

    public UserAutomation(Usuario usuario, AutomationEventType eventType, AutomationActionType actionType, LocalTime executionTime) {
        this.usuario = usuario;
        this.eventType = eventType;
        this.actionType = actionType;
        this.executionTime = executionTime != null ? executionTime : LocalTime.of(8, 0);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public AutomationEventType getEventType() { return eventType; }
    public void setEventType(AutomationEventType eventType) { this.eventType = eventType; }
    public AutomationActionType getActionType() { return actionType; }
    public void setActionType(AutomationActionType actionType) { this.actionType = actionType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalTime executionTime) { this.executionTime = executionTime; }
}
