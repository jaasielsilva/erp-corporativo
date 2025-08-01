package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "correcao_ponto")
public class CorrecaoPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_ponto_id", nullable = false)
    private RegistroPonto registroPonto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_solicitante_id", nullable = false)
    private Colaborador colaboradorSolicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCorrecao tipoCorrecao;

    @Column
    private LocalTime horarioAnterior;

    @Column
    private LocalTime horarioNovo;

    @Column(nullable = false, length = 1000)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCorrecao status = StatusCorrecao.PENDENTE;

    @Column(length = 1000)
    private String observacaoAprovador;

    @ManyToOne
    @JoinColumn(name = "usuario_aprovador_id")
    private Usuario usuarioAprovador;

    @Column
    private LocalDateTime dataAprovacao;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoCorrecao {
        ENTRADA_1,
        SAIDA_1,
        ENTRADA_2,
        SAIDA_2,
        ENTRADA_3,
        SAIDA_3,
        ENTRADA_4,
        SAIDA_4,
        ABONO_FALTA,
        JUSTIFICATIVA_ATRASO
    }

    public enum StatusCorrecao {
        PENDENTE,
        APROVADA,
        REJEITADA,
        CANCELADA
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusCorrecao.PENDENTE;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        if (status == StatusCorrecao.APROVADA || status == StatusCorrecao.REJEITADA) {
            if (dataAprovacao == null) {
                dataAprovacao = LocalDateTime.now();
            }
        }
    }

    public String getTipoCorrecaoDescricao() {
        switch (tipoCorrecao) {
            case ENTRADA_1:
                return "1ª Entrada";
            case SAIDA_1:
                return "1ª Saída";
            case ENTRADA_2:
                return "2ª Entrada";
            case SAIDA_2:
                return "2ª Saída";
            case ENTRADA_3:
                return "3ª Entrada";
            case SAIDA_3:
                return "3ª Saída";
            case ENTRADA_4:
                return "4ª Entrada";
            case SAIDA_4:
                return "4ª Saída";
            case ABONO_FALTA:
                return "Abono de Falta";
            case JUSTIFICATIVA_ATRASO:
                return "Justificativa de Atraso";
            default:
                return tipoCorrecao.toString();
        }
    }

    public String getStatusDescricao() {
        switch (status) {
            case PENDENTE:
                return "Pendente";
            case APROVADA:
                return "Aprovada";
            case REJEITADA:
                return "Rejeitada";
            case CANCELADA:
                return "Cancelada";
            default:
                return status.toString();
        }
    }

    public String getStatusCor() {
        switch (status) {
            case PENDENTE:
                return "warning";
            case APROVADA:
                return "success";
            case REJEITADA:
                return "danger";
            case CANCELADA:
                return "secondary";
            default:
                return "primary";
        }
    }

    public boolean isPendente() {
        return status == StatusCorrecao.PENDENTE;
    }

    public boolean isAprovada() {
        return status == StatusCorrecao.APROVADA;
    }

    public boolean isRejeitada() {
        return status == StatusCorrecao.REJEITADA;
    }

    public boolean isCancelada() {
        return status == StatusCorrecao.CANCELADA;
    }

    public boolean podeSerEditada() {
        return status == StatusCorrecao.PENDENTE;
    }

    public boolean podeSerCancelada() {
        return status == StatusCorrecao.PENDENTE;
    }
}