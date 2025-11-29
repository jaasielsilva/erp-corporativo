package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "folha_pagamento")
public class FolhaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mesReferencia;

    @Column(nullable = false)
    private Integer anoReferencia;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBruto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDescontos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLiquido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalInss;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIrrf;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFgts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFolha status = StatusFolha.EM_PROCESSAMENTO;

    @Column(nullable = false)
    private LocalDate dataProcessamento;

    @Column
    private LocalDate dataFechamento;

    @ManyToOne
    @JoinColumn(name = "usuario_processamento_id")
    private Usuario usuarioProcessamento;

    @OneToMany(mappedBy = "folhaPagamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Holerite> holerites;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @Column(name = "tipo_folha", length = 32)
    private String tipoFolha;

    public enum StatusFolha {
        EM_PROCESSAMENTO,
        PROCESSADA,
        FECHADA,
        CANCELADA
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusFolha.EM_PROCESSAMENTO;
        }
        if (dataProcessamento == null) {
            dataProcessamento = LocalDate.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }
}
