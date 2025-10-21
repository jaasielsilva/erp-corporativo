package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.CampanhaMarketing;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampanhaMarketingRepository extends JpaRepository<CampanhaMarketing, Long> {

    // ------------------------
    // PAGINAÇÃO
    // ------------------------

    /**
     * Retorna todas as campanhas paginadas, ordenadas por data de criação decrescente.
     */
    Page<CampanhaMarketing> findAll(Pageable pageable);

    /**
     * Retorna campanhas por status, paginadas e ordenadas por data de criação decrescente.
     */
    Page<CampanhaMarketing> findByStatus(CampanhaMarketing.StatusCampanha status, Pageable pageable);

    // ------------------------
    // CONSULTAS SIMPLES
    // ------------------------

    List<CampanhaMarketing> findByStatusOrderByDataCriacaoDesc(CampanhaMarketing.StatusCampanha status);

    List<CampanhaMarketing> findByTipoOrderByDataCriacaoDesc(CampanhaMarketing.TipoCampanha tipo);

    List<CampanhaMarketing> findByObjetivoOrderByDataCriacaoDesc(CampanhaMarketing.ObjetivoCampanha objetivo);

    List<CampanhaMarketing> findByUsuarioResponsavelOrderByDataCriacaoDesc(Usuario usuario);

    List<CampanhaMarketing> findByDataInicioBetweenOrderByDataInicio(LocalDate inicio, LocalDate fim);

    List<CampanhaMarketing> findByDataFimBetweenOrderByDataFim(LocalDate inicio, LocalDate fim);

    // ------------------------
    // CONSULTAS COMPLEXAS COM @Query
    // ------------------------

    /**
     * Campanhas ativas atualmente (data início <= hoje e data fim >= hoje ou null) com status EM_ANDAMENTO.
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.dataInicio <= :hoje AND (c.dataFim IS NULL OR c.dataFim >= :hoje) AND c.status = 'EM_ANDAMENTO' ORDER BY c.dataInicio")
    List<CampanhaMarketing> findCampanhasAtivas(@Param("hoje") LocalDate hoje);

    /**
     * Campanhas expiradas (data fim < hoje e ainda não finalizadas).
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.dataFim < :hoje AND c.status != 'FINALIZADA' ORDER BY c.dataFim")
    List<CampanhaMarketing> findCampanhasExpiradas(@Param("hoje") LocalDate hoje);

    /**
     * Campanhas agendadas para iniciar amanhã.
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.dataInicio = :amanha AND c.status = 'AGENDADA' ORDER BY c.nome")
    List<CampanhaMarketing> findCampanhasParaIniciarAmanha(@Param("amanha") LocalDate amanha);

    /**
     * Contagem de campanhas por status.
     */
    @Query("SELECT COUNT(c) FROM CampanhaMarketing c WHERE c.status = :status")
    Long countByStatus(@Param("status") CampanhaMarketing.StatusCampanha status);

    /**
     * Soma do orçamento planejado por status.
     */
    @Query("SELECT SUM(c.orcamentoPlanejado) FROM CampanhaMarketing c WHERE c.status = :status")
    BigDecimal sumOrcamentoPlanejadoByStatus(@Param("status") CampanhaMarketing.StatusCampanha status);

    /**
     * Soma do orçamento gasto de campanhas em andamento ou finalizadas.
     */
    @Query("SELECT SUM(c.orcamentoGasto) FROM CampanhaMarketing c WHERE c.status IN ('EM_ANDAMENTO', 'FINALIZADA')")
    BigDecimal sumOrcamentoGasto();

    /**
     * Soma da receita gerada de campanhas finalizadas entre um período.
     */
    @Query("SELECT SUM(c.receitaGerada) FROM CampanhaMarketing c WHERE c.status = 'FINALIZADA' AND c.dataInicio BETWEEN :inicio AND :fim")
    BigDecimal sumReceitaGeradaByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    /**
     * Contagem de campanhas agrupadas por tipo.
     */
    @Query("SELECT c.tipo, COUNT(c) FROM CampanhaMarketing c GROUP BY c.tipo ORDER BY COUNT(c) DESC")
    List<Object[]> countByTipo();

    /**
     * Contagem de campanhas agrupadas por objetivo.
     */
    @Query("SELECT c.objetivo, COUNT(c) FROM CampanhaMarketing c GROUP BY c.objetivo ORDER BY COUNT(c) DESC")
    List<Object[]> countByObjetivo();

    /**
     * Contagem de campanhas agrupadas por status.
     */
    @Query("SELECT c.status, COUNT(c) FROM CampanhaMarketing c GROUP BY c.status ORDER BY COUNT(c) DESC")
    List<Object[]> countByStatusDetalhado();

    /**
     * Quantidade de campanhas criadas por dia em um intervalo.
     */
    @Query("SELECT DATE(c.dataCriacao), COUNT(c) FROM CampanhaMarketing c WHERE c.dataCriacao BETWEEN :inicio AND :fim GROUP BY DATE(c.dataCriacao) ORDER BY DATE(c.dataCriacao)")
    List<Object[]> getCampanhasCriadasPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    /**
     * Quantidade de campanhas criadas por mês em um intervalo.
     */
    @Query("SELECT YEAR(c.dataCriacao), MONTH(c.dataCriacao), COUNT(c) FROM CampanhaMarketing c WHERE c.dataCriacao BETWEEN :inicio AND :fim GROUP BY YEAR(c.dataCriacao), MONTH(c.dataCriacao) ORDER BY YEAR(c.dataCriacao), MONTH(c.dataCriacao)")
    List<Object[]> getCampanhasCriadasPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    /**
     * Campanhas com receita > 0, ordenadas da mais lucrativa.
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.receitaGerada > 0 ORDER BY c.receitaGerada DESC")
    List<CampanhaMarketing> findCampanhasMaisLucrativas();

    /**
     * Campanhas com conversões > 0, ordenadas pelas que mais converteram.
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.conversoes > 0 ORDER BY c.conversoes DESC")
    List<CampanhaMarketing> findCampanhasComMaisConversoes();

    /**
     * Média da taxa de conversão das campanhas finalizadas.
     */
    @Query("SELECT AVG(c.conversoes * 100.0 / NULLIF(c.cliques, 0)) FROM CampanhaMarketing c WHERE c.cliques > 0 AND c.status = 'FINALIZADA'")
    Double getMediaTaxaConversao();

    /**
     * Média do CTR (cliques / alcance efetivo) das campanhas finalizadas.
     */
    @Query("SELECT AVG(c.cliques * 100.0 / NULLIF(c.alcanceEfetivo, 0)) FROM CampanhaMarketing c WHERE c.alcanceEfetivo > 0 AND c.status = 'FINALIZADA'")
    Double getMediaCTR();

    /**
     * Média do custo de aquisição por conversão.
     */
    @Query("SELECT AVG(c.orcamentoGasto / NULLIF(c.conversoes, 0)) FROM CampanhaMarketing c WHERE c.conversoes > 0 AND c.status = 'FINALIZADA'")
    BigDecimal getMediaCustoAquisicao();

    /**
     * Pesquisar campanhas por nome (like %nome%).
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.nome LIKE %:nome% ORDER BY c.dataCriacao DESC")
    List<CampanhaMarketing> findByNomeContaining(@Param("nome") String nome);

    /**
     * Pesquisar campanhas por tipo e status.
     */
    @Query("SELECT c FROM CampanhaMarketing c WHERE c.tipo = :tipo AND c.status = :status ORDER BY c.dataCriacao DESC")
    List<CampanhaMarketing> findByTipoAndStatus(@Param("tipo") CampanhaMarketing.TipoCampanha tipo, @Param("status") CampanhaMarketing.StatusCampanha status);

    /**
     * Verifica se já existe uma campanha com o nome informado.
     */
    boolean existsByNome(String nome);

    // ------------------------
    // RELATÓRIOS E PERFORMANCE
    // ------------------------

    /**
     * Estatísticas gerais de campanhas em um período.
     */
    @Query("SELECT " +
           "COUNT(c) as totalCampanhas, " +
           "SUM(c.orcamentoGasto) as custoTotal, " +
           "SUM(c.receitaGerada) as receitaTotal, " +
           "SUM(c.conversoes) as totalConversoes, " +
           "SUM(c.vendas) as totalVendas, " +
           "AVG(c.conversoes * 100.0 / NULLIF(c.cliques, 0)) as taxaConversaoMedia " +
           "FROM CampanhaMarketing c " +
           "WHERE c.dataInicio BETWEEN :inicio AND :fim")
    List<Object[]> getEstatisticasGerais(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    /**
     * Performance de cada usuário em campanhas no período.
     */
    @Query("SELECT c.usuarioResponsavel, COUNT(c), SUM(c.receitaGerada), AVG(c.conversoes * 100.0 / NULLIF(c.cliques, 0)) " +
           "FROM CampanhaMarketing c " +
           "WHERE c.usuarioResponsavel IS NOT NULL AND c.dataInicio BETWEEN :inicio AND :fim " +
           "GROUP BY c.usuarioResponsavel " +
           "ORDER BY SUM(c.receitaGerada) DESC")
    List<Object[]> getPerformancePorUsuario(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    /**
     * Performance de campanhas por tipo em um período.
     */
    @Query("SELECT c.tipo, " +
           "COUNT(c) as quantidade, " +
           "SUM(c.orcamentoGasto) as custoTotal, " +
           "SUM(c.receitaGerada) as receitaTotal, " +
           "AVG(c.conversoes * 100.0 / NULLIF(c.cliques, 0)) as taxaConversao " +
           "FROM CampanhaMarketing c " +
           "WHERE c.dataInicio BETWEEN :inicio AND :fim " +
           "GROUP BY c.tipo " +
           "ORDER BY SUM(c.receitaGerada) DESC")
    List<Object[]> getPerformancePorTipo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

}
