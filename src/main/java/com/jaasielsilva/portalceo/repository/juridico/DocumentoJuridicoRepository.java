package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoJuridicoRepository extends JpaRepository<DocumentoJuridico, Long> {
    java.util.List<DocumentoJuridico> findTop10ByOrderByCriadoEmDesc();
}