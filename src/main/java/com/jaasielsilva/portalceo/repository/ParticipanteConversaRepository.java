package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ParticipanteConversa;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteConversaRepository extends JpaRepository<ParticipanteConversa, Long> {

    // Buscar participantes ativos de uma conversa
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true")
    List<ParticipanteConversa> findParticipantesAtivosByConversaId(@Param("conversaId") Long conversaId);

    // Buscar todos os participantes de uma conversa (ativos e inativos)
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId")
    List<ParticipanteConversa> findAllParticipantesByConversaId(@Param("conversaId") Long conversaId);

    // Buscar conversas de um usuário
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.ativo = true ORDER BY p.conversa.ultimaAtividade DESC")
    List<ParticipanteConversa> findConversasDoUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar participante específico
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId")
    Optional<ParticipanteConversa> findByUsuarioIdAndConversaId(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId);

    // Buscar participante ativo específico
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId AND p.ativo = true")
    Optional<ParticipanteConversa> findParticipanteAtivoByUsuarioIdAndConversaId(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId);

    // Contar participantes ativos de uma conversa
    @Query("SELECT COUNT(p) FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true")
    Long countParticipantesAtivosByConversaId(@Param("conversaId") Long conversaId);

    // Buscar administradores de uma conversa
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true AND (p.tipo = 'ADMINISTRADOR' OR p.tipo = 'CRIADOR')")
    List<ParticipanteConversa> findAdministradoresByConversaId(@Param("conversaId") Long conversaId);

    // Verificar se usuário é administrador
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId AND p.ativo = true AND (p.tipo = 'ADMINISTRADOR' OR p.tipo = 'CRIADOR')")
    boolean isUsuarioAdministrador(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId);

    // Remover participante (marcar como inativo)
    @Modifying
    @Query("UPDATE ParticipanteConversa p SET p.ativo = false, p.removidoEm = :dataRemocao WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId")
    void removerParticipante(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId, @Param("dataRemocao") LocalDateTime dataRemocao);

    // Reativar participante
    @Modifying
    @Query("UPDATE ParticipanteConversa p SET p.ativo = true, p.removidoEm = null WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId")
    void reativarParticipante(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId);

    // Atualizar última visualização
    @Modifying
    @Query("UPDATE ParticipanteConversa p SET p.ultimaVisualizacao = :dataVisualizacao WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId")
    void atualizarUltimaVisualizacao(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId, @Param("dataVisualizacao") LocalDateTime dataVisualizacao);

    // Buscar participantes com notificações ativas
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true AND p.notificacoesAtivas = true")
    List<ParticipanteConversa> findParticipantesComNotificacoesAtivas(@Param("conversaId") Long conversaId);

    // Alterar configuração de notificações
    @Modifying
    @Query("UPDATE ParticipanteConversa p SET p.notificacoesAtivas = :ativas WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId")
    void alterarNotificacoes(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId, @Param("ativas") boolean ativas);

    // Promover a administrador
    @Modifying
    @Query("UPDATE ParticipanteConversa p SET p.tipo = 'ADMINISTRADOR' WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId AND p.tipo != 'CRIADOR'")
    void promoverAAdministrador(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId);

    // Rebaixar a membro
    @Modifying
    @Query("UPDATE ParticipanteConversa p SET p.tipo = 'MEMBRO' WHERE p.usuario.id = :usuarioId AND p.conversa.id = :conversaId AND p.tipo != 'CRIADOR'")
    void rebaixarAMembro(@Param("usuarioId") Long usuarioId, @Param("conversaId") Long conversaId);

    // Buscar conversas onde usuário é administrador
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.ativo = true AND (p.tipo = 'ADMINISTRADOR' OR p.tipo = 'CRIADOR')")
    List<ParticipanteConversa> findConversasOndeUsuarioEhAdmin(@Param("usuarioId") Long usuarioId);

    // Buscar participantes recém adicionados
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.adicionadoEm >= :dataInicio ORDER BY p.adicionadoEm DESC")
    List<ParticipanteConversa> findParticipantesRecentementeAdicionados(@Param("conversaId") Long conversaId, @Param("dataInicio") LocalDateTime dataInicio);

    // Buscar participantes recém removidos
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = false AND p.removidoEm >= :dataInicio ORDER BY p.removidoEm DESC")
    List<ParticipanteConversa> findParticipantesRecentementeRemovidos(@Param("conversaId") Long conversaId, @Param("dataInicio") LocalDateTime dataInicio);

    // Contar conversas de um usuário
    @Query("SELECT COUNT(p) FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.ativo = true")
    Long countConversasDoUsuario(@Param("usuarioId") Long usuarioId);

    // Buscar participantes por tipo
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true AND p.tipo = :tipo")
    List<ParticipanteConversa> findParticipantesByTipo(@Param("conversaId") Long conversaId, @Param("tipo") ParticipanteConversa.TipoParticipante tipo);

    // Buscar conversas com atividade recente
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.usuario.id = :usuarioId AND p.ativo = true AND p.conversa.ultimaAtividade >= :dataInicio ORDER BY p.conversa.ultimaAtividade DESC")
    List<ParticipanteConversa> findConversasComAtividadeRecente(@Param("usuarioId") Long usuarioId, @Param("dataInicio") LocalDateTime dataInicio);

    // Buscar participantes que não visualizaram mensagens
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true AND (p.ultimaVisualizacao IS NULL OR p.ultimaVisualizacao < :dataUltimaMensagem)")
    List<ParticipanteConversa> findParticipantesComMensagensNaoVisualizadas(@Param("conversaId") Long conversaId, @Param("dataUltimaMensagem") LocalDateTime dataUltimaMensagem);

    // Buscar participantes com visualização desatualizada
    @Query("SELECT p FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true AND p.ultimaVisualizacao < :dataLimite")
    List<ParticipanteConversa> findParticipantesComVisualizacaoDesatualizada(@Param("conversaId") Long conversaId, @Param("dataLimite") LocalDateTime dataLimite);

    // Limpar participantes inativos antigos
    @Modifying
    @Query("DELETE FROM ParticipanteConversa p WHERE p.ativo = false AND p.removidoEm < :dataLimite")
    void limparParticipantesInativosAntigos(@Param("dataLimite") LocalDateTime dataLimite);

    // Estatísticas de participação
    @Query("SELECT COUNT(p), p.tipo FROM ParticipanteConversa p WHERE p.conversa.id = :conversaId AND p.ativo = true GROUP BY p.tipo")
    List<Object[]> getEstatisticasParticipacao(@Param("conversaId") Long conversaId);

    // Buscar conversas em comum entre dois usuários
    @Query("SELECT p1.conversa FROM ParticipanteConversa p1 JOIN ParticipanteConversa p2 ON p1.conversa.id = p2.conversa.id WHERE p1.usuario.id = :usuario1Id AND p2.usuario.id = :usuario2Id AND p1.ativo = true AND p2.ativo = true")
    List<Conversa> findConversasEmComum(@Param("usuario1Id") Long usuario1Id, @Param("usuario2Id") Long usuario2Id);
}