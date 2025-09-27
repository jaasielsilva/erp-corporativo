package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.VendaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {

    @Query("""
           SELECT vi.produto.categoria.nome, SUM(vi.precoUnitario * vi.quantidade)
           FROM VendaItem vi
           GROUP BY vi.produto.categoria.nome
           """)
    List<Object[]> totalVendasPorCategoria();
}
