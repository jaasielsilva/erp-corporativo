package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "holerite")
public class Holerite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folha_pagamento_id", nullable = false)
    private FolhaPagamento folhaPagamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Column(precision = 10, scale = 2)
    private BigDecimal horasExtras = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal adicionalNoturno = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal adicionalPericulosidade = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal adicionalInsalubridade = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal comissoes = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal bonificacoes = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal valeTransporte = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal valeRefeicao = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal auxilioSaude = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProventos;

    @Column(precision = 10, scale = 2)
    private BigDecimal descontoInss = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal descontoIrrf = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal descontoFgts = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal descontoValeTransporte = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal descontoValeRefeicao = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal descontoPlanoSaude = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal outrosDescontos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDescontos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioLiquido;

    @Column(nullable = false)
    private Integer diasTrabalhados;

    @Column(nullable = false)
    private Integer horasTrabalhadas;

    @Column
    private Integer faltas = 0;

    @Column
    private Integer atrasos = 0;

    @OneToMany(mappedBy = "holerite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DescontoFolha> descontosAdicionais;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        calcularTotais();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularTotais();
    }

    private void calcularTotais() {
        // Calcular total de proventos
        totalProventos = salarioBase
            .add(horasExtras)
            .add(adicionalNoturno)
            .add(adicionalPericulosidade)
            .add(adicionalInsalubridade)
            .add(comissoes)
            .add(bonificacoes)
            .add(valeTransporte)
            .add(valeRefeicao)
            .add(auxilioSaude);

        // Calcular total de descontos
        totalDescontos = descontoInss
            .add(descontoIrrf)
            .add(descontoFgts)
            .add(descontoValeTransporte)
            .add(descontoValeRefeicao)
            .add(descontoPlanoSaude)
            .add(outrosDescontos);

        // Calcular salário líquido
        salarioLiquido = totalProventos.subtract(totalDescontos);
    }
}