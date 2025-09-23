package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContratoAditivo;
import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContratoAditivoRepository extends JpaRepository<ContratoAditivo, Long> {

    List<ContratoAditivo> findByContratoOrderByDataCriacaoDesc(ContratoLegal contrato);

    List<ContratoAditivo> findByStatusOrderByDataCriacaoDesc(ContratoAditivo.StatusAditivo status);

    List<ContratoAditivo> findByTipoOrderByDataCriacaoDesc(ContratoAditivo.TipoAditivo tipo);

    List<ContratoAditivo> findByUsuarioCriacaoOrderByDataCriacaoDesc(Usuario usuario);

    List<ContratoAditivo> findByDataCriacaoBetweenOrderByDataCriacao(LocalDate inicio, LocalDate fim);

    List<ContratoAditivo> findByDataAssinaturaBetweenOrderByDataAssinatura(LocalDate inicio, LocalDate fim);

    @Query("SELECT a FROM ContratoAditivo a WHERE a.contrato.id = :contratoId ORDER BY a.dataCriacao DESC")
    List<ContratoAditivo> findByContratoId(@Param("contratoId") Long contratoId);

    @Query("SELECT a FROM ContratoAditivo a WHERE a.status = 'EM_ANALISE' ORDER BY a.dataCriacao")
    List<ContratoAditivo> findAditivosPendentesAnalise();

    @Query("SELECT a FROM ContratoAditivo a WHERE a.status = 'APROVADO' AND a.dataAssinatura IS NULL ORDER BY a.dataAprovacao")
    List<ContratoAditivo> findAditivosPendentesAssinatura();

    @Query("SELECT a FROM ContratoAditivo a WHERE a.status = 'ASSINADO' AND a.dataVigencia IS NULL ORDER BY a.dataAssinatura")
    List<ContratoAditivo> findAditivosPendentesVigencia();

    @Query("SELECT COUNT(a) FROM ContratoAditivo a WHERE a.contrato = :contrato")
    Long countByContrato(@Param("contrato") ContratoLegal contrato);

    @Query("SELECT COUNT(a) FROM ContratoAditivo a WHERE a.status = :status")
    Long countByStatus(@Param("status") ContratoAditivo.StatusAditivo status);

    @Query("SELECT COUNT(a) FROM ContratoAditivo a WHERE a.tipo = :tipo AND a.dataCriacao BETWEEN :inicio AND :fim")
    Long countByTipoAndPeriodo(@Param("tipo") ContratoAditivo.TipoAditivo tipo, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT a.tipo, COUNT(a) FROM ContratoAditivo a GROUP BY a.tipo ORDER BY COUNT(a) DESC")
    List<Object[]> countByTipo();

    @Query("SELECT a.status, COUNT(a) FROM ContratoAditivo a GROUP BY a.status ORDER BY COUNT(a) DESC")
    List<Object[]> countByStatusDetalhado();

    @Query("SELECT YEAR(a.dataCriacao), MONTH(a.dataCriacao), COUNT(a) FROM ContratoAditivo a WHERE a.dataCriacao BETWEEN :inicio AND :fim GROUP BY YEAR(a.dataCriacao), MONTH(a.dataCriacao) ORDER BY YEAR(a.dataCriacao), MONTH(a.dataCriacao)")
    List<Object[]> getAditivosCriadosPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(a.valorDiferenca) FROM ContratoAditivo a WHERE a.tipo = 'VALOR' AND a.status = 'VIGENTE' AND a.valorDiferenca > 0")
    BigDecimal sumAumentosValor();

    @Query("SELECT SUM(ABS(a.valorDiferenca)) FROM ContratoAditivo a WHERE a.tipo = 'VALOR' AND a.status = 'VIGENTE' AND a.valorDiferenca < 0")
    BigDecimal sumReducoesValor();

    @Query("SELECT AVG(a.duracaoNova - a.duracaoAnterior) FROM ContratoAditivo a WHERE a.tipo = 'PRAZO' AND a.status = 'VIGENTE' AND a.duracaoAnterior IS NOT NULL AND a.duracaoNova IS NOT NULL")
    Double getMediaAlteracoesPrazo();

    @Query("SELECT a FROM ContratoAditivo a WHERE a.valorDiferenca IS NOT NULL AND ABS(a.valorDiferenca) > :valorMinimo ORDER BY ABS(a.valorDiferenca) DESC")
    List<ContratoAditivo> findAditivosComMaiorImpactoFinanceiro(@Param("valorMinimo") BigDecimal valorMinimo);

    @Query("SELECT a FROM ContratoAditivo a WHERE a.tipo = 'PRAZO' AND a.duracaoNova IS NOT NULL AND a.duracaoAnterior IS NOT NULL AND (a.duracaoNova - a.duracaoAnterior) > :mesesMinimos ORDER BY (a.duracaoNova - a.duracaoAnterior) DESC")
    List<ContratoAditivo> findAditivosComMaiorProrrogacao(@Param("mesesMinimos") Integer mesesMinimos);

    @Query("SELECT a.contrato.id, COUNT(a) FROM ContratoAditivo a GROUP BY a.contrato.id HAVING COUNT(a) > :quantidadeMinima ORDER BY COUNT(a) DESC")
    List<Object[]> findContratosComMaisAditivos(@Param("quantidadeMinima") Long quantidadeMinima);

    @Query("SELECT a.usuarioCriacao, COUNT(a) FROM ContratoAditivo a WHERE a.usuarioCriacao IS NOT NULL GROUP BY a.usuarioCriacao ORDER BY COUNT(a) DESC")
    List<Object[]> getAditivosPorUsuario();

    @Query("SELECT a FROM ContratoAditivo a WHERE a.numeroAditivo LIKE %:numero% ORDER BY a.dataCriacao DESC")
    List<ContratoAditivo> findByNumeroAditivoContaining(@Param("numero") String numero);

    @Query("SELECT a FROM ContratoAditivo a WHERE a.titulo LIKE %:titulo% ORDER BY a.dataCriacao DESC")
    List<ContratoAditivo> findByTituloContaining(@Param("titulo") String titulo);

    @Query("SELECT a FROM ContratoAditivo a WHERE a.tipo = :tipo AND a.status = :status ORDER BY a.dataCriacao DESC")
    List<ContratoAditivo> findByTipoAndStatus(@Param("tipo") ContratoAditivo.TipoAditivo tipo, @Param("status") ContratoAditivo.StatusAditivo status);

    // Performance and Analytics
    @Query("SELECT " +
           "COUNT(a) as totalAditivos, " +
           "COUNT(CASE WHEN a.tipo = 'VALOR' THEN 1 END) as aditivosValor, " +
           "COUNT(CASE WHEN a.tipo = 'PRAZO' THEN 1 END) as aditivosPrazo, " +
           "COUNT(CASE WHEN a.tipo = 'ESCOPO' THEN 1 END) as aditivosEscopo, " +
           "SUM(CASE WHEN a.valorDiferenca > 0 THEN a.valorDiferenca ELSE 0 END) as totalAumentos, " +
           "SUM(CASE WHEN a.valorDiferenca < 0 THEN ABS(a.valorDiferenca) ELSE 0 END) as totalReducoes " +
           "FROM ContratoAditivo a " +
           "WHERE a.dataCriacao BETWEEN :inicio AND :fim")
    List<Object[]> getEstatisticasGerais(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT a.tipo, " +
           "COUNT(a) as quantidade, " +
           "AVG(CASE WHEN a.valorDiferenca IS NOT NULL THEN ABS(a.valorDiferenca) ELSE 0 END) as valorMedio, " +
           "COUNT(CASE WHEN a.status = 'VIGENTE' THEN 1 END) as vigentes " +
           "FROM ContratoAditivo a " +
           "WHERE a.dataCriacao BETWEEN :inicio AND :fim " +
           "GROUP BY a.tipo " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getEstatisticasPorTipo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    boolean existsByNumeroAditivo(String numeroAditivo);

    boolean existsByContratoAndNumeroAditivo(ContratoLegal contrato, String numeroAditivo);
}