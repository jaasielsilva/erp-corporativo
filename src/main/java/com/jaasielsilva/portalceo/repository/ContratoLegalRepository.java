package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoLegalRepository extends JpaRepository<ContratoLegal, Long> {

    Optional<ContratoLegal> findByNumeroContrato(String numeroContrato);

    List<ContratoLegal> findByStatusOrderByDataCriacaoDesc(ContratoLegal.StatusContrato status);

    List<ContratoLegal> findByTipoOrderByDataCriacaoDesc(ContratoLegal.TipoContrato tipo);

    List<ContratoLegal> findByPrioridadeOrderByDataCriacaoDesc(ContratoLegal.PrioridadeContrato prioridade);

    List<ContratoLegal> findByClienteOrderByDataCriacaoDesc(Cliente cliente);

    List<ContratoLegal> findByFornecedorOrderByDataCriacaoDesc(Fornecedor fornecedor);

    List<ContratoLegal> findByUsuarioResponsavelOrderByDataCriacaoDesc(Usuario usuario);

    List<ContratoLegal> findByDataInicioBetweenOrderByDataInicio(LocalDate inicio, LocalDate fim);

    List<ContratoLegal> findByDataFimBetweenOrderByDataFim(LocalDate inicio, LocalDate fim);

    @Query("SELECT c FROM ContratoLegal c WHERE c.dataVencimento BETWEEN :inicio AND :fim ORDER BY c.dataVencimento")
    List<ContratoLegal> findByDataVencimentoBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c FROM ContratoLegal c WHERE c.dataVencimento <= :data AND c.status = 'ATIVO' ORDER BY c.dataVencimento")
    List<ContratoLegal> findContratosVencidos(@Param("data") LocalDate data);

    @Query("SELECT c FROM ContratoLegal c WHERE c.dataVencimento BETWEEN :hoje AND :futuro AND c.status = 'ATIVO' ORDER BY c.dataVencimento")
    List<ContratoLegal> findContratosVencendoEm(@Param("hoje") LocalDate hoje, @Param("futuro") LocalDate futuro);

    @Query("SELECT c FROM ContratoLegal c WHERE c.renovacaoAutomatica = true AND c.dataVencimento BETWEEN :hoje AND :futuro ORDER BY c.dataVencimento")
    List<ContratoLegal> findContratosParaRenovacao(@Param("hoje") LocalDate hoje, @Param("futuro") LocalDate futuro);

    @Query("SELECT c FROM ContratoLegal c WHERE c.status = 'ATIVO' AND c.dataFim <= :hoje ORDER BY c.dataFim")
    List<ContratoLegal> findContratosExpirados(@Param("hoje") LocalDate hoje);

    @Query("SELECT COUNT(c) FROM ContratoLegal c WHERE c.status = :status")
    Long countByStatus(@Param("status") ContratoLegal.StatusContrato status);

    @Query("SELECT SUM(c.valorContrato) FROM ContratoLegal c WHERE c.status = :status")
    BigDecimal sumValorContratoByStatus(@Param("status") ContratoLegal.StatusContrato status);

    @Query("SELECT SUM(c.valorMensal) FROM ContratoLegal c WHERE c.status = 'ATIVO'")
    BigDecimal sumValorMensalAtivos();

    @Query("SELECT c.tipo, COUNT(c) FROM ContratoLegal c GROUP BY c.tipo ORDER BY COUNT(c) DESC")
    List<Object[]> countByTipo();

    @Query("SELECT c.status, COUNT(c) FROM ContratoLegal c GROUP BY c.status ORDER BY COUNT(c) DESC")
    List<Object[]> countByStatus();

    @Query("SELECT c.prioridade, COUNT(c) FROM ContratoLegal c GROUP BY c.prioridade ORDER BY COUNT(c) DESC")
    List<Object[]> countByPrioridade();

    @Query("SELECT DATE(c.dataCriacao), COUNT(c) FROM ContratoLegal c WHERE c.dataCriacao BETWEEN :inicio AND :fim GROUP BY DATE(c.dataCriacao) ORDER BY DATE(c.dataCriacao)")
    List<Object[]> getContratosCriadosPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT YEAR(c.dataCriacao), MONTH(c.dataCriacao), COUNT(c) FROM ContratoLegal c WHERE c.dataCriacao BETWEEN :inicio AND :fim GROUP BY YEAR(c.dataCriacao), MONTH(c.dataCriacao) ORDER BY YEAR(c.dataCriacao), MONTH(c.dataCriacao)")
    List<Object[]> getContratosCriadosPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c FROM ContratoLegal c WHERE c.valorContrato > 0 ORDER BY c.valorContrato DESC")
    List<ContratoLegal> findContratosMaiorValor();

    @Query("SELECT c.cliente, COUNT(c), SUM(c.valorContrato) FROM ContratoLegal c WHERE c.cliente IS NOT NULL GROUP BY c.cliente ORDER BY SUM(c.valorContrato) DESC")
    List<Object[]> getClientesComMaisContratos();

    @Query("SELECT c.fornecedor, COUNT(c), SUM(c.valorContrato) FROM ContratoLegal c WHERE c.fornecedor IS NOT NULL GROUP BY c.fornecedor ORDER BY SUM(c.valorContrato) DESC")
    List<Object[]> getFornecedoresComMaisContratos();

    @Query("SELECT AVG(c.duracaoMeses) FROM ContratoLegal c WHERE c.duracaoMeses IS NOT NULL")
    Double getMediaDuracaoContratos();

    @Query("SELECT AVG(c.valorContrato) FROM ContratoLegal c WHERE c.valorContrato IS NOT NULL AND c.valorContrato > 0")
    BigDecimal getMediaValorContratos();

    @Query("SELECT c FROM ContratoLegal c WHERE c.titulo LIKE %:termo% OR c.descricao LIKE %:termo% OR c.numeroContrato LIKE %:termo%")
    List<ContratoLegal> findByTermoBusca(@Param("termo") String termo);

    @Query("SELECT c FROM ContratoLegal c WHERE c.tipo = :tipo AND c.status = :status ORDER BY c.dataCriacao DESC")
    List<ContratoLegal> findByTipoAndStatus(@Param("tipo") ContratoLegal.TipoContrato tipo, @Param("status") ContratoLegal.StatusContrato status);

    // Performance and compliance queries
    @Query("SELECT " +
           "COUNT(c) as totalContratos, " +
           "SUM(c.valorContrato) as valorTotal, " +
           "AVG(c.valorContrato) as valorMedio, " +
           "COUNT(CASE WHEN c.status = 'ATIVO' THEN 1 END) as ativos, " +
           "COUNT(CASE WHEN c.dataVencimento < :hoje THEN 1 END) as vencidos " +
           "FROM ContratoLegal c")
    List<Object[]> getEstatisticasGerais(@Param("hoje") LocalDate hoje);

    @Query("SELECT c.usuarioResponsavel, COUNT(c), SUM(c.valorContrato), AVG(c.duracaoMeses) " +
           "FROM ContratoLegal c " +
           "WHERE c.usuarioResponsavel IS NOT NULL " +
           "GROUP BY c.usuarioResponsavel " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getPerformancePorUsuario();

    @Query("SELECT c.tipo, " +
           "COUNT(c) as quantidade, " +
           "SUM(c.valorContrato) as valorTotal, " +
           "AVG(c.valorContrato) as valorMedio, " +
           "AVG(c.duracaoMeses) as duracaoMedia " +
           "FROM ContratoLegal c " +
           "GROUP BY c.tipo " +
           "ORDER BY SUM(c.valorContrato) DESC")
    List<Object[]> getEstatisticasPorTipo();

    boolean existsByNumeroContrato(String numeroContrato);

    @Query("SELECT COUNT(c) FROM ContratoLegal c WHERE c.dataVencimento <= :data AND c.status = 'ATIVO'")
    Long countContratosVencidos(@Param("data") LocalDate data);

    @Query("SELECT COUNT(c) FROM ContratoLegal c WHERE c.dataVencimento BETWEEN :hoje AND :futuro AND c.status = 'ATIVO'")
    Long countContratosVencendoEm(@Param("hoje") LocalDate hoje, @Param("futuro") LocalDate futuro);

    @Query("SELECT COUNT(c) FROM ContratoLegal c WHERE c.renovacaoAutomatica = true AND c.dataVencimento BETWEEN :hoje AND :futuro")
    Long countContratosParaRenovacao(@Param("hoje") LocalDate hoje, @Param("futuro") LocalDate futuro);

    // Compliance and risk analysis
    @Query("SELECT c FROM ContratoLegal c WHERE c.status = 'ATIVO' AND SIZE(c.alertas) > 0 ORDER BY c.prioridade DESC")
    List<ContratoLegal> findContratosComAlertas();

    @Query("SELECT c FROM ContratoLegal c WHERE c.status = 'ATIVO' AND SIZE(c.aditivos) >= 3 ORDER BY SIZE(c.aditivos) DESC")
    List<ContratoLegal> findContratosComMuitosAditivos();

    @Query("SELECT c FROM ContratoLegal c WHERE c.status = 'ATIVO' AND c.valorContrato > :valor ORDER BY c.valorContrato DESC")
    List<ContratoLegal> findContratosAltoValor(@Param("valor") BigDecimal valor);
}