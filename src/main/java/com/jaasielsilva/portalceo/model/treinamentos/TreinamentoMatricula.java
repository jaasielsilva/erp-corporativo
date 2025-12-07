package com.jaasielsilva.portalceo.model.treinamentos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import com.jaasielsilva.portalceo.model.Colaborador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rh_treinamentos_matriculas", indexes = {
        @Index(name = "idx_treinamento_matricula_status", columnList = "status")
})
@Getter
@Setter
public class TreinamentoMatricula extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private TreinamentoTurma turma;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;
    @Column(length = 20)
    private String status;
    private LocalDateTime dataMatricula;
}

