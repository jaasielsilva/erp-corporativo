package com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity;

import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "juridico_previd_processo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoPrevidenciario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_atual", nullable = false, length = 30)
    private ProcessoPrevidenciarioStatus statusAtual = ProcessoPrevidenciarioStatus.ABERTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_atual", nullable = false, length = 30)
    private EtapaWorkflowCodigo etapaAtual = EtapaWorkflowCodigo.CADASTRO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;

    @PrePersist
    public void prePersist() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        if (statusAtual == null) {
            statusAtual = ProcessoPrevidenciarioStatus.ABERTO;
        }
        if (etapaAtual == null) {
            etapaAtual = EtapaWorkflowCodigo.CADASTRO;
        }
    }
}
