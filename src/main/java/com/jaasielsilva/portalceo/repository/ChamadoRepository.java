package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    // Buscar chamado por número
    Optional<Chamado> findByNumero(String numero);

    // Buscar chamados por status
    List<Chamado> findByStatus(StatusChamado status);

    // Buscar chamados por prioridade
    List<Chamado> findByPrioridade(Prioridade prioridade);

    // Buscar chamados por técnico responsável
    List<Chamado> findByTecnicoResponsavel(String tecnicoResponsavel);

    // Buscar chamados abertos
    @Query("SELECT c FROM Chamado c WHERE c.status = 'ABERTO' ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosAbertos();

    // Buscar chamados em andamento
    @Query("SELECT c FROM Chamado c WHERE c.status = 'EM_ANDAMENTO' ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosEmAndamento();

    // Buscar chamados resolvidos
    @Query("SELECT c FROM Chamado c WHERE c.status IN ('RESOLVIDO', 'FECHADO') ORDER BY c.dataResolucao DESC")
    List<Chamado> findChamadosResolvidos();
    
    // Buscar chamados que foram avaliados
    @Query("SELECT c FROM Chamado c WHERE c.avaliacao IS NOT NULL ORDER BY c.dataResolucao DESC")
    List<Chamado> findChamadosAvaliados();

    // Contar chamados por status
    long countByStatus(StatusChamado status);

    // Contar chamados por prioridade
    long countByPrioridade(Prioridade prioridade);

    // Buscar chamados por colaborador responsável
    List<Chamado> findByColaboradorResponsavelId(Long colaboradorId);

    // Buscar chamados ativos por colaborador responsável
    @Query("SELECT c FROM Chamado c WHERE c.colaboradorResponsavel.id = :colaboradorId AND c.status IN ('ABERTO', 'EM_ANDAMENTO')")
    List<Chamado> findChamadosAtivosByColaborador(@Param("colaboradorId") Long colaboradorId);

    // Contar chamados ativos por colaborador
    @Query("SELECT COUNT(c) FROM Chamado c WHERE c.colaboradorResponsavel.id = :colaboradorId AND c.status IN ('ABERTO', 'EM_ANDAMENTO')")
    int countChamadosAtivosByColaborador(@Param("colaboradorId") Long colaboradorId);

    // Buscar chamados sem colaborador atribuído
    @Query("SELECT c FROM Chamado c WHERE c.colaboradorResponsavel IS NULL AND c.status = 'ABERTO' ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosSemAtribuicao();

    // Buscar chamados próximos do vencimento do SLA
    @Query("SELECT c FROM Chamado c WHERE c.slaVencimento IS NOT NULL AND c.slaVencimento <= :dataLimite AND c.status IN ('ABERTO', 'EM_ANDAMENTO')")
    List<Chamado> findChamadosProximosVencimentoSla(@Param("dataLimite") LocalDateTime dataLimite);

    // Query para calcular tempo médio de resolução (em horas)
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, c.dataAbertura, c.dataResolucao)) " +
           "FROM Chamado c " +
           "WHERE c.status IN ('RESOLVIDO', 'FECHADO') " +
           "AND c.dataResolucao IS NOT NULL")
    Double calcularTempoMedioResolucaoEmHoras();

    @Query("SELECT AVG(c.avaliacao) FROM Chamado c WHERE c.avaliacao IS NOT NULL")
    Double calcularAvaliacaoMedia();

    // Query para buscar chamados resolvidos com tempo de resolução
    @Query("SELECT c, TIMESTAMPDIFF(HOUR, c.dataAbertura, c.dataResolucao) as tempoResolucao " +
           "FROM Chamado c " +
           "WHERE c.status IN ('RESOLVIDO', 'FECHADO') " +
           "AND c.dataResolucao IS NOT NULL " +
           "ORDER BY c.dataResolucao DESC")
    List<Object[]> findChamadosResolvidosComTempo();

    // Buscar chamados por período
    @Query("SELECT c FROM Chamado c " +
           "WHERE c.dataAbertura BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY c.dataAbertura DESC")
    List<Chamado> findByPeriodo(@Param("dataInicio") LocalDateTime dataInicio, 
                               @Param("dataFim") LocalDateTime dataFim);
    
    // Contar chamados por período (para evolução mensal)
    @Query("SELECT COUNT(c) FROM Chamado c " +
           "WHERE c.dataAbertura BETWEEN :dataInicio AND :dataFim")
    Long countByDataAberturaBetween(@Param("dataInicio") LocalDateTime dataInicio, 
                                   @Param("dataFim") LocalDateTime dataFim);

    // Buscar chamados próximos ao vencimento do SLA
    @Query("SELECT c FROM Chamado c " +
           "WHERE c.status IN ('ABERTO', 'EM_ANDAMENTO') " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosProximosVencimento();

    // Estatísticas por prioridade
    @Query("SELECT c.prioridade, COUNT(c) " +
           "FROM Chamado c " +
           "GROUP BY c.prioridade " +
           "ORDER BY c.prioridade DESC")
    List<Object[]> countByPrioridadeGrouped();

    // Estatísticas por status
    @Query("SELECT c.status, COUNT(c) " +
           "FROM Chamado c " +
           "GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    // Estatísticas por categoria
    @Query("SELECT c.categoria, COUNT(c) " +
           "FROM Chamado c " +
           "WHERE c.categoria IS NOT NULL " +
           "GROUP BY c.categoria " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> countByCategoriaGrouped();

    // Buscar chamados do mês atual
    @Query("SELECT c FROM Chamado c " +
           "WHERE YEAR(c.dataAbertura) = YEAR(CURRENT_DATE) " +
           "AND MONTH(c.dataAbertura) = MONTH(CURRENT_DATE) " +
           "ORDER BY c.dataAbertura DESC")
    List<Chamado> findChamadosDoMesAtual();

    // Buscar chamados da semana atual
    @Query("SELECT c FROM Chamado c " +
           "WHERE YEARWEEK(c.dataAbertura, 1) = YEARWEEK(CURRENT_DATE, 1) " +
           "ORDER BY c.dataAbertura DESC")
    List<Chamado> findChamadosDaSemanaAtual();

    // Buscar últimos chamados (para dashboard)
    @Query("SELECT c FROM Chamado c " +
           "ORDER BY c.dataAbertura DESC")
    List<Chamado> findUltimosChamados();

    // Buscar chamados por categoria
    List<Chamado> findByCategoria(String categoria);

    // Buscar chamados por solicitante
    List<Chamado> findBySolicitanteEmailOrderByDataAberturaDesc(String email);
    
    // Buscar chamados por colaborador responsável
    List<Chamado> findByColaboradorResponsavel(com.jaasielsilva.portalceo.model.Colaborador colaborador);
    
    // Contar chamados por colaborador responsável e status
    int countByColaboradorResponsavelAndStatusIn(com.jaasielsilva.portalceo.model.Colaborador colaborador, List<StatusChamado> status);
    
    // Buscar chamados por colaborador responsável e status
    List<Chamado> findByColaboradorResponsavelAndStatusIn(com.jaasielsilva.portalceo.model.Colaborador colaborador, List<StatusChamado> status);
    
    // Buscar chamados fechados por período
    List<Chamado> findByStatusAndDataFechamentoBetween(StatusChamado status, LocalDateTime dataInicio, LocalDateTime dataFim);

    // Query para dashboard - contadores
    @Query("SELECT " +
           "SUM(CASE WHEN c.status = 'ABERTO' THEN 1 ELSE 0 END) as abertos, " +
           "SUM(CASE WHEN c.status = 'EM_ANDAMENTO' THEN 1 ELSE 0 END) as emAndamento, " +
           "SUM(CASE WHEN c.status = 'RESOLVIDO' THEN 1 ELSE 0 END) as resolvidos, " +
           "SUM(CASE WHEN c.status = 'FECHADO' THEN 1 ELSE 0 END) as fechados " +
           "FROM Chamado c")
    Object[] getEstatisticasDashboard();

    // Buscar chamados com SLA próximo do vencimento (últimas 4 horas)
    @Query("SELECT c FROM Chamado c " +
           "WHERE c.status IN ('ABERTO', 'EM_ANDAMENTO') " +
           "AND (" +
           "  (c.prioridade = 'URGENTE' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 4) OR " +
           "  (c.prioridade = 'ALTA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 20) OR " +
           "  (c.prioridade = 'MEDIA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 44) OR " +
           "  (c.prioridade = 'BAIXA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 68)" +
           ") " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosComSlaProximoVencimento();

    // Buscar chamados com SLA vencido
    @Query("SELECT c FROM Chamado c " +
           "WHERE c.status IN ('ABERTO', 'EM_ANDAMENTO') " +
           "AND (" +
           "  (c.prioridade = 'URGENTE' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) > 8) OR " +
           "  (c.prioridade = 'ALTA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) > 24) OR " +
           "  (c.prioridade = 'MEDIA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) > 48) OR " +
           "  (c.prioridade = 'BAIXA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) > 72)" +
           ") " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosComSlaVencido();

    // ==================== MÉTODOS COM FETCH JOIN PARA EVITAR N+1 ====================
    
    /**
     * Buscar todos os chamados com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "ORDER BY c.dataAbertura DESC")
    List<Chamado> findAllWithColaborador();
    
    /**
     * Buscar chamado por ID com colaborador responsável (fetch join)
     */
    @Query("SELECT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.id = :id")
    Optional<Chamado> findByIdWithColaborador(@Param("id") Long id);
    
    /**
     * Buscar chamado por número com colaborador responsável (fetch join)
     */
    @Query("SELECT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.numero = :numero")
    Optional<Chamado> findByNumeroWithColaborador(@Param("numero") String numero);
    
    /**
     * Buscar chamados abertos com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.status = 'ABERTO' " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosAbertosWithColaborador();
    
    /**
     * Buscar chamados em andamento com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.status = 'EM_ANDAMENTO' " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosEmAndamentoWithColaborador();
    
    /**
     * Buscar chamados resolvidos com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.status IN ('RESOLVIDO', 'FECHADO') " +
           "ORDER BY c.dataResolucao DESC")
    List<Chamado> findChamadosResolvidosWithColaborador();
    
    /**
     * Buscar chamados por status com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.status = :status " +
           "ORDER BY c.dataAbertura DESC")
    List<Chamado> findByStatusWithColaborador(@Param("status") StatusChamado status);
    
    /**
     * Buscar chamados sem atribuição com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.colaboradorResponsavel IS NULL AND c.status = 'ABERTO' " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosSemAtribuicaoWithColaborador();
    
    /**
     * Buscar chamados com SLA próximo do vencimento com colaborador responsável (fetch join)
     */
    @Query("SELECT DISTINCT c FROM Chamado c " +
           "LEFT JOIN FETCH c.colaboradorResponsavel col " +
           "LEFT JOIN FETCH col.cargo " +
           "LEFT JOIN FETCH col.departamento " +
           "WHERE c.status IN ('ABERTO', 'EM_ANDAMENTO') " +
           "AND (" +
           "  (c.prioridade = 'URGENTE' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 4) OR " +
           "  (c.prioridade = 'ALTA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 20) OR " +
           "  (c.prioridade = 'MEDIA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 44) OR " +
           "  (c.prioridade = 'BAIXA' AND TIMESTAMPDIFF(HOUR, c.dataAbertura, NOW()) >= 68)" +
           ") " +
           "ORDER BY c.prioridade DESC, c.dataAbertura ASC")
    List<Chamado> findChamadosComSlaProximoVencimentoWithColaborador();
}