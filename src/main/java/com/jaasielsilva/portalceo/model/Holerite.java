package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@DynamicInsert
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
    private BigDecimal descontoFaltas = BigDecimal.ZERO;

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

    @Column(nullable = false)
    private Integer dependentes = 0;

    @Column
    private Integer faltas = 0;

    @Column
    private Integer atrasos = 0;

    @OneToMany(mappedBy = "holerite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DescontoFolha> descontosAdicionais;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @Column(name = "tipo_folha", length = 32)
    private String tipoFolha;

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

    public BigDecimal getSalarioBase() {
        return salarioBase != null ? salarioBase : BigDecimal.ZERO;
    }

    public BigDecimal getHorasExtras() {
        return horasExtras != null ? horasExtras : BigDecimal.ZERO;
    }

    public BigDecimal getAdicionalNoturno() {
        return adicionalNoturno != null ? adicionalNoturno : BigDecimal.ZERO;
    }

    public BigDecimal getAdicionalPericulosidade() {
        return adicionalPericulosidade != null ? adicionalPericulosidade : BigDecimal.ZERO;
    }

    public BigDecimal getAdicionalInsalubridade() {
        return adicionalInsalubridade != null ? adicionalInsalubridade : BigDecimal.ZERO;
    }

    public BigDecimal getComissoes() {
        return comissoes != null ? comissoes : BigDecimal.ZERO;
    }

    public BigDecimal getBonificacoes() {
        return bonificacoes != null ? bonificacoes : BigDecimal.ZERO;
    }

    public BigDecimal getValeTransporte() {
        return valeTransporte != null ? valeTransporte : BigDecimal.ZERO;
    }

    public BigDecimal getValeRefeicao() {
        return valeRefeicao != null ? valeRefeicao : BigDecimal.ZERO;
    }

    public BigDecimal getAuxilioSaude() {
        return auxilioSaude != null ? auxilioSaude : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoInss() {
        return descontoInss != null ? descontoInss : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoIrrf() {
        return descontoIrrf != null ? descontoIrrf : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoFgts() {
        return descontoFgts != null ? descontoFgts : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoValeTransporte() {
        return descontoValeTransporte != null ? descontoValeTransporte : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoValeRefeicao() {
        return descontoValeRefeicao != null ? descontoValeRefeicao : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoPlanoSaude() {
        return descontoPlanoSaude != null ? descontoPlanoSaude : BigDecimal.ZERO;
    }

    public BigDecimal getDescontoFaltas() {
        return descontoFaltas != null ? descontoFaltas : BigDecimal.ZERO;
    }

    public BigDecimal getOutrosDescontos() {
        return outrosDescontos != null ? outrosDescontos : BigDecimal.ZERO;
    }

    public String getTipoFolha() {
        return tipoFolha != null ? tipoFolha : "normal";
    }

    public Integer getDependentes() {
        return dependentes != null ? dependentes : 0;
    }

    public BigDecimal getBaseInss() {
        return getSalarioBase().add(getHorasExtras());
    }

    public BigDecimal getBaseIrrf() {
        BigDecimal base = getSalarioBase().add(getHorasExtras()).subtract(getDescontoInss());
        if ("ferias".equalsIgnoreCase(getTipoFolha())) {
            base = base.add(getBonificacoes());
        }
        return base.max(BigDecimal.ZERO);
    }

    public BigDecimal getValorFgts() {
        BigDecimal base = getSalarioBase().add(getHorasExtras());
        if ("ferias".equalsIgnoreCase(getTipoFolha())) {
            base = base.add(getBonificacoes());
        }
        return base.multiply(new java.math.BigDecimal("0.08"));
    }

    public boolean hasHorasExtras() { return getHorasExtras().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasAdicionalNoturno() { return getAdicionalNoturno().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasComissoes() { return getComissoes().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasAdicionalPericulosidade() { return getAdicionalPericulosidade().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasAdicionalInsalubridade() { return getAdicionalInsalubridade().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasBonificacoes() { return getBonificacoes().compareTo(java.math.BigDecimal.ZERO) > 0; }

    public boolean hasDescontoInss() { return getDescontoInss().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasDescontoIrrf() { return getDescontoIrrf().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasDescontoPlanoSaude() { return getDescontoPlanoSaude().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasDescontoFaltas() { return getDescontoFaltas().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasDescontoValeRefeicao() { return getDescontoValeRefeicao().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasDescontoValeTransporte() { return getDescontoValeTransporte().compareTo(java.math.BigDecimal.ZERO) > 0; }
    public boolean hasOutrosDescontos() { return getOutrosDescontos().compareTo(java.math.BigDecimal.ZERO) > 0; }

    public boolean isFerias() {
        return "ferias".equalsIgnoreCase(getTipoFolha());
    }

    private void calcularTotais() {
        // Calcular total de proventos
        totalProventos = salarioBase
            .add(getHorasExtras())
            .add(getAdicionalNoturno())
            .add(getAdicionalPericulosidade())
            .add(getAdicionalInsalubridade())
            .add(getComissoes())
            .add(getBonificacoes())
            .add(getValeTransporte())
            .add(getValeRefeicao());
            // auxilioSaude removido dos proventos pois é apenas informativo/subsídio

        // Calcular total de descontos
        totalDescontos = getDescontoInss()
            .add(getDescontoIrrf())
            .add(getDescontoFgts())
            .add(getDescontoValeTransporte())
            .add(getDescontoValeRefeicao())
            .add(getDescontoPlanoSaude())
            .add(getDescontoFaltas())
            .add(getOutrosDescontos());

        // Calcular salário líquido
        salarioLiquido = totalProventos.subtract(totalDescontos);
    }
}
