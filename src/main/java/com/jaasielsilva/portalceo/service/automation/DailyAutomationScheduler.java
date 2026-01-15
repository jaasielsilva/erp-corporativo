package com.jaasielsilva.portalceo.service.automation;

import com.jaasielsilva.portalceo.event.DailyJobEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DailyAutomationScheduler {

    private final ApplicationEventPublisher eventPublisher;

    public DailyAutomationScheduler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executa a cada minuto
     */
    @Scheduled(cron = "0 * * * * ?")
    public void runMinuteJob() {
        // Dispara evento com a hora atual (truncada para minutos)
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        eventPublisher.publishEvent(new DailyJobEvent(this, now));
    }
}
