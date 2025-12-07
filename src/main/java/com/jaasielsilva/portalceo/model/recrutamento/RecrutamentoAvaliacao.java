package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_recrutamento_avaliacoes")
@Getter
@Setter
public class RecrutamentoAvaliacao extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidatura_id", nullable = false)
    private RecrutamentoCandidatura candidatura;
    private Integer nota;
    @Column(columnDefinition = "TEXT")
    private String feedback;
}

