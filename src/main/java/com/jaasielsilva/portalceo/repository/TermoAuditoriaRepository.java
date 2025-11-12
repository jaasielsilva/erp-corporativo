package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.TermoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermoAuditoriaRepository extends JpaRepository<TermoAuditoria, Long> {
    List<TermoAuditoria> findByTermoOrderByDataEventoDesc(Termo termo);
}