package com.jaasielsilva.portalceo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaasielsilva.portalceo.model.Beneficio;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {
    // Aqui você pode adicionar consultas customizadas se precisar
}
