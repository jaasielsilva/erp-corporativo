package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Conversa;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    // Buscar conversa entre dois usuários específicos
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuario1Id AND c.usuario2.id = :usuario2Id) OR " +
           "(c.usuario1.id = :usuario2Id AND c.usuario2.id = :usuario1Id)")
    Optional<Conversa> findConversaEntreUsuarios(@Param("usuario1Id") Long usuario1Id, 
                                                 @Param("usuario2Id") Long usuario2Id);

    // Buscar todas as conversas de um usuário, ordenadas por última atividade
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = true " +
           "ORDER BY c.ultimaAtividade DESC")
    List<Conversa> findConversasDoUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar conversas ativas de um usuário
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = true")
    List<Conversa> findConversasAtivasDoUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar conversas inativas de um usuário
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = false")
    List<Conversa> findConversasInativasDoUsuario(@Param("usuarioId") Long usuarioId);

    // Contar conversas ativas de um usuário
    @Query("SELECT COUNT(c) FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = true")
    long countConversasAtivasDoUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar conversas com mensagens não lidas para um usuário
    @Query("SELECT DISTINCT c FROM Conversa c " +
           "JOIN c.mensagens m " +
           "WHERE (c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND m.destinatario.id = :usuarioId " +
           "AND m.lida = false " +
           "AND c.ativa = true " +
           "ORDER BY c.ultimaAtividade DESC")
    List<Conversa> findConversasComMensagensNaoLidas(@Param("usuarioId") Long usuarioId);

    // Buscar conversas recentes (com atividade nos últimos X dias)
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = true " +
           "AND c.ultimaAtividade >= CURRENT_TIMESTAMP - :dias " +
           "ORDER BY c.ultimaAtividade DESC")
    List<Conversa> findConversasRecentes(@Param("usuarioId") Long usuarioId, 
                                         @Param("dias") int dias);

    // Verificar se existe conversa entre dois usuários
    @Query("SELECT COUNT(c) > 0 FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuario1Id AND c.usuario2.id = :usuario2Id) OR " +
           "(c.usuario1.id = :usuario2Id AND c.usuario2.id = :usuario1Id)")
    boolean existeConversaEntreUsuarios(@Param("usuario1Id") Long usuario1Id, 
                                        @Param("usuario2Id") Long usuario2Id);

    // Buscar usuários com quem o usuário já conversou
    @Query("SELECT DISTINCT CASE " +
           "WHEN c.usuario1.id = :usuarioId THEN c.usuario2 " +
           "ELSE c.usuario1 END " +
           "FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = true")
    List<Usuario> findUsuariosComQuemJaConversou(@Param("usuarioId") Long usuarioId);

    // Buscar conversas por status ativo/inativo
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId) " +
           "AND c.ativa = :ativa " +
           "ORDER BY c.ultimaAtividade DESC")
    List<Conversa> findConversasPorStatus(@Param("usuarioId") Long usuarioId, 
                                          @Param("ativa") boolean ativa);
}