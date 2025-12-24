package com.jaasielsilva.portalceo.juridico.previdenciario.workflow.repository;

import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflow;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapaWorkflowRepository extends JpaRepository<EtapaWorkflow, Long> {
    Optional<EtapaWorkflow> findByCodigo(EtapaWorkflowCodigo codigo);
    List<EtapaWorkflow> findAllByOrderByOrdemAsc();
}

