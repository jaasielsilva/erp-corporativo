package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rh_avaliacoes_desenpenho", indexes = {
        @Index(name = "idx_avaliacao_status", columnList = "status"),
        @Index(name = "idx_avaliacao_colaborador", columnList = "colaborador_id")
})
@Getter
@Setter
public class AvaliacaoDesempenho extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliador_id")
    private Usuario avaliador;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private LocalDate periodoFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusAvaliacao status = StatusAvaliacao.ABERTA;

    @Column(name = "nota")
    private Double nota;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "data_submissao")
    private LocalDateTime dataSubmissao;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    public enum StatusAvaliacao {
        ABERTA,
        SUBMETIDA,
        APROVADA,
        REPROVADA
    }
}

