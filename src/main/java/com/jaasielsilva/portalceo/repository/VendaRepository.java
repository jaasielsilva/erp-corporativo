package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // Busca as 2 últimas vendas realizadas
    @Query("SELECT v FROM Venda v ORDER BY v.dataVenda DESC")
    List<Venda> findTop2ByOrderByDataVendaDesc(Pageable pageable);

    // metodo pra totalizar vendas por mês desde uma data
    @Query("SELECT YEAR(v.dataVenda), MONTH(v.dataVenda), SUM(v.total) "
     + "FROM Venda v "
     + "WHERE v.dataVenda >= :dataInicio "
     + "GROUP BY YEAR(v.dataVenda), MONTH(v.dataVenda) "
     + "ORDER BY YEAR(v.dataVenda), MONTH(v.dataVenda)")
List<Object[]> totalVendasPorMesDesde(@Param("dataInicio") LocalDateTime dataInicio);
}
