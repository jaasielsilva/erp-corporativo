package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNome(String nome);
}
