package com.jaasielsilva.portalceo.model.treinamentos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rh_treinamentos_avaliacoes")
@Getter
@Setter
public class TreinamentoAvaliacao extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id", nullable = false)
    private TreinamentoMatricula matricula;
    private Integer nota;
    @Column(columnDefinition = "TEXT")
    private String feedback;
}

