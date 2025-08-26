package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Conversa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    // Buscar mensagens de uma conversa específica, ordenadas por data
    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId ORDER BY m.enviadaEm ASC")
    List<Mensagem> findByConversaIdOrderByDataEnvioAsc(@Param("conversaId") Long conversaId);

    // Buscar mensagens de uma conversa com paginação
    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId ORDER BY m.enviadaEm DESC")
    Page<Mensagem> findByConversaIdOrderByDataEnvioDesc(@Param("conversaId") Long conversaId, Pageable pageable);

    // Buscar mensagens entre dois usuários (baseado na conversa)
    @Query("SELECT m FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p1 " +
           "JOIN c.participantes p2 " +
           "WHERE p1.usuario.id = :usuario1Id AND p2.usuario.id = :usuario2Id " +
           "AND p1.ativo = true AND p2.ativo = true " +
           "AND c.tipo = 'INDIVIDUAL' " +
           "ORDER BY m.enviadaEm ASC")
    List<Mensagem> findMensagensEntreUsuarios(@Param("usuario1Id") Long usuario1Id, 
                                              @Param("usuario2Id") Long usuario2Id);

    // Contar mensagens não lidas de um usuário (mensagens que não são dele)
    @Query("SELECT COUNT(m) FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p " +
           "WHERE p.usuario.id = :usuarioId " +
           "AND p.ativo = true " +
           "AND m.remetente.id != :usuarioId " +
           "AND m.lida = false")
    long countMensagensNaoLidas(@Param("usuarioId") Long usuarioId);

    // Contar mensagens não lidas de um remetente específico para um destinatário
    @Query("SELECT COUNT(m) FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p " +
           "WHERE p.usuario.id = :destinatarioId " +
           "AND p.ativo = true " +
           "AND m.remetente.id = :remetenteId " +
           "AND m.lida = false")
    long countMensagensNaoLidasDeRemetente(@Param("destinatarioId") Long destinatarioId, 
                                           @Param("remetenteId") Long remetenteId);

    // Buscar mensagens não lidas de um usuário
    @Query("SELECT m FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p " +
           "WHERE p.usuario.id = :usuarioId " +
           "AND p.ativo = true " +
           "AND m.remetente.id != :usuarioId " +
           "AND m.lida = false " +
           "ORDER BY m.enviadaEm DESC")
    List<Mensagem> findMensagensNaoLidas(@Param("usuarioId") Long usuarioId);

    // Marcar todas as mensagens de uma conversa como lidas para um usuário
    @Modifying
    @Query("UPDATE Mensagem m SET m.lida = true, m.lidaEm = :dataLeitura " +
           "WHERE m.conversa.id = :conversaId " +
           "AND m.remetente.id != :usuarioId " +
           "AND m.lida = false")
    void marcarMensagensComoLidas(@Param("conversaId") Long conversaId, 
                                  @Param("usuarioId") Long usuarioId, 
                                  @Param("dataLeitura") LocalDateTime dataLeitura);

    // Buscar última mensagem de uma conversa
    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId " +
           "ORDER BY m.enviadaEm DESC LIMIT 1")
    Mensagem findUltimaMensagemDaConversa(@Param("conversaId") Long conversaId);

    // Buscar mensagens recentes (últimas 24 horas)
    @Query("SELECT m FROM Mensagem m WHERE m.enviadaEm >= :dataInicio " +
           "ORDER BY m.enviadaEm DESC")
    List<Mensagem> findMensagensRecentes(@Param("dataInicio") LocalDateTime dataInicio);

    // Buscar mensagens por tipo
    @Query("SELECT m FROM Mensagem m WHERE m.tipo = :tipo " +
           "ORDER BY m.enviadaEm DESC")
    List<Mensagem> findByTipoMensagem(@Param("tipo") Mensagem.TipoMensagem tipo);

    // Buscar conversas com mensagens não lidas para um usuário
    @Query("SELECT DISTINCT m.conversa FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p " +
           "WHERE p.usuario.id = :usuarioId " +
           "AND p.ativo = true " +
           "AND m.remetente.id != :usuarioId " +
           "AND m.lida = false")
    List<Conversa> findConversasComMensagensNaoLidas(@Param("usuarioId") Long usuarioId);

    // Buscar remetentes com mensagens não lidas para um usuário
    @Query("SELECT DISTINCT m.remetente FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p " +
           "WHERE p.usuario.id = :usuarioId " +
           "AND p.ativo = true " +
           "AND m.remetente.id != :usuarioId " +
           "AND m.lida = false")
    List<Usuario> findRemetentesComMensagensNaoLidas(@Param("usuarioId") Long usuarioId);
    
    // Buscar mensagens de uma conversa com paginação (usado no ChatService)
    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId ORDER BY m.enviadaEm DESC")
    List<Mensagem> findMensagensPorConversa(@Param("conversaId") Long conversaId, Pageable pageable);
    
    // Contar mensagens de uma conversa
    @Query("SELECT COUNT(m) FROM Mensagem m WHERE m.conversa.id = :conversaId")
    long countMensagensPorConversa(@Param("conversaId") Long conversaId);
    
    // Buscar mensagens entre dois usuários com paginação (baseado na conversa)
    @Query("SELECT m FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p1 " +
           "JOIN c.participantes p2 " +
           "WHERE p1.usuario.id = :usuario1Id AND p2.usuario.id = :usuario2Id " +
           "AND p1.ativo = true AND p2.ativo = true " +
           "AND c.tipo = 'INDIVIDUAL' " +
           "ORDER BY m.enviadaEm DESC")
    List<Mensagem> findMensagensEntreUsuarios(@Param("usuario1Id") Long usuario1Id, 
                                              @Param("usuario2Id") Long usuario2Id, 
                                              Pageable pageable);
    
    // Buscar última mensagem entre dois usuários
    @Query("SELECT m FROM Mensagem m " +
           "JOIN m.conversa c " +
           "JOIN c.participantes p1 " +
           "JOIN c.participantes p2 " +
           "WHERE p1.usuario.id = :usuario1Id AND p2.usuario.id = :usuario2Id " +
           "AND p1.ativo = true AND p2.ativo = true " +
           "AND c.tipo = 'INDIVIDUAL' " +
           "ORDER BY m.enviadaEm DESC LIMIT 1")
    Optional<Mensagem> findUltimaMensagemEntreUsuarios(@Param("usuario1Id") Long usuario1Id, 
                                                       @Param("usuario2Id") Long usuario2Id);
}