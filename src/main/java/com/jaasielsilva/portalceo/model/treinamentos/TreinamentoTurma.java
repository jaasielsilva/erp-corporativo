package com.jaasielsilva.portalceo.model.treinamentos;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rh_treinamentos_turmas", indexes = {
        @Index(name = "idx_treinamento_turma_status", columnList = "status")
})
@Getter
@Setter
public class TreinamentoTurma extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private TreinamentoCurso curso;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_id")
    private TreinamentoInstrutor instrutor;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    @Column(length = 120)
    private String local;
    private Integer capacidade;
    @Column(length = 20)
    private String status;
    private Long agendaEventoId;
}

