package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContaPagar;
import com.jaasielsilva.portalceo.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaPagarRepository extends JpaRepository<ContaPagar, Long> {

    List<ContaPagar> findByStatusOrderByDataVencimento(ContaPagar.StatusContaPagar status);

    List<ContaPagar> findByFornecedorOrderByDataVencimento(Fornecedor fornecedor);

    List<ContaPagar> findByDataVencimentoBetweenOrderByDataVencimento(LocalDate dataInicio, LocalDate dataFim);

    List<ContaPagar> findByDataVencimentoBeforeAndStatusIn(LocalDate data, List<ContaPagar.StatusContaPagar> statuses);

    List<ContaPagar> findByCategoriaAndStatusOrderByDataVencimento(ContaPagar.CategoriaContaPagar categoria,
            ContaPagar.StatusContaPagar status);

    @Query("SELECT c FROM ContaPagar c WHERE c.dataVencimento BETWEEN :inicio AND :fim ORDER BY c.dataVencimento")
    List<ContaPagar> findByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c FROM ContaPagar c WHERE c.dataVencimento <= :data AND c.status IN :statuses ORDER BY c.dataVencimento")
    List<ContaPagar> findVencidas(@Param("data") LocalDate data,
            @Param("statuses") List<ContaPagar.StatusContaPagar> statuses);

    @Query("SELECT SUM(c.valorOriginal) FROM ContaPagar c WHERE c.status = :status")
    BigDecimal sumValorOriginalByStatus(@Param("status") ContaPagar.StatusContaPagar status);

    @Query("SELECT SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto) FROM ContaPagar c WHERE c.status = :status")
    BigDecimal sumValorTotalByStatus(@Param("status") ContaPagar.StatusContaPagar status);

    @Query("SELECT SUM(c.valorPago) FROM ContaPagar c WHERE c.status = 'PAGA' AND c.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal sumValorPagoByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(c) FROM ContaPagar c WHERE c.status = :status")
    Long countByStatus(@Param("status") ContaPagar.StatusContaPagar status);

    @Query("SELECT COUNT(c) FROM ContaPagar c WHERE c.dataVencimento <= :data AND c.status IN ('PENDENTE', 'APROVADA')")
    Long countVencidas(@Param("data") LocalDate data);

    @Query("SELECT c FROM ContaPagar c WHERE c.dataVencimento BETWEEN :hoje AND :futuro AND c.status IN ('PENDENTE', 'APROVADA') ORDER BY c.dataVencimento")
    List<ContaPagar> findVencendoEm(@Param("hoje") LocalDate hoje, @Param("futuro") LocalDate futuro);

    @Query("SELECT c.categoria, SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto) FROM ContaPagar c WHERE c.dataPagamento BETWEEN :inicio AND :fim GROUP BY c.categoria")
    List<Object[]> sumValorTotalByCategoriaPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c.fornecedor, SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto) FROM ContaPagar c WHERE c.status = :status GROUP BY c.fornecedor ORDER BY SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto) DESC")
    List<Object[]> sumValorTotalByFornecedorStatus(@Param("status") ContaPagar.StatusContaPagar status);

    @Query("SELECT DATE(c.dataPagamento), SUM(c.valorPago) FROM ContaPagar c WHERE c.dataPagamento BETWEEN :inicio AND :fim GROUP BY DATE(c.dataPagamento) ORDER BY DATE(c.dataPagamento)")
    List<Object[]> sumValorPagoPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    boolean existsByNumeroDocumento(String numeroDocumento);

    List<ContaPagar> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT c FROM ContaPagar c WHERE c.usuarioCriacao.id = :usuarioId ORDER BY c.dataCriacao DESC")
    List<ContaPagar> findByUsuarioCriacao(@Param("usuarioId") Long usuarioId);

    @Query("SELECT SUM(c.valorOriginal) FROM ContaPagar c WHERE c.status = :status")
    BigDecimal sumByStatus(@Param("status") ContaPagar.StatusContaPagar status);

    @Query("SELECT SUM(c.valorOriginal) FROM ContaPagar c WHERE c.status = :status AND c.dataVencimento < :data")
    BigDecimal sumByStatusAndDataVencimentoBefore(@Param("status") ContaPagar.StatusContaPagar status,
            @Param("data") LocalDate data);

}