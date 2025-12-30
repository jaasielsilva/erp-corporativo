package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

       /**
        * Busca notificações ativas para um usuário específico
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
                     "(n.user = :user OR n.user IS NULL) AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       Page<Notification> findActiveNotificationsForUser(@Param("user") Usuario user,
                     @Param("now") LocalDateTime now,
                     Pageable pageable);

       /**
        * Busca notificações não lidas para um usuário
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND n.isRead = false AND " +
                     "(n.user = :user OR n.user IS NULL) AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findUnreadNotificationsForUser(@Param("user") Usuario user,
                     @Param("now") LocalDateTime now);

       /**
        * Conta notificações não lidas para um usuário
        */
       @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true AND n.isRead = false AND " +
                     "(n.user = :user OR n.user IS NULL) AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now)")
       Long countUnreadNotificationsForUser(@Param("user") Usuario user,
                     @Param("now") LocalDateTime now);

       /**
        * Busca notificações por prioridade
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND n.priority = :priority AND " +
                     "(n.user = :user OR n.user IS NULL) AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findByPriorityForUser(@Param("priority") Notification.Priority priority,
                     @Param("user") Usuario user,
                     @Param("now") LocalDateTime now);

       /**
        * Busca notificações por tipo
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND n.type = :type AND " +
                     "(n.user = :user OR n.user IS NULL) AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findByTypeForUser(@Param("type") String type,
                     @Param("user") Usuario user,
                     @Param("now") LocalDateTime now);

       /**
        * Marca todas as notificações de um usuário como lidas
        */
       @Modifying
       @Query("UPDATE Notification n SET n.isRead = true WHERE n.active = true AND " +
                     "(n.user = :user OR n.user IS NULL) AND n.isRead = false")
       int markAllAsReadForUser(@Param("user") Usuario user);

       /**
        * Marca uma notificação específica como lida
        */
       @Modifying
       @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND " +
                     "(n.user = :user OR n.user IS NULL)")
       int markAsReadForUser(@Param("id") Long id, @Param("user") Usuario user);

       /**
        * Remove notificações expiradas
        */
       @Modifying
       @Query("UPDATE Notification n SET n.active = false WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
       int deactivateExpiredNotifications(@Param("now") LocalDateTime now);

       /**
        * Remove notificações antigas (limpeza automática)
        */
       @Modifying
       @Query("UPDATE Notification n SET n.active = false WHERE n.timestamp < :cutoffDate")
       int deactivateOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

       /**
        * Busca notificações globais (sem usuário específico)
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND n.user IS NULL AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findGlobalNotifications(@Param("now") LocalDateTime now);

       /**
        * Busca notificações por entidade relacionada
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
                     "n.entityType = :entityType AND n.entityId = :entityId AND " +
                     "(n.user = :user OR n.user IS NULL) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findByRelatedEntity(@Param("entityType") String entityType,
                     @Param("entityId") Long entityId,
                     @Param("user") Usuario user);

       /**
        * Busca notificações recentes (últimas 24 horas)
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
                     "n.timestamp >= :since AND " +
                     "(n.user = :user OR n.user IS NULL) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findRecentNotifications(@Param("since") LocalDateTime since,
                     @Param("user") Usuario user);

       /**
        * Estatísticas de notificações por usuário
        */
       @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true AND " +
                     "(n.user = :user OR n.user IS NULL)")
       Long countTotalNotificationsForUser(@Param("user") Usuario user);

       /**
        * Busca notificações por período
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
                     "n.timestamp BETWEEN :startDate AND :endDate AND " +
                     "(n.user = :user OR n.user IS NULL) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("user") Usuario user);

       /**
        * Busca notificações globais ativas (compatibilidade com NotificacaoService)
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND n.user IS NULL AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findActiveGlobalNotifications(@Param("now") LocalDateTime now);

       /**
        * Busca notificações globais não lidas (compatibilidade com NotificacaoService)
        */
       @Query("SELECT n FROM Notification n WHERE n.active = true AND n.isRead = false AND n.user IS NULL AND " +
                     "(n.expiresAt IS NULL OR n.expiresAt > :now) " +
                     "ORDER BY n.timestamp DESC")
       List<Notification> findUnreadGlobalNotifications(@Param("now") LocalDateTime now);

       boolean existsByTypeAndEntityTypeAndEntityId(String type, String entityType, Long entityId);
}
