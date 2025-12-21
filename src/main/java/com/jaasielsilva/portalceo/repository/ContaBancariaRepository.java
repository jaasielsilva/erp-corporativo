package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    List<ContaBancaria> findByAtivoTrue();
    boolean existsByNome(String nome);
}
