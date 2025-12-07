package com.jaasielsilva.portalceo.model.recrutamento;

import com.jaasielsilva.portalceo.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rh_recrutamento_entrevistas")
@Getter
@Setter
public class RecrutamentoEntrevista extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidatura_id", nullable = false)
    private RecrutamentoCandidatura candidatura;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    @Column(length = 120)
    private String local;
    @Column(length = 40)
    private String tipo;
    private Long agendaEventoId;
}

