package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContaReceber;
import com.jaasielsilva.portalceo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {

    // ===================== LISTAS POR STATUS, CLIENTE OU DATA =====================
    
    List<ContaReceber> findByStatusOrderByDataVencimento(ContaReceber.StatusContaReceber status);
    
    List<ContaReceber> findByClienteOrderByDataVencimento(Cliente cliente);
    
    List<ContaReceber> findByDataVencimentoBetweenOrderByDataVencimento(LocalDate dataInicio, LocalDate dataFim);
    
    List<ContaReceber> findByDataVencimentoBeforeAndStatusIn(LocalDate data, List<ContaReceber.StatusContaReceber> statuses);

    List<ContaReceber> findByCategoriaAndStatusOrderByDataVencimento(
            ContaReceber.CategoriaContaReceber categoria, ContaReceber.StatusContaReceber status
    );

    // ===================== CONSULTAS CUSTOMIZADAS =====================

    @Query("SELECT c FROM ContaReceber c WHERE c.dataVencimento <= :data AND c.status IN :statuses ORDER BY c.dataVencimento")
    List<ContaReceber> findVencidas(@Param("data") LocalDate data, @Param("statuses") List<ContaReceber.StatusContaReceber> statuses);

    @Query("SELECT COUNT(c) FROM ContaReceber c WHERE c.dataVencimento <= :data AND c.status IN ('PENDENTE','PARCIAL','VENCIDA')")
    Long countVencidas(@Param("data") LocalDate data);

    @Query("SELECT COUNT(c) FROM ContaReceber c WHERE c.status = :status")
    Long countByStatus(@Param("status") ContaReceber.StatusContaReceber status);

    @Query("SELECT c FROM ContaReceber c WHERE c.status = 'INADIMPLENTE' ORDER BY c.dataVencimento")
    List<ContaReceber> findInadimplentes();

    @Query("SELECT c FROM ContaReceber c WHERE c.dataVencimento BETWEEN :inicio AND :fim ORDER BY c.dataVencimento")
    List<ContaReceber> findByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c FROM ContaReceber c WHERE c.dataVencimento BETWEEN :hoje AND :futuro AND c.status IN ('PENDENTE','PARCIAL') ORDER BY c.dataVencimento")
    List<ContaReceber> findVencendoEm(@Param("hoje") LocalDate hoje, @Param("futuro") LocalDate futuro);

    @Query("SELECT SUM(c.valorRecebido) FROM ContaReceber c WHERE c.status = 'RECEBIDA' AND c.dataRecebimento BETWEEN :inicio AND :fim")
    BigDecimal sumValorRecebidoByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c.categoria, SUM(c.valorRecebido) FROM ContaReceber c WHERE c.dataRecebimento BETWEEN :inicio AND :fim GROUP BY c.categoria")
    List<Object[]> sumValorRecebidoByCategoriaPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c.cliente, SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto - c.valorRecebido) " +
            "FROM ContaReceber c WHERE c.status IN ('PENDENTE','PARCIAL','VENCIDA','INADIMPLENTE') " +
            "GROUP BY c.cliente ORDER BY SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto - c.valorRecebido) DESC")
    List<Object[]> sumSaldoReceberByCliente();

    @Query("SELECT DATE(c.dataRecebimento), SUM(c.valorRecebido) FROM ContaReceber c " +
            "WHERE c.dataRecebimento BETWEEN :inicio AND :fim GROUP BY DATE(c.dataRecebimento) ORDER BY DATE(c.dataRecebimento)")
    List<Object[]> sumValorRecebidoPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(c), SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto - c.valorRecebido) " +
            "FROM ContaReceber c WHERE c.dataVencimento BETWEEN :inicio AND :fim " +
            "AND c.status IN ('PENDENTE','PARCIAL','VENCIDA','INADIMPLENTE')")
    List<Object[]> getAgingData(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT c FROM ContaReceber c WHERE c.venda.id = :vendaId")
    List<ContaReceber> findByVendaId(@Param("vendaId") Long vendaId);

    @Query("SELECT c FROM ContaReceber c WHERE c.usuarioCriacao.id = :usuarioId ORDER BY c.dataCriacao DESC")
    List<ContaReceber> findByUsuarioCriacao(@Param("usuarioId") Long usuarioId);

    // ===================== SOMAS E EXISTÊNCIAS =====================

    @Query("SELECT SUM(c.valorOriginal + c.valorJuros + c.valorMulta - c.valorDesconto - c.valorRecebido) " +
            "FROM ContaReceber c WHERE c.status IN ('PENDENTE','PARCIAL','VENCIDA','INADIMPLENTE')")
    BigDecimal sumSaldoReceber();

    boolean existsByNumeroDocumento(String numeroDocumento);
}
