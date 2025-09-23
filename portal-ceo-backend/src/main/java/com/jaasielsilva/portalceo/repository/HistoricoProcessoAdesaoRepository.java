package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.HistoricoProcessoAdesao;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoProcessoAdesaoRepository extends JpaRepository<HistoricoProcessoAdesao, Long> {
    
    /**
     * Busca histórico por processo de adesão ordenado por data
     */
    List<HistoricoProcessoAdesao> findByProcessoAdesaoOrderByDataEventoDesc(ProcessoAdesao processoAdesao);
    
    /**
     * Busca histórico por processo de adesão (ID) ordenado por data
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.processoAdesao.id = :processoId ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findByProcessoAdesaoIdOrderByDataEventoDesc(@Param("processoId") Long processoId);
    
    /**
     * Busca histórico por tipo de evento
     */
    List<HistoricoProcessoAdesao> findByTipoEventoOrderByDataEventoDesc(HistoricoProcessoAdesao.TipoEvento tipoEvento);
    
    /**
     * Busca histórico por processo e tipo de evento
     */
    List<HistoricoProcessoAdesao> findByProcessoAdesaoAndTipoEventoOrderByDataEventoDesc(
        ProcessoAdesao processoAdesao, 
        HistoricoProcessoAdesao.TipoEvento tipoEvento
    );
    
    /**
     * Busca histórico por usuário responsável
     */
    List<HistoricoProcessoAdesao> findByUsuarioResponsavelOrderByDataEventoDesc(String usuarioResponsavel);
    
    /**
     * Busca histórico por período
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.dataEvento BETWEEN :dataInicio AND :dataFim ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);
    
    /**
     * Busca último evento de um processo
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.processoAdesao = :processo ORDER BY h.dataEvento DESC LIMIT 1")
    HistoricoProcessoAdesao findUltimoEventoPorProcesso(@Param("processo") ProcessoAdesao processo);
    
    /**
     * Busca eventos de aprovação/rejeição
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.tipoEvento IN ('APROVACAO', 'REJEICAO') ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findEventosAprovacaoRejeicao();
    
    /**
     * Conta eventos por tipo
     */
    @Query("SELECT h.tipoEvento, COUNT(h) FROM HistoricoProcessoAdesao h GROUP BY h.tipoEvento")
    List<Object[]> contarEventosPorTipo();
    
    /**
     * Busca eventos de mudança de status
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.tipoEvento = 'MUDANCA_STATUS' AND h.statusAtual = :status ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findMudancasParaStatus(@Param("status") ProcessoAdesao.StatusProcesso status);
    
    /**
     * Busca eventos de mudança de etapa
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.tipoEvento = 'MUDANCA_ETAPA' AND h.etapaAtual = :etapa ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findMudancasParaEtapa(@Param("etapa") String etapa);
    
    /**
     * Busca histórico com observações
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.observacoes IS NOT NULL AND h.observacoes != '' ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findEventosComObservacoes();
    
    /**
     * Busca eventos por processo e período
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.processoAdesao = :processo AND h.dataEvento BETWEEN :dataInicio AND :dataFim ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findByProcessoAndPeriodo(
        @Param("processo") ProcessoAdesao processo,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );
    
    /**
     * Busca eventos recentes (últimas 24 horas)
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.dataEvento >= :dataLimite ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findEventosRecentes(@Param("dataLimite") LocalDateTime dataLimite);
    
    /**
     * Busca estatísticas de eventos por usuário
     */
    @Query("SELECT h.usuarioResponsavel, h.tipoEvento, COUNT(h) FROM HistoricoProcessoAdesao h WHERE h.usuarioResponsavel IS NOT NULL GROUP BY h.usuarioResponsavel, h.tipoEvento ORDER BY h.usuarioResponsavel")
    List<Object[]> getEstatisticasPorUsuario();
    
    /**
     * Remove histórico antigo (mais de 1 ano)
     */
    @Query("DELETE FROM HistoricoProcessoAdesao h WHERE h.dataEvento < :dataLimite")
    void removeHistoricoAntigo(@Param("dataLimite") LocalDateTime dataLimite);
    
    /**
     * Busca primeiro evento de um processo
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.processoAdesao = :processo ORDER BY h.dataEvento ASC LIMIT 1")
    HistoricoProcessoAdesao findPrimeiroEventoPorProcesso(@Param("processo") ProcessoAdesao processo);
    
    /**
     * Busca eventos de um processo por sessionId
     */
    @Query("SELECT h FROM HistoricoProcessoAdesao h WHERE h.processoAdesao.sessionId = :sessionId ORDER BY h.dataEvento DESC")
    List<HistoricoProcessoAdesao> findBySessionId(@Param("sessionId") String sessionId);

    // Buscar histórico pelo id do processo
    List<HistoricoProcessoAdesao> findByProcessoAdesaoId(Long processoId);
}