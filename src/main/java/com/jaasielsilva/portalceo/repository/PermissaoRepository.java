package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    // Pode adicionar métodos personalizados se quiser
}
