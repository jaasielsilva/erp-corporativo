package com.jaasielsilva.portalceo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {
    Optional<PlanoSaude> findByNome(String nome);
}
