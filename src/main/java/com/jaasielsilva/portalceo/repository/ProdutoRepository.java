package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Produto findByEan(String ean);

    @Query("SELECT COALESCE(SUM(p.estoque), 0) FROM Produto p WHERE p.ativo = true")
    long somarQuantidadeEstoque();

}
