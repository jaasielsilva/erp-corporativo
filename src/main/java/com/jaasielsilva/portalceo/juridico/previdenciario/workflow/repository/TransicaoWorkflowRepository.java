package com.jaasielsilva.portalceo.juridico.previdenciario.workflow.repository;

import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.TransicaoWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransicaoWorkflowRepository extends JpaRepository<TransicaoWorkflow, Long> {
    List<TransicaoWorkflow> findByEtapaOrigem_CodigoAndEtapaDestino_Codigo(EtapaWorkflowCodigo origem, EtapaWorkflowCodigo destino);
    List<TransicaoWorkflow> findByEtapaOrigem_Codigo(EtapaWorkflowCodigo origem);
}

