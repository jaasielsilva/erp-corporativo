package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Caixa;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    // Conta quantas vendas um cliente específico realizou
    long countByClienteId(Long clienteId);

    // Soma total de todas as vendas registradas no sistema
    @Query("SELECT SUM(v.total) FROM Venda v")
    Optional<BigDecimal> calcularTotalDeVendas();

    // Retorna as últimas 10 vendas realizadas, ordenadas pela data (mais recentes primeiro)
    List<Venda> findTop10ByOrderByDataVendaDesc();

    // Busca todas as vendas por parte do CPF ou CNPJ do cliente (sem diferenciar maiúsculas de minúsculas)
    List<Venda> findByClienteCpfCnpjContainingIgnoreCase(String cpfCnpj);

    // Versão paginada da busca acima (útil para paginação em telas)
    Page<Venda> findByClienteCpfCnpjContainingIgnoreCase(String cpfCnpj, Pageable pageable);

    // Busca genérica com ordenação por data (pode ser usada com qualquer quantidade via Pageable)
    List<Venda> findAllByOrderByDataVendaDesc(Pageable pageable);

    // método para totalizar vendas por mês desde uma data
    @Query("SELECT YEAR(v.dataVenda), MONTH(v.dataVenda), SUM(v.total) " +
           "FROM Venda v " +
           "WHERE v.dataVenda >= :dataInicio " +
           "GROUP BY YEAR(v.dataVenda), MONTH(v.dataVenda) " +
           "ORDER BY YEAR(v.dataVenda), MONTH(v.dataVenda)")
    List<Object[]> totalVendasPorMesDesde(@Param("dataInicio") LocalDateTime dataInicio);

    // Conta vendas a partir de uma data específica
    long countByDataVendaGreaterThanEqual(LocalDateTime dataInicio);

    // Calcula o faturamento (soma dos totais) entre duas datas
    @Query("SELECT SUM(v.total) FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim")
    Optional<BigDecimal> calcularFaturamentoPorPeriodo(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    // Calcula vendas por categoria (nome da categoria e valor total vendido)
    @Query("SELECT p.categoria.nome, SUM(vi.subtotal) " +
           "FROM VendaItem vi " +
           "JOIN vi.produto p " +
           "JOIN vi.venda v " +
           "WHERE p.ativo = true " +
           "GROUP BY p.categoria.nome " +
           "ORDER BY SUM(vi.subtotal) DESC")
    List<Object[]> calcularVendasPorCategoria();

    // ===== MÉTODOS PARA PDV =====
    
    // Buscar vendas por período
    List<Venda> findByDataVendaBetween(LocalDateTime inicio, LocalDateTime fim);
    
    // Buscar vendas por forma de pagamento
    List<Venda> findByFormaPagamentoIgnoreCase(String formaPagamento);
    
    // Buscar vendas por forma de pagamento e período
    List<Venda> findByFormaPagamentoIgnoreCaseAndDataVendaBetween(
        String formaPagamento, LocalDateTime inicio, LocalDateTime fim);
    
    // Buscar vendas por status
    List<Venda> findByStatus(String status);
    
    // Buscar vendas por usuário
    List<Venda> findByUsuario(Usuario usuario);
    
    // Buscar vendas por caixa
    List<Venda> findByCaixa(Caixa caixa);
    
    // Buscar vendas por número da venda
    Optional<Venda> findByNumeroVenda(String numeroVenda);
    
    // Contar vendas por período
    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
    long countByDataVendaBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    // Somar total por forma de pagamento e período
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venda v WHERE v.formaPagamento = :formaPagamento AND v.dataVenda BETWEEN :inicio AND :fim")
    BigDecimal somarTotalPorFormaPagamentoEPeriodo(
        @Param("formaPagamento") String formaPagamento,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim);
    
    // Buscar últimas vendas
    @Query("SELECT v FROM Venda v ORDER BY v.dataVenda DESC")
    List<Venda> findUltimasVendas(Pageable pageable);
    
    // Buscar vendas do dia por usuário
    @Query("SELECT v FROM Venda v WHERE v.usuario = :usuario AND DATE(v.dataVenda) = CURRENT_DATE ORDER BY v.dataVenda DESC")
    List<Venda> findVendasDoDiaPorUsuario(@Param("usuario") Usuario usuario);
    
    // Calcular ticket médio por período
    @Query("SELECT AVG(v.total) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
    BigDecimal calcularTicketMedioPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

}
