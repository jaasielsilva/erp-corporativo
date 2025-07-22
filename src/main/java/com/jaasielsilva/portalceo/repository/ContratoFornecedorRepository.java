package com.jaasielsilva.portalceo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaasielsilva.portalceo.model.ContratoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;

@Repository
public interface ContratoFornecedorRepository extends JpaRepository<ContratoFornecedor, Long> {
    List<ContratoFornecedor> findByFornecedor(Fornecedor fornecedor);
}

