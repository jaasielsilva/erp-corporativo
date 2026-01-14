package com.jaasielsilva.portalceo.dto;

public class AutomationDTO {
    private Long id;
    private String eventType;
    private String actionType;
    private String executionTime;
    private boolean active;

    public AutomationDTO() {
    }

    public AutomationDTO(Long id, String eventType, String actionType, String executionTime, boolean active) {
        this.id = id;
        this.eventType = eventType;
        this.actionType = actionType;
        this.executionTime = executionTime;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getExecutionTime() { return executionTime; }
    public void setExecutionTime(String executionTime) { this.executionTime = executionTime; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
