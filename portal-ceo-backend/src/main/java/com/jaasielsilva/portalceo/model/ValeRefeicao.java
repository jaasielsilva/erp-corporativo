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
@Table(name = "vale_refeicao")
public class ValeRefeicao {

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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotalMes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal percentualDesconto = BigDecimal.valueOf(20); // 20% do valor

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSubsidioEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVale tipo = TipoVale.REFEICAO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusValeRefeicao status = StatusValeRefeicao.ATIVO;

    @Column
    private LocalDate dataAdesao;

    @Column
    private LocalDate dataCancelamento;

    @Column(length = 100)
    private String numeroCartao;

    @Column(length = 100)
    private String operadora;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoVale {
        REFEICAO,
        ALIMENTACAO,
        MISTO
    }

    public enum StatusValeRefeicao {
        ATIVO,
        SUSPENSO,
        CANCELADO
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusValeRefeicao.ATIVO;
        }
        if (tipo == null) {
            tipo = TipoVale.REFEICAO;
        }
        if (percentualDesconto == null) {
            percentualDesconto = BigDecimal.valueOf(20);
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
        if (valorDiario != null && diasUteis != null) {
            // Valor total do mês = dias úteis * valor diário
            valorTotalMes = valorDiario.multiply(BigDecimal.valueOf(diasUteis));

            // Calcular desconto do colaborador
            valorDesconto = valorTotalMes.multiply(percentualDesconto).divide(BigDecimal.valueOf(100));

            // Subsídio da empresa = valor total - desconto do colaborador
            valorSubsidioEmpresa = valorTotalMes.subtract(valorDesconto);
        }
    }

    public String getTipoDescricao() {
        switch (tipo) {
            case REFEICAO:
                return "Vale Refeição";
            case ALIMENTACAO:
                return "Vale Alimentação";
            case MISTO:
                return "Vale Misto";
            default:
                return tipo.toString();
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
        return status == StatusValeRefeicao.ATIVO;
    }

    public BigDecimal getValorLiquidoColaborador() {
        return valorTotalMes.subtract(valorDesconto);
    }
}