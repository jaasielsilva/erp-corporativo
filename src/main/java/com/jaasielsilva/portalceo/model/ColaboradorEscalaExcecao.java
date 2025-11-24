package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "colaborador_escala_excecao")
public class ColaboradorEscalaExcecao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(nullable = false)
    private LocalDate data;

    @Column
    private LocalTime entrada1;

    @Column
    private LocalTime saida1;

    @Column
    private LocalTime entrada2;

    @Column
    private LocalTime saida2;

    @Column(length = 500)
    private String motivo;

    @Column(nullable = false)
    private Boolean ativo = true;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) ativo = true;
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }
}
