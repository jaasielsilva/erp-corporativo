package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vale_transporte")
public class ValeTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @Column(nullable = false)
    private Integer mesReferencia;

    @Column(nullable = false)
    private Integer anoReferencia;

    @Column(nullable = false)
    private Integer diasUteis;

    @Column(nullable = false)
    private Integer viagensDia = 2; // ida e volta

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPassagem;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalMes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal percentualDesconto = BigDecimal.valueOf(6); // 6% do salário

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSubsidioEmpresa;

    @Column(length = 200)
    private String linhaOnibus;

    @Column(length = 200)
    private String enderecoOrigem;

    @Column(length = 200)
    private String enderecoDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusValeTransporte status = StatusValeTransporte.ATIVO;

    @Column
    private LocalDate dataAdesao;

    @Column
    private LocalDate dataCancelamento;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum StatusValeTransporte {
        ATIVO,
        SUSPENSO,
        CANCELADO
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusValeTransporte.ATIVO;
        }
        if (viagensDia == null) {
            viagensDia = 2;
        }
        if (percentualDesconto == null) {
            percentualDesconto = BigDecimal.valueOf(6);
        }
        if (dataAdesao == null) {
            dataAdesao = LocalDate.now();
        }
        calcularValores();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularValores();
    }

    private void calcularValores() {
        if (valorPassagem != null && diasUteis != null && viagensDia != null) {
            // Valor total do mês = dias úteis * viagens por dia * valor da passagem
            valorTotalMes = valorPassagem
                .multiply(BigDecimal.valueOf(diasUteis))
                .multiply(BigDecimal.valueOf(viagensDia));

            // Calcular desconto baseado no salário do colaborador (máximo 6%)
            if (colaborador != null && colaborador.getCargo() != null) {
                // Aqui seria necessário ter o salário no modelo Colaborador ou Cargo
                // Por enquanto, vamos usar um valor fixo para demonstração
                BigDecimal salarioBase = BigDecimal.valueOf(1500); // Valor exemplo
                BigDecimal descontoMaximo = salarioBase.multiply(percentualDesconto).divide(BigDecimal.valueOf(100));
                
                valorDesconto = valorTotalMes.min(descontoMaximo);
            } else {
                valorDesconto = BigDecimal.ZERO;
            }

            // Subsídio da empresa = valor total - desconto do colaborador
            valorSubsidioEmpresa = valorTotalMes.subtract(valorDesconto);
        }
    }

    public String getStatusDescricao() {
        switch (status) {
            case ATIVO:
                return "Ativo";
            case SUSPENSO:
                return "Suspenso";
            case CANCELADO:
                return "Cancelado";
            default:
                return status.toString();
        }
    }

    public boolean isAtivo() {
        return status == StatusValeTransporte.ATIVO;
    }

    public Integer getTotalViagensmes() {
        return diasUteis * viagensDia;
    }
}