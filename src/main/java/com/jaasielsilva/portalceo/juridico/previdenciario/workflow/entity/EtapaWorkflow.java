package com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "juridico_previd_etapa_workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtapaWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private EtapaWorkflowCodigo codigo;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "permite_anexo", nullable = false)
    private Boolean permiteAnexo = false;
}

