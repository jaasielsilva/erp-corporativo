package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Fornecedor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findByAtivoTrue();
}
