package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rh_recrutamento_candidaturas")
@Getter
@Setter
public class RecrutamentoCandidatura extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", nullable = false)
    private RecrutamentoCandidato candidato;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private RecrutamentoVaga vaga;
    @Column(length = 20)
    private String etapa;
    private LocalDateTime inicio;
    private LocalDateTime conclusao;
    @Column(length = 120)
    private String origem;
}

