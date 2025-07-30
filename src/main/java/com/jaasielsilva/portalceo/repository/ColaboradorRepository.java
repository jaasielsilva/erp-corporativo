package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    List<Colaborador> findByAtivoTrue();
}
