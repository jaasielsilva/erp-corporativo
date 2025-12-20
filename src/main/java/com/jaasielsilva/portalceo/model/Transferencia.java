package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transferencia")
@EqualsAndHashCode(callSuper = true)
public class Transferencia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A conta de origem é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private ContaBancaria contaOrigem;

    @NotNull(message = "A conta de destino é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private ContaBancaria contaDestino;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    private LocalDate dataTransferencia;

    @Column(length = 255)
    private String descricao;

    @Column(length = 500)
    private String observacoes;

    // Construtor utilitário
    public Transferencia(ContaBancaria origem, ContaBancaria destino, BigDecimal valor, LocalDate data, String descricao) {
        this.contaOrigem = origem;
        this.contaDestino = destino;
        this.valor = valor;
        this.dataTransferencia = data;
        this.descricao = descricao;
    }
}
