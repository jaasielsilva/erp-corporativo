package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conta_bancaria")
@EqualsAndHashCode(callSuper = true)
public class ContaBancaria extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O nome da conta é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 50)
    private String numeroConta;

    @Column(length = 50)
    private String agencia;

    @Column(length = 50)
    private String banco;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "ativo")
    private Boolean ativo = true;

    // Construtor utilitário
    public ContaBancaria(String nome, BigDecimal saldoInicial) {
        this.nome = nome;
        this.saldo = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        this.ativo = true;
    }
}
