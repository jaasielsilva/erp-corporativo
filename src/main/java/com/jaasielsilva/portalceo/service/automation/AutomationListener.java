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

import com.jaasielsilva.portalceo.model.automation.AutomationLog;
import com.jaasielsilva.portalceo.repository.automation.AutomationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AutomationListener {

    private static final Logger logger = LoggerFactory.getLogger(AutomationListener.class);
    private final UserAutomationRepository automationRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AutomationLogRepository logRepository;

    public AutomationListener(UserAutomationRepository automationRepository,
                              EmailService emailService,
                              NotificationService notificationService,
                              AutomationLogRepository logRepository) {
        this.automationRepository = automationRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.logRepository = logRepository;
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

                try {
                    if (rule.getActionType() == AutomationActionType.EMAIL_ALERT) {
                        emailService.enviarEmail(rule.getUsuario().getEmail(), "Alerta de Clientes Inativos", msg);
                    } else if (rule.getActionType() == AutomationActionType.SYSTEM_NOTIFICATION) {
                        notificationService.enviarNotificacao(rule.getUsuario().getEmail(), "Automação", msg);
                    }
                    
                    // Log de Sucesso
                    logRepository.save(new AutomationLog(rule, "SUCCESS", "Executado com sucesso. " + event.getTotalInativos() + " clientes identificados."));
                    
                } catch (Exception e) {
                    logger.error("Erro ao executar automação ID {}: {}", rule.getId(), e.getMessage());
                    // Log de Erro
                    logRepository.save(new AutomationLog(rule, "ERROR", "Falha na execução: " + e.getMessage()));
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
