package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    long countByClienteId(Long clienteId);

    @Query("SELECT SUM(v.total) FROM Venda v")
    Optional<BigDecimal> calcularTotalDeVendas();

    List<Venda> findTop10ByOrderByDataVendaDesc(); // ou use um Pageable se quiser mais flexibilidade
}
