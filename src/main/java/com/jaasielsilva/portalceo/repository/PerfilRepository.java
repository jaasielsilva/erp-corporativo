package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(String nome);
}
