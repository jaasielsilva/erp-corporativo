package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Venda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    long countByClienteId(Long clienteId);

    @Query("SELECT SUM(v.total) FROM Venda v")
    Optional<BigDecimal> calcularTotalDeVendas();

    List<Venda> findTop10ByOrderByDataVendaDesc();

    // Pesquisa por cpfCnpj do cliente (contendo ignore case)
    List<Venda> findByClienteCpfCnpjContainingIgnoreCase(String cpfCnpj);

    // Método paginado (se quiser futuramente paginar)
    Page<Venda> findByClienteCpfCnpjContainingIgnoreCase(String cpfCnpj, Pageable pageable);
}
