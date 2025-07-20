package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    private Integer quantidade;

    private String localOrigem;

    private String localDestino;

    private LocalDateTime dataHora;

    private String responsavel;

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
    }
}
