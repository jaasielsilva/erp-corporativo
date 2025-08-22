package com.jaasielsilva.portalceo.model.agenda;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    private LocalDateTime dataHoraInicio;

    private LocalDateTime dataHoraFim;

    private boolean lembrete;

    private String local;

}
