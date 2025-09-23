package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.NotificacaoChat;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacaoChatRepository extends JpaRepository<NotificacaoChat, Long> {

    // Buscar notificações não lidas de um usuário
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId AND n.lida = false " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacaoNaoLidas(@Param("usuarioId") Long usuarioId);

    // Contar notificações não lidas de um usuário
    @Query("SELECT COUNT(n) FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId AND n.lida = false")
    long countNotificacaoNaoLidas(@Param("usuarioId") Long usuarioId);

    // Buscar todas as notificações de um usuário (lidas e não lidas)
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacoesDoUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar notificações de um remetente específico para um usuário
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId " +
           "AND n.remetente.id = :remetenteId " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacoesDeRemetente(@Param("usuarioId") Long usuarioId, 
                                                      @Param("remetenteId") Long remetenteId);

    // Buscar notificações não lidas de um remetente específico
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId " +
           "AND n.remetente.id = :remetenteId AND n.lida = false " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacoesNaoLidasDeRemetente(@Param("usuarioId") Long usuarioId, 
                                                             @Param("remetenteId") Long remetenteId);

    // Marcar todas as notificações de um usuário como lidas
    @Modifying
    @Transactional
    @Query("UPDATE NotificacaoChat n SET n.lida = true, n.dataLeitura = :dataLeitura " +
           "WHERE n.usuario.id = :usuarioId AND n.lida = false")
    void marcarTodasComoLidas(@Param("usuarioId") Long usuarioId, 
                              @Param("dataLeitura") LocalDateTime dataLeitura);

    // Marcar notificações de um remetente específico como lidas
    @Modifying
    @Transactional
    @Query("UPDATE NotificacaoChat n SET n.lida = true, n.dataLeitura = :dataLeitura " +
           "WHERE n.usuario.id = :usuarioId AND n.remetente.id = :remetenteId AND n.lida = false")
    void marcarNotificacoesDeRemetenteComoLidas(@Param("usuarioId") Long usuarioId, 
                                               @Param("remetenteId") Long remetenteId, 
                                               @Param("dataLeitura") LocalDateTime dataLeitura);

    // Buscar notificações por tipo
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId " +
           "AND n.tipo = :tipo " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacoesPorTipo(@Param("usuarioId") Long usuarioId, 
                                                  @Param("tipo") NotificacaoChat.TipoNotificacao tipo);

    // Buscar notificações por prioridade
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId " +
           "AND n.prioridade = :prioridade " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacoesPorPrioridade(@Param("usuarioId") Long usuarioId, 
                                                        @Param("prioridade") NotificacaoChat.Prioridade prioridade);

    // Buscar remetentes com notificações não lidas
    @Query("SELECT DISTINCT n.remetente FROM NotificacaoChat n " +
           "WHERE n.usuario.id = :usuarioId AND n.lida = false")
    List<Usuario> findRemetentesComNotificacoesNaoLidas(@Param("usuarioId") Long usuarioId);

    // Contar notificações não lidas por remetente
    @Query("SELECT COUNT(n) FROM NotificacaoChat n " +
           "WHERE n.usuario.id = :usuarioId AND n.remetente.id = :remetenteId AND n.lida = false")
    long countNotificacoesNaoLidasDeRemetente(@Param("usuarioId") Long usuarioId, 
                                              @Param("remetenteId") Long remetenteId);

    // Deletar notificações antigas (mais de X dias)
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificacaoChat n WHERE n.dataCriacao < :dataLimite")
    void deletarNotificacoesAntigas(@Param("dataLimite") LocalDateTime dataLimite);

    // Buscar notificações recentes (últimas X horas)
    @Query("SELECT n FROM NotificacaoChat n WHERE n.usuario.id = :usuarioId " +
           "AND n.dataCriacao >= :dataInicio " +
           "ORDER BY n.dataCriacao DESC")
    List<NotificacaoChat> findNotificacoesRecentes(@Param("usuarioId") Long usuarioId, 
                                                   @Param("dataInicio") LocalDateTime dataInicio);
}