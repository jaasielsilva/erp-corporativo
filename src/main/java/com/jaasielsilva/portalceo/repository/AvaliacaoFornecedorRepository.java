package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.AvaliacaoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AvaliacaoFornecedorRepository extends JpaRepository<AvaliacaoFornecedor, Long> {
    List<AvaliacaoFornecedor> findByFornecedor(Fornecedor fornecedor);

    Page<AvaliacaoFornecedor> findByFornecedor(Fornecedor fornecedor, Pageable pageable);
}
