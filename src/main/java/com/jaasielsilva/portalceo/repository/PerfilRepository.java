package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    // Pode adicionar métodos personalizados se quiser
}
