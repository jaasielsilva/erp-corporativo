package com.jaasielsilva.portalceo.repository.ajuda;

import com.jaasielsilva.portalceo.model.ajuda.AjudaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AjudaCategoriaRepository extends JpaRepository<AjudaCategoria, Long> {
    Optional<AjudaCategoria> findBySlug(String slug);
}
