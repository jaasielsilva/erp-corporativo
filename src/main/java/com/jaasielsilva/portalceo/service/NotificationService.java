package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Cria uma nova notificação
     */
    public Notification createNotification(String type, String title, String message,
            Notification.Priority priority, Usuario user) {
        Notification notification = new Notification(type, title, message, priority, user);
        return notificationRepository.save(notification);
    }

    /**
     * Cria uma notificação global (para todos os usuários)
     */
    public Notification createGlobalNotification(String type, String title, String message,
            Notification.Priority priority) {
        Notification notification = new Notification(type, title, message, priority);
        return notificationRepository.save(notification);
    }

    /**
     * Cria uma notificação com perfis específicos
     */
    public Notification createNotificationForProfiles(String type, String title, String message,
            Notification.Priority priority, Set<String> profiles) {
        Notification notification = new Notification(type, title, message, priority);
        notification.setRelevantProfiles(profiles);
        return notificationRepository.save(notification);
    }

    /**
     * Cria uma notificação relacionada a uma entidade
     */
    public Notification createEntityNotification(String type, String title, String message,
            Notification.Priority priority, Usuario user,
            String entityType, Long entityId) {
        Notification notification = new Notification(type, title, message, priority, user);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        return notificationRepository.save(notification);
    }

    /**
     * Busca notificações ativas para um usuário com paginação
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(Usuario user, Pageable pageable) {
        return notificationRepository.findActiveNotificationsForUser(user, LocalDateTime.now(), pageable);
    }

    /**
     * Busca notificações não lidas para um usuário
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsForUser(Usuario user) {
        return notificationRepository.findUnreadNotificationsForUser(user, LocalDateTime.now());
    }

    /**
     * Conta notificações não lidas para um usuário
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotificationsForUser(Usuario user) {
        return notificationRepository.countUnreadNotificationsForUser(user, LocalDateTime.now());
    }

    /**
     * Busca notificações por prioridade
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByPriority(Usuario user, Notification.Priority priority) {
        return notificationRepository.findByPriorityForUser(priority, user, LocalDateTime.now());
    }

    /**
     * Busca notificações por tipo
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(Usuario user, String type) {
        return notificationRepository.findByTypeForUser(type, user, LocalDateTime.now());
    }

    /**
     * Marca uma notificação como lida
     */
    public boolean markAsRead(Long notificationId, Usuario user) {
        return notificationRepository.markAsReadForUser(notificationId, user) > 0;
    }

    /**
     * Marca todas as notificações de um usuário como lidas
     */
    public int markAllAsRead(Usuario user) {
        return notificationRepository.markAllAsReadForUser(user);
    }

    /**
     * Remove uma notificação (marca como inativa)
     */
    public boolean deleteNotification(Long notificationId, Usuario user) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            Notification n = notification.get();
            // Verifica se o usuário tem permissão para deletar
            if (n.getUser() == null || n.getUser().equals(user)) {
                n.setActive(false);
                notificationRepository.save(n);
                return true;
            }
        }
        return false;
    }

    /**
     * Busca notificações relacionadas a uma entidade
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByEntity(Usuario user, String entityType, Long entityId) {
        return notificationRepository.findByRelatedEntity(entityType, entityId, user);
    }

    /**
     * Busca notificações recentes (últimas 24 horas)
     */
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(Usuario user) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return notificationRepository.findRecentNotifications(since, user);
    }

    /**
     * Estatísticas de notificações para um usuário
     */
    @Transactional(readOnly = true)
    public Long getTotalNotificationsCount(Usuario user) {
        return notificationRepository.countTotalNotificationsForUser(user);
    }

    /**
     * Limpeza automática de notificações expiradas (executada a cada hora)
     */
    @Scheduled(fixedRate = 3600000) // 1 hora
    @Async
    public void cleanupExpiredNotifications() {
        int deactivated = notificationRepository.deactivateExpiredNotifications(LocalDateTime.now());
        if (deactivated > 0) {
            System.out.println("🧹 Limpeza automática: " + deactivated + " notificações expiradas removidas");
        }
    }

    /**
     * Limpeza automática de notificações antigas (executada diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Todo dia às 2h da manhã
    @Async
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Remove notificações com mais de 30 dias
        int deactivated = notificationRepository.deactivateOldNotifications(cutoffDate);
        if (deactivated > 0) {
            System.out.println("🧹 Limpeza automática: " + deactivated + " notificações antigas removidas");
        }
    }

    // Métodos de conveniência para tipos específicos de notificação

    /**
     * Notificação de nova solicitação de acesso
     */
    public Notification notifyNewAccessRequest(Usuario user, String protocol, String requesterName) {
        return createNotification(
                "access_request",
                "Nova Solicitação de Acesso",
                String.format("Solicitação %s criada por %s aguarda aprovação.", protocol, requesterName),
                Notification.Priority.HIGH,
                user);
    }

    /**
     * Notificação de aprovação de solicitação
     */
    public Notification notifyAccessRequestApproved(Usuario user, String protocol) {
        return createNotification(
                "access_approved",
                "Solicitação Aprovada",
                String.format("Sua solicitação %s foi aprovada com sucesso.", protocol),
                Notification.Priority.MEDIUM,
                user);
    }

    /**
     * Notificação de rejeição de solicitação
     */
    public Notification notifyAccessRequestRejected(Usuario user, String protocol, String reason) {
        return createNotification(
                "access_rejected",
                "Solicitação Rejeitada",
                String.format("Sua solicitação %s foi rejeitada. Motivo: %s", protocol, reason),
                Notification.Priority.MEDIUM,
                user);
    }

    /**
     * Notificação de sistema/manutenção
     */
    public Notification notifySystemMaintenance(String message, LocalDateTime scheduledTime) {
        Notification notification = createGlobalNotification(
                "maintenance",
                "Manutenção Programada",
                message,
                Notification.Priority.MEDIUM);
        notification.setExpiresAt(scheduledTime.plusHours(2)); // Expira 2 horas após a manutenção
        return notificationRepository.save(notification);
    }

    /**
     * Notificação de treinamento obrigatório
     */
    public Notification notifyMandatoryTraining(Set<String> profiles, String trainingName, LocalDateTime deadline) {
        Notification notification = createNotificationForProfiles(
                "training",
                "Treinamento Obrigatório",
                String.format("Treinamento '%s' deve ser concluído até %s.", trainingName, deadline.toLocalDate()),
                Notification.Priority.HIGH,
                profiles);
        notification.setExpiresAt(deadline);
        return notificationRepository.save(notification);
    }

    public void enviarNotificacaoRenovacao(ContratoLegal contrato) {
        String destinatario = contrato.getCliente().getEmail();
        String assunto = "Renovação de Contrato";
        String corpo = String.format("""
                    Olá %s,

                    O seu contrato %s está próximo da data de renovação (%s).

                    Por favor, entre em contato para efetuar a renovação.

                    Att,
                    Equipe Portal CEO
                """, contrato.getCliente().getNome(), contrato.getNumeroContrato(), contrato.getDataFim());

        emailService.enviarEmail(destinatario, assunto, corpo);
    }
}