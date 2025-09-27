package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ColaboradorBeneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaboradorBeneficioRepository extends JpaRepository<ColaboradorBeneficio, Long> {
}
