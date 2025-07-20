package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    private Integer quantidadeContada;

    private LocalDate data;

    private String responsavel;

    private String observacoes;
}
