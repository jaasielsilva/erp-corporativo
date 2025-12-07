package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rh_solicitacoes_ferias", indexes = {
        @Index(name = "idx_ferias_status", columnList = "status"),
        @Index(name = "idx_ferias_colaborador", columnList = "colaborador_id")
})
@Getter
@Setter
public class SolicitacaoFerias extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSolicitacao status = StatusSolicitacao.SOLICITADA;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_aprovacao_id")
    private Usuario usuarioAprovacao;

    public enum StatusSolicitacao {
        SOLICITADA,
        APROVADA,
        REPROVADA,
        REGISTRADA
    }
}

