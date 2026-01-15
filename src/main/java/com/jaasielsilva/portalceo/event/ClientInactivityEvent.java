package com.jaasielsilva.portalceo.event;

import org.springframework.context.ApplicationEvent;

public class ClientInactivityEvent extends ApplicationEvent {
    
    private final long totalInativos;

    public ClientInactivityEvent(Object source, long totalInativos) {
        super(source);
        this.totalInativos = totalInativos;
    }

    public long getTotalInativos() {
        return totalInativos;
    }
}
