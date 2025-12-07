package com.jaasielsilva.portalceo.model.treinamentos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rh_treinamentos_frequencias", indexes = {
        @Index(name = "idx_treinamento_frequencia_matricula", columnList = "matricula_id")
})
@Getter
@Setter
public class TreinamentoFrequencia extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id", nullable = false)
    private TreinamentoMatricula matricula;
    private LocalDateTime dataHora;
    private Boolean presente;
    @Column(columnDefinition = "TEXT")
    private String observacoes;
}

