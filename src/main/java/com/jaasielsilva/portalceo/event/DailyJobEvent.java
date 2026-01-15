package com.jaasielsilva.portalceo.event;

import org.springframework.context.ApplicationEvent;
import java.time.LocalTime;

public class DailyJobEvent extends ApplicationEvent {

    private final LocalTime executionTime;

    public DailyJobEvent(Object source, LocalTime executionTime) {
        super(source);
        this.executionTime = executionTime;
    }

    public LocalTime getExecutionTime() {
        return executionTime;
    }
}
