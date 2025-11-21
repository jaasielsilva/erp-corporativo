package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.DocumentoModelo;
import com.jaasielsilva.portalceo.model.juridico.ModeloStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoModeloRepository extends JpaRepository<DocumentoModelo, Long> {
    Page<DocumentoModelo> findByCategoriaContainingIgnoreCaseAndStatus(String categoria, ModeloStatus status, Pageable pageable);
    Page<DocumentoModelo> findByCategoriaContainingIgnoreCase(String categoria, Pageable pageable);
    Page<DocumentoModelo> findByStatus(ModeloStatus status, Pageable pageable);
}