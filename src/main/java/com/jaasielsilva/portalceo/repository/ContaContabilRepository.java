package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContaBancaria;
import com.jaasielsilva.portalceo.model.ContaContabil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContaContabilRepository extends JpaRepository<ContaContabil, Long> {
    Optional<ContaContabil> findByCodigo(String codigo);
    Optional<ContaContabil> findByContaBancaria(ContaBancaria contaBancaria);
    boolean existsByCodigo(String codigo);
}

