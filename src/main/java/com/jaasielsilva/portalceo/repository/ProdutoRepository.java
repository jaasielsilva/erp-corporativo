package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Produto findByEan(String ean);
}
