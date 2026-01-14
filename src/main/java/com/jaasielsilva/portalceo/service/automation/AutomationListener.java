package com.jaasielsilva.portalceo.service.automation;

import com.jaasielsilva.portalceo.event.ClientInactivityEvent;
import com.jaasielsilva.portalceo.model.automation.AutomationActionType;
import com.jaasielsilva.portalceo.model.automation.AutomationEventType;
import com.jaasielsilva.portalceo.model.automation.UserAutomation;
import com.jaasielsilva.portalceo.repository.automation.UserAutomationRepository;
import com.jaasielsilva.portalceo.service.EmailService;
import com.jaasielsilva.portalceo.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;

import java.time.LocalTime;

@Component
public class AutomationListener {

    private final UserAutomationRepository automationRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public AutomationListener(UserAutomationRepository automationRepository,
                              EmailService emailService,
                              NotificationService notificationService) {
        this.automationRepository = automationRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void handleClientInactivity(ClientInactivityEvent event) {
        List<UserAutomation> rules = automationRepository.findByEventTypeAndActiveTrue(AutomationEventType.CLIENTE_INATIVO);
        LocalTime now = LocalTime.now();

        for (UserAutomation rule : rules) {
            // Verifica se a hora atual coincide com a hora configurada (com tolerância de minutos)
            if (isSameHour(rule.getExecutionTime(), now)) {
                String msg = "Detectamos " + event.getTotalInativos() + " clientes inativos há mais de 30 dias. Verifique a base para recuperação.";

                if (rule.getActionType() == AutomationActionType.EMAIL_ALERT) {
                    emailService.enviarEmail(rule.getUsuario().getEmail(), "Alerta de Clientes Inativos", msg);
                } else if (rule.getActionType() == AutomationActionType.SYSTEM_NOTIFICATION) {
                    notificationService.enviarNotificacao(rule.getUsuario().getEmail(), "Automação", msg);
                }
            }
        }
    }

    private boolean isSameHour(LocalTime configTime, LocalTime currentTime) {
        if (configTime == null) return false;
        // Compara apenas a HORA. Ex: Config 08:00 bate com 08:00, 08:30, 08:59? 
        // Não, o job roda na hora cheia (08:00), então a comparação deve ser exata na hora.
        return configTime.getHour() == currentTime.getHour();
    }
}
