package com.jaasielsilva.portalceo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.portalceo.model.UserProgress;
import com.jaasielsilva.portalceo.repository.UserProgressRepository;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdesaoProgressService {
    private static final Logger logger = LoggerFactory.getLogger(AdesaoProgressService.class);

    @Autowired
    private UserProgressRepository repository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WorkflowAdesaoService workflowService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void autosave(Long userId, String flowId, String currentStep, Map<String, Object> formData) {
        try {
            UserProgress up = repository.findByUserIdAndFlowId(userId, flowId)
                    .orElseGet(() -> {
                        UserProgress n = new UserProgress();
                        n.setUserId(userId);
                        n.setFlowId(flowId);
                        n.setCurrentStep(currentStep);
                        n.setCompletedSteps("");
                        n.setFormData("{}");
                        return n;
                    });

            up.setCurrentStep(currentStep);
            up.setFormData(objectMapper.writeValueAsString(formData != null ? formData : Map.of()));
            up.setLastUpdated(LocalDateTime.now());
            if (up.getCreatedAt() == null) up.setCreatedAt(LocalDateTime.now());
            repository.save(up);
            logger.info("Autosave realizado para userId={}, flowId={}, step={}", userId, flowId, currentStep);
        } catch (Exception e) {
            logger.error("Falha no autosave do progresso: {}", e.getMessage());
        }
    }

    public void markStepCompleted(Long userId, String flowId, String step, Map<String, Object> formData) {
        try {
            UserProgress up = repository.findByUserIdAndFlowId(userId, flowId)
                    .orElseGet(() -> {
                        UserProgress n = new UserProgress();
                        n.setUserId(userId);
                        n.setFlowId(flowId);
                        n.setCurrentStep(step);
                        n.setCompletedSteps(step);
                        n.setFormData("{}");
                        n.setCreatedAt(LocalDateTime.now());
                        return n;
                    });

            // Atualizar lista de etapas concluídas
            Set<String> done = new LinkedHashSet<>();
            if (up.getCompletedSteps() != null && !up.getCompletedSteps().isEmpty()) {
                done.addAll(Arrays.asList(up.getCompletedSteps().split(",")));
            }
            done.add(step);
            up.setCompletedSteps(String.join(",", done));

            // Atualizar etapa atual
            up.setCurrentStep(step);

            // Atualizar dados
            Map<String, Object> payload = formData != null ? formData : Map.of();
            up.setFormData(objectMapper.writeValueAsString(payload));
            up.setLastUpdated(LocalDateTime.now());

            repository.save(up);
            logger.info("Etapa marcada como concluída: userId={}, flowId={}, step={}", userId, flowId, step);

            // Auditoria básica via log
            logger.info("Workflow progresso atualizado: flowId={}, step={}", flowId, step);
        } catch (Exception e) {
            logger.error("Erro ao marcar etapa concluída: {}", e.getMessage());
        }
    }

    public Optional<UserProgress> resumeFlow(Long userId) {
        try {
            return repository.findTopByUserIdOrderByLastUpdatedDesc(userId);
        } catch (Exception e) {
            logger.error("Erro ao recuperar progresso do usuário {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> m = new HashMap<>();
        try {
            long total = repository.count();
            long revisao = repository.findAll().stream().filter(p -> "revisao".equalsIgnoreCase(p.getCurrentStep())).count();
            m.put("total", total);
            m.put("emRevisao", revisao);
            return m;
        } catch (Exception e) {
            m.put("erro", e.getMessage());
            return m;
        }
    }

    public void reset(Long userId, String flowId) {
        try {
            if (flowId != null && !flowId.isBlank()) {
                repository.deleteByUserIdAndFlowId(userId, flowId);
            } else {
                repository.deleteByUserId(userId);
            }
        } catch (Exception e) {
            logger.error("Erro ao resetar progresso: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void alertStalledFlows() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            List<UserProgress> stalled = repository.findByLastUpdatedBefore(cutoff);
            for (UserProgress up : stalled) {
                try {
                    notificationService.createGlobalNotification(
                            "workflow_stalled",
                            "Fluxo de Adesão Parado",
                            String.format("Fluxo %s do usuário %d está parado há mais de 24h.", up.getFlowId(), up.getUserId()),
                            com.jaasielsilva.portalceo.model.Notification.Priority.HIGH
                    );
                } catch (Exception ignored) {}
            }
            if (!stalled.isEmpty()) {
                logger.warn("Fluxos parados >24h: {}", stalled.size());
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar fluxos parados: {}", e.getMessage());
        }
    }
}
