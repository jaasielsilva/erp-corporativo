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
@Table(name = "plano_saude")
public class PlanoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String operadora;

    @Column(length = 20)
    private String codigo;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlano tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTitular;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDependente;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentualEmpresa = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentualColaborador = BigDecimal.valueOf(100);

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 500)
    private String observacoes;

    @OneToMany(mappedBy = "planoSaude", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdesaoPlanoSaude> adesoes;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    @Column(length = 500)
    private String cobertura;

    @Column(length = 500)
    private String redeCredenciada;

    @Column(length = 100)
    private String carencia;

    public BigDecimal getValorMensal() {
        return this.valorTitular;
    }

    public enum TipoPlano {
        BASICO,
        INTERMEDIARIO,
        PREMIUM,
        EXECUTIVO
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
        if (percentualEmpresa == null) {
            percentualEmpresa = BigDecimal.ZERO;
        }
        if (percentualColaborador == null) {
            percentualColaborador = BigDecimal.valueOf(100);
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    public String getTipoDescricao() {
        switch (tipo) {
            case BASICO:
                return "Básico";
            case INTERMEDIARIO:
                return "Intermediário";
            case PREMIUM:
                return "Premium";
            case EXECUTIVO:
                return "Executivo";
            default:
                return tipo.toString();
        }
    }

    public BigDecimal calcularValorColaboradorTitular() {
        return valorTitular.multiply(percentualColaborador).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularValorColaboradorDependente() {
        return valorDependente.multiply(percentualColaborador).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularValorEmpresaTitular() {
        return valorTitular.multiply(percentualEmpresa).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularValorEmpresaDependente() {
        return valorDependente.multiply(percentualEmpresa).divide(BigDecimal.valueOf(100));
    }
}