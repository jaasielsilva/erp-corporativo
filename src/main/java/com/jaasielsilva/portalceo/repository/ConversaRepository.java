package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Conversa;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversaRepository extends JpaRepository<Conversa, Long> {

       // Buscar conversa individual entre dois usuários específicos
       @Query("SELECT c FROM Conversa c " +
                     "WHERE c.tipo = 'INDIVIDUAL' " +
                     "AND ((c.usuario1Id = :usuario1Id AND c.usuario2Id = :usuario2Id) " +
                     "OR (c.usuario1Id = :usuario2Id AND c.usuario2Id = :usuario1Id)) " +
                     "AND c.ativa = true")
       Optional<Conversa> findConversaEntreUsuarios(@Param("usuario1Id") Long usuario1Id,
                     @Param("usuario2Id") Long usuario2Id);

       // Buscar todas as conversas de um usuário (individuais e em grupo), ordenadas por última atividade
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "LEFT JOIN c.participantes p " +
                     "WHERE (c.tipo = 'INDIVIDUAL' AND (c.usuario1Id = :usuarioId OR c.usuario2Id = :usuarioId)) " +
                     "OR (c.tipo IN ('GRUPO', 'DEPARTAMENTO') AND p.usuario.id = :usuarioId AND p.ativo = true) " +
                     "AND c.ativa = true " +
                     "ORDER BY c.ultimaAtividade DESC")
       List<Conversa> findConversasDoUsuario(@Param("usuarioId") Long usuarioId);

       // Buscar conversas ativas de um usuário
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "WHERE p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND c.ativa = true")
       List<Conversa> findConversasAtivasDoUsuario(@Param("usuarioId") Long usuarioId);

       // Buscar conversas inativas de um usuário
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "WHERE p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND c.ativa = false")
       List<Conversa> findConversasInativasDoUsuario(@Param("usuarioId") Long usuarioId);

       // Contar conversas ativas de um usuário
       @Query("SELECT COUNT(DISTINCT c) FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "WHERE p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND c.ativa = true")
       long countConversasAtivasDoUsuario(@Param("usuarioId") Long usuarioId);

       // Buscar conversas com mensagens não lidas para um usuário
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "JOIN c.mensagens m " +
                     "WHERE p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND m.remetente.id != :usuarioId " +
                     "AND m.lida = false " +
                     "AND c.ativa = true " +
                     "ORDER BY c.ultimaAtividade DESC")
       List<Conversa> findConversasComMensagensNaoLidas(@Param("usuarioId") Long usuarioId);

       @Query("""
                         SELECT DISTINCT c
                         FROM Conversa c
                         JOIN c.participantes p
                         WHERE p.usuario.id = :usuarioId
                           AND p.ativo = true
                           AND c.ultimaAtividade >= :limite
                         ORDER BY c.ultimaAtividade DESC
                     """)
       List<Conversa> findConversasRecentes(@Param("usuarioId") Long usuarioId,
                     @Param("limite") LocalDateTime limite);


                     
       // Verificar se existe conversa individual entre dois usuários
       @Query("SELECT COUNT(c) > 0 FROM Conversa c " +
                     "WHERE c.tipo = 'INDIVIDUAL' " +
                     "AND ((c.usuario1Id = :usuario1Id AND c.usuario2Id = :usuario2Id) " +
                     "OR (c.usuario1Id = :usuario2Id AND c.usuario2Id = :usuario1Id)) " +
                     "AND c.ativa = true")
       boolean existeConversaEntreUsuarios(@Param("usuario1Id") Long usuario1Id,
                     @Param("usuario2Id") Long usuario2Id);

       // Buscar usuários com quem o usuário já conversou
       @Query("SELECT DISTINCT p2.usuario " +
                     "FROM Conversa c " +
                     "JOIN c.participantes p1 " +
                     "JOIN c.participantes p2 " +
                     "WHERE p1.usuario.id = :usuarioId " +
                     "AND p1.ativo = true " +
                     "AND p2.ativo = true " +
                     "AND p2.usuario.id != :usuarioId " +
                     "AND c.ativa = true")
       List<Usuario> findUsuariosComQuemJaConversou(@Param("usuarioId") Long usuarioId);

       // Buscar conversas por status ativo/inativo
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "WHERE p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND c.ativa = :ativa " +
                     "ORDER BY c.ultimaAtividade DESC")
       List<Conversa> findConversasPorStatus(@Param("usuarioId") Long usuarioId,
                     @Param("ativa") boolean ativa);

       // ==================== QUERIES ESPECÍFICAS PARA GRUPOS ====================

       // Buscar conversas em grupo de um usuário
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "WHERE p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND c.tipo IN ('GRUPO', 'DEPARTAMENTO') " +
                     "AND c.ativa = true " +
                     "ORDER BY c.ultimaAtividade DESC")
       List<Conversa> findConversasGrupoDoUsuario(@Param("usuarioId") Long usuarioId);

       // Buscar conversas individuais de um usuário
       @Query("SELECT c FROM Conversa c " +
                     "WHERE c.tipo = 'INDIVIDUAL' " +
                     "AND (c.usuario1Id = :usuarioId OR c.usuario2Id = :usuarioId) " +
                     "AND c.ativa = true " +
                     "ORDER BY c.ultimaAtividade DESC")
       List<Conversa> findConversasIndividuaisDoUsuario(@Param("usuarioId") Long usuarioId);

       // Buscar conversa em grupo por ID e verificar se usuário participa
       @Query("SELECT c FROM Conversa c " +
                     "JOIN c.participantes p " +
                     "WHERE c.id = :conversaId " +
                     "AND p.usuario.id = :usuarioId " +
                     "AND p.ativo = true " +
                     "AND c.tipo IN ('GRUPO', 'DEPARTAMENTO') " +
                     "AND c.ativa = true")
       Optional<Conversa> findConversaGrupoPorIdEUsuario(@Param("conversaId") Long conversaId,
                     @Param("usuarioId") Long usuarioId);

       // Buscar conversas por tipo
       @Query("SELECT DISTINCT c FROM Conversa c " +
                     "LEFT JOIN c.participantes p " +
                     "WHERE c.tipo = :tipo " +
                     "AND ((c.tipo = 'INDIVIDUAL' AND (c.usuario1Id = :usuarioId OR c.usuario2Id = :usuarioId)) " +
                     "OR (c.tipo IN ('GRUPO', 'DEPARTAMENTO') AND p.usuario.id = :usuarioId AND p.ativo = true)) " +
                     "AND c.ativa = true " +
                     "ORDER BY c.ultimaAtividade DESC")
       List<Conversa> findConversasPorTipo(@Param("usuarioId") Long usuarioId,
                     @Param("tipo") String tipo);
}