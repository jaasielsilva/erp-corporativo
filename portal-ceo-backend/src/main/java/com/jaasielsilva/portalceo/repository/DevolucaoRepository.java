package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Devolucao;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DevolucaoRepository extends JpaRepository<Devolucao, Long> {

    List<Devolucao> findByStatusOrderByDataDevolucaoDesc(Devolucao.StatusDevolucao status);

    List<Devolucao> findByVendaOriginalOrderByDataDevolucaoDesc(Venda vendaOriginal);

    List<Devolucao> findByVendaOriginal_ClienteOrderByDataDevolucaoDesc(Cliente cliente);

    List<Devolucao> findByMotivoOrderByDataDevolucaoDesc(Devolucao.MotivoDevolucao motivo);

    List<Devolucao> findByTipoOrderByDataDevolucaoDesc(Devolucao.TipoDevolucao tipo);

    List<Devolucao> findByDataDevolucaoBetweenOrderByDataDevolucaoDesc(LocalDateTime inicio, LocalDateTime fim);

    List<Devolucao> findByUsuarioResponsavelOrderByDataDevolucaoDesc(com.jaasielsilva.portalceo.model.Usuario usuario);

    @Query("SELECT d FROM Devolucao d WHERE d.status = :status AND d.dataDevolucao BETWEEN :inicio AND :fim ORDER BY d.dataDevolucao DESC")
    List<Devolucao> findByStatusAndPeriodo(@Param("status") Devolucao.StatusDevolucao status, 
                                          @Param("inicio") LocalDateTime inicio, 
                                          @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(d) FROM Devolucao d WHERE d.status = :status")
    Long countByStatus(@Param("status") Devolucao.StatusDevolucao status);

    @Query("SELECT SUM(d.valorTotal) FROM Devolucao d WHERE d.status = :status")
    BigDecimal sumValorTotalByStatus(@Param("status") Devolucao.StatusDevolucao status);

    @Query("SELECT SUM(d.valorEstorno) FROM Devolucao d WHERE d.status = 'PROCESSADA' AND d.dataDevolucao BETWEEN :inicio AND :fim")
    BigDecimal sumValorEstornoByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(d) FROM Devolucao d WHERE d.dataDevolucao BETWEEN :inicio AND :fim")
    Long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT d.motivo, COUNT(d) FROM Devolucao d GROUP BY d.motivo ORDER BY COUNT(d) DESC")
    List<Object[]> countByMotivo();

    @Query("SELECT d.tipo, COUNT(d) FROM Devolucao d GROUP BY d.tipo ORDER BY COUNT(d) DESC")
    List<Object[]> countByTipo();

    @Query("SELECT DATE(d.dataDevolucao), COUNT(d), SUM(d.valorTotal) " +
           "FROM Devolucao d " +
           "WHERE d.dataDevolucao BETWEEN :inicio AND :fim " +
           "GROUP BY DATE(d.dataDevolucao) " +
           "ORDER BY DATE(d.dataDevolucao)")
    List<Object[]> getDevolucoesPorDia(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT YEAR(d.dataDevolucao), MONTH(d.dataDevolucao), COUNT(d), SUM(d.valorTotal) " +
           "FROM Devolucao d " +
           "WHERE d.dataDevolucao BETWEEN :inicio AND :fim " +
           "GROUP BY YEAR(d.dataDevolucao), MONTH(d.dataDevolucao) " +
           "ORDER BY YEAR(d.dataDevolucao), MONTH(d.dataDevolucao)")
    List<Object[]> getDevolucoesPorMes(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT d FROM Devolucao d WHERE d.status IN ('PENDENTE', 'APROVADA') AND d.dataDevolucao < :dataLimite ORDER BY d.dataDevolucao")
    List<Devolucao> findDevolucoesPendentesAntigas(@Param("dataLimite") LocalDateTime dataLimite);

    @Query("SELECT c, COUNT(d), SUM(d.valorTotal) " +
           "FROM Devolucao d " +
           "JOIN d.vendaOriginal.cliente c " +
           "WHERE d.dataDevolucao BETWEEN :inicio AND :fim " +
           "GROUP BY c " +
           "ORDER BY COUNT(d) DESC")
    List<Object[]> getClientesComMaisDevolucoes(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p, COUNT(di), SUM(di.subtotal) " +
           "FROM DevolucaoItem di " +
           "JOIN di.produto p " +
           "JOIN di.devolucao d " +
           "WHERE d.dataDevolucao BETWEEN :inicio AND :fim " +
           "GROUP BY p " +
           "ORDER BY COUNT(di) DESC")
    List<Object[]> getProdutosComMaisDevolucoes(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT AVG(DATEDIFF(d.dataProcessamento, d.dataDevolucao)) " +
           "FROM Devolucao d " +
           "WHERE d.status = 'PROCESSADA' AND d.dataProcessamento IS NOT NULL")
    Double getTempoMedioProcessamento();

    boolean existsByVendaOriginalAndStatus(Venda vendaOriginal, Devolucao.StatusDevolucao status);

    @Query("SELECT d FROM Devolucao d WHERE d.vendaOriginal.numeroVenda = :numeroVenda")
    List<Devolucao> findByNumeroVenda(@Param("numeroVenda") String numeroVenda);

    // Relatórios de performance
    @Query("SELECT " +
           "COUNT(d) as totalDevolucoes, " +
           "SUM(d.valorTotal) as valorTotal, " +
           "AVG(d.valorTotal) as valorMedio, " +
           "COUNT(CASE WHEN d.status = 'PROCESSADA' THEN 1 END) as processadas, " +
           "COUNT(CASE WHEN d.status = 'REJEITADA' THEN 1 END) as rejeitadas " +
           "FROM Devolucao d " +
           "WHERE d.dataDevolucao BETWEEN :inicio AND :fim")
    List<Object[]> getEstatisticasPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}