package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {

    // Buscar Benefício por nome
    Optional<Beneficio> findByNome(String nome);

    boolean existsByNome(String nome);
}
