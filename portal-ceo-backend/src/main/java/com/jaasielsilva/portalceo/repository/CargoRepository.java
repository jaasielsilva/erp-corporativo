package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    Optional<Cargo> findByNome(String nome);

    List<Cargo> findByAtivoTrue();
}
