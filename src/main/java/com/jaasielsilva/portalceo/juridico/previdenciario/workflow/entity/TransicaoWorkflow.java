package com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "juridico_previd_transicao_workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransicaoWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_origem_id", nullable = false)
    private EtapaWorkflow etapaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_destino_id", nullable = false)
    private EtapaWorkflow etapaDestino;

    @Column(name = "role_permitida", nullable = false, length = 80)
    private String rolePermitida;
}

