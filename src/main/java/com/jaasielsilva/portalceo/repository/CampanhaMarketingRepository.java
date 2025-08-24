package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.CampanhaMarketing;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampanhaMarketingRepository extends JpaRepository<CampanhaMarketing, Long> {

    List<CampanhaMarketing> findByStatusOrderByDataCriacaoDesc(CampanhaMarketing.StatusCampanha status);

    List<CampanhaMarketing> findByTipoOrderByDataCriacaoDesc(CampanhaMarketing.TipoCampanha tipo);

    List<CampanhaMarketing> findByObjetivoOrderByDataCriacaoDesc(CampanhaMarketing.ObjetivoCampanha objetivo);

    List<CampanhaMarketing> findByUsuarioResponsavelOrderByDataCriacaoDesc(Usuario usuario);

    List<CampanhaMarketing> findByDataInicioBetweenOrderByDataInicio(LocalDate inicio, LocalDate fim);

    List<CampanhaMarketing> findByDataFimBetweenOrderByDataFim(LocalDate inicio, LocalDate fim);

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.dataInicio <= :hoje AND (c.dataFim IS NULL OR c.dataFim >= :hoje) AND c.status = 'EM_ANDAMENTO' ORDER BY c.dataInicio")
    List<CampanhaMarketing> findCampanhasAtivas(@Param("hoje") LocalDate hoje);

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.dataFim < :hoje AND c.status != 'FINALIZADA' ORDER BY c.dataFim")
    List<CampanhaMarketing> findCampanhasExpiradas(@Param("hoje") LocalDate hoje);

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.dataInicio = :amanha AND c.status = 'AGENDADA' ORDER BY c.nome")
    List<CampanhaMarketing> findCampanhasParaIniciarAmanha(@Param("amanha") LocalDate amanha);

    @Query("SELECT COUNT(c) FROM CampanhaMarketing c WHERE c.status = :status")
    Long countByStatus(@Param("status") CampanhaMarketing.StatusCampanha status);

    @Query("SELECT SUM(c.orcamentoPlanejado) FROM CampanhaMarketing c WHERE c.status = :status")
    BigDecimal sumOrcamentoPlanejadoByStatus(@Param("status") CampanhaMarketing.StatusCampanha status);

    @Query("SELECT SUM(c.orcamentoGasto) FROM CampanhaMarketing c WHERE c.status IN ('EM_ANDAMENTO', 'FINALIZADA')")
    BigDecimal sumOrcamentoGasto();

    @Query("SELECT SUM(c.receitaGerada) FROM CampanhaMarketing c WHERE c.status = 'FINALIZADA' AND c.dataInicio BETWEEN :inicio AND :fim")
    BigDecimal sumReceitaGeradaByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c.tipo, COUNT(c) FROM CampanhaMarketing c GROUP BY c.tipo ORDER BY COUNT(c) DESC")
    List<Object[]> countByTipo();

    @Query("SELECT c.objetivo, COUNT(c) FROM CampanhaMarketing c GROUP BY c.objetivo ORDER BY COUNT(c) DESC")
    List<Object[]> countByObjetivo();

    @Query("SELECT c.status, COUNT(c) FROM CampanhaMarketing c GROUP BY c.status ORDER BY COUNT(c) DESC")
    List<Object[]> countByStatusDetalhado();

    @Query("SELECT DATE(c.dataCriacao), COUNT(c) FROM CampanhaMarketing c WHERE c.dataCriacao BETWEEN :inicio AND :fim GROUP BY DATE(c.dataCriacao) ORDER BY DATE(c.dataCriacao)")
    List<Object[]> getCampanhasCriadasPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT YEAR(c.dataCriacao), MONTH(c.dataCriacao), COUNT(c) FROM CampanhaMarketing c WHERE c.dataCriacao BETWEEN :inicio AND :fim GROUP BY YEAR(c.dataCriacao), MONTH(c.dataCriacao) ORDER BY YEAR(c.dataCriacao), MONTH(c.dataCriacao)")
    List<Object[]> getCampanhasCriadasPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.receitaGerada > 0 ORDER BY c.receitaGerada DESC")
    List<CampanhaMarketing> findCampanhasMaisLucrativas();

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.conversoes > 0 ORDER BY c.conversoes DESC")
    List<CampanhaMarketing> findCampanhasComMaisConversoes();

    @Query("SELECT AVG(c.conversoes * 100.0 / NULLIF(c.cliques, 0)) FROM CampanhaMarketing c WHERE c.cliques > 0 AND c.status = 'FINALIZADA'")
    Double getMediaTaxaConversao();

    @Query("SELECT AVG(c.cliques * 100.0 / NULLIF(c.alcanceEfetivo, 0)) FROM CampanhaMarketing c WHERE c.alcanceEfetivo > 0 AND c.status = 'FINALIZADA'")
    Double getMediaCTR();

    @Query("SELECT AVG(c.orcamentoGasto / NULLIF(c.conversoes, 0)) FROM CampanhaMarketing c WHERE c.conversoes > 0 AND c.status = 'FINALIZADA'")
    BigDecimal getMediaCustoAquisicao();

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.nome LIKE %:nome% ORDER BY c.dataCriacao DESC")
    List<CampanhaMarketing> findByNomeContaining(@Param("nome") String nome);

    @Query("SELECT c FROM CampanhaMarketing c WHERE c.tipo = :tipo AND c.status = :status ORDER BY c.dataCriacao DESC")
    List<CampanhaMarketing> findByTipoAndStatus(@Param("tipo") CampanhaMarketing.TipoCampanha tipo, @Param("status") CampanhaMarketing.StatusCampanha status);

    // Performance Analysis
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

    @Query("SELECT c.usuarioResponsavel, COUNT(c), SUM(c.receitaGerada), AVG(c.conversoes * 100.0 / NULLIF(c.cliques, 0)) " +
           "FROM CampanhaMarketing c " +
           "WHERE c.usuarioResponsavel IS NOT NULL AND c.dataInicio BETWEEN :inicio AND :fim " +
           "GROUP BY c.usuarioResponsavel " +
           "ORDER BY SUM(c.receitaGerada) DESC")
    List<Object[]> getPerformancePorUsuario(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

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

    boolean existsByNome(String nome);
}