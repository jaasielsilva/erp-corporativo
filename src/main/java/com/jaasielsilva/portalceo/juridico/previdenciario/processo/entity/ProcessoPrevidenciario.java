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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoDecisaoResultado;

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

    @Column(name = "numero_protocolo", length = 60)
    private String numeroProtocolo;

    @Column(name = "data_protocolo")
    private LocalDate dataProtocolo;

    @Column(name = "url_meu_inss", length = 500)
    private String urlMeuInss;

    @Column(name = "valor_causa")
    private BigDecimal valorCausa;

    @Column(name = "valor_concedido")
    private BigDecimal valorConcedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_decisao", length = 30)
    private ProcessoDecisaoResultado resultadoDecisao;

    @Column(name = "ganhou_causa")
    private Boolean ganhouCausa;

    @Column(name = "data_decisao")
    private LocalDate dataDecisao;

    @Column(name = "observacao_decisao", length = 1000)
    private String observacaoDecisao;

    @Column(name = "data_envio_documentacao")
    private LocalDateTime dataEnvioDocumentacao;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @Column(name = "pendencia_analise")
    private Boolean pendenciaAnalise = false;

    @Column(name = "status_contrato", length = 30)
    private String statusContrato; // NAO_ENVIADO, ENVIADO, ASSINADO

    @Column(name = "data_envio_contrato")
    private LocalDateTime dataEnvioContrato;

    @Column(name = "data_assinatura_contrato")
    private LocalDateTime dataAssinaturaContrato;

    @Column(name = "status_medico", length = 30)
    private String statusMedico; // PENDENTE, PAGO, LAUDO_EMITIDO

    @Column(name = "data_pagamento_medico")
    private LocalDateTime dataPagamentoMedico;

    @Column(name = "data_laudo_medico")
    private LocalDateTime dataLaudoMedico;

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
