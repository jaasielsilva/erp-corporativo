package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.FluxoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FluxoCaixaRepository extends JpaRepository<FluxoCaixa, Long> {

    List<FluxoCaixa> findByDataBetweenOrderByData(LocalDate dataInicio, LocalDate dataFim);

    org.springframework.data.domain.Page<FluxoCaixa> findByDataBetween(LocalDate dataInicio, LocalDate dataFim, org.springframework.data.domain.Pageable pageable);

    List<FluxoCaixa> findByTipoMovimentoAndDataBetweenOrderByData(FluxoCaixa.TipoMovimento tipoMovimento, LocalDate dataInicio, LocalDate dataFim);

    List<FluxoCaixa> findByCategoriaAndDataBetweenOrderByData(FluxoCaixa.CategoriaFluxo categoria, LocalDate dataInicio, LocalDate dataFim);

    List<FluxoCaixa> findByStatusAndDataBetweenOrderByData(FluxoCaixa.StatusFluxo status, LocalDate dataInicio, LocalDate dataFim);

    @Query("SELECT f FROM FluxoCaixa f LEFT JOIN FETCH f.contaBancaria LEFT JOIN FETCH f.usuarioCriacao WHERE f.status = :status AND f.data BETWEEN :inicio AND :fim ORDER BY f.data")
    List<FluxoCaixa> findByStatusAndDataBetweenWithRelations(@Param("status") FluxoCaixa.StatusFluxo status, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(f.valor) FROM FluxoCaixa f WHERE f.tipoMovimento = 'ENTRADA' AND f.data BETWEEN :inicio AND :fim AND f.status = 'REALIZADO'")
    BigDecimal sumEntradasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(f.valor) FROM FluxoCaixa f WHERE f.tipoMovimento = 'SAIDA' AND f.data BETWEEN :inicio AND :fim AND f.status = 'REALIZADO'")
    BigDecimal sumSaidasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(CASE WHEN f.tipoMovimento = 'ENTRADA' THEN f.valor ELSE -f.valor END) FROM FluxoCaixa f WHERE f.data BETWEEN :inicio AND :fim AND f.status = 'REALIZADO'")
    BigDecimal calcularSaldoByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(CASE WHEN f.tipoMovimento = 'ENTRADA' THEN f.valor ELSE -f.valor END) FROM FluxoCaixa f WHERE f.data <= :data AND f.status = 'REALIZADO'")
    BigDecimal calcularSaldoAcumuladoAte(@Param("data") LocalDate data);

    @Query("SELECT f.categoria, SUM(f.valor) FROM FluxoCaixa f WHERE f.tipoMovimento = :tipoMovimento AND f.data BETWEEN :inicio AND :fim AND f.status = 'REALIZADO' GROUP BY f.categoria ORDER BY SUM(f.valor) DESC")
    List<Object[]> sumValorByCategoriaAndTipoMovimento(@Param("tipoMovimento") FluxoCaixa.TipoMovimento tipoMovimento, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT f.data, SUM(CASE WHEN f.tipoMovimento = 'ENTRADA' THEN f.valor ELSE 0 END), SUM(CASE WHEN f.tipoMovimento = 'SAIDA' THEN f.valor ELSE 0 END) FROM FluxoCaixa f WHERE f.data BETWEEN :inicio AND :fim AND f.status = 'REALIZADO' GROUP BY f.data ORDER BY f.data")
    List<Object[]> getFluxoDiario(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT YEAR(f.data), MONTH(f.data), SUM(CASE WHEN f.tipoMovimento = 'ENTRADA' THEN f.valor ELSE 0 END), SUM(CASE WHEN f.tipoMovimento = 'SAIDA' THEN f.valor ELSE 0 END) FROM FluxoCaixa f WHERE f.data BETWEEN :inicio AND :fim AND f.status = 'REALIZADO' GROUP BY YEAR(f.data), MONTH(f.data) ORDER BY YEAR(f.data), MONTH(f.data)")
    List<Object[]> getFluxoMensal(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(f) FROM FluxoCaixa f WHERE f.tipoMovimento = :tipoMovimento AND f.data BETWEEN :inicio AND :fim")
    Long countByTipoMovimentoAndPeriodo(@Param("tipoMovimento") FluxoCaixa.TipoMovimento tipoMovimento, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(f) FROM FluxoCaixa f WHERE f.status = :status AND f.data BETWEEN :inicio AND :fim")
    Long countByStatusAndPeriodo(@Param("status") FluxoCaixa.StatusFluxo status, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    List<FluxoCaixa> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT f FROM FluxoCaixa f WHERE f.contaPagar.id = :contaPagarId")
    List<FluxoCaixa> findByContaPagar(@Param("contaPagarId") Long contaPagarId);

    @Query("SELECT f FROM FluxoCaixa f WHERE f.contaReceber.id = :contaReceberId")
    List<FluxoCaixa> findByContaReceber(@Param("contaReceberId") Long contaReceberId);

    boolean existsByNumeroDocumento(String numeroDocumento);

    @Query("SELECT f FROM FluxoCaixa f WHERE f.usuarioCriacao.id = :usuarioId ORDER BY f.dataCriacao DESC")
    List<FluxoCaixa> findByUsuarioCriacao(@Param("usuarioId") Long usuarioId);

    // Projeções para relatórios
    @Query("SELECT SUM(f.valor) FROM FluxoCaixa f WHERE f.tipoMovimento = 'ENTRADA' AND f.status = 'PREVISTO' AND f.data BETWEEN :inicio AND :fim")
    BigDecimal sumEntradasPrevistasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(f.valor) FROM FluxoCaixa f WHERE f.tipoMovimento = 'SAIDA' AND f.status = 'PREVISTO' AND f.data BETWEEN :inicio AND :fim")
    BigDecimal sumSaidasPrevistasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
