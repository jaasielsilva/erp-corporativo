package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conta_pagar")
@EqualsAndHashCode(callSuper = true)
public class ContaPagar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A descrição é obrigatória")
    @Column(nullable = false, length = 255)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @NotNull(message = "O valor é obrigatório")
    @PositiveOrZero(message = "O valor deve ser positivo ou zero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorOriginal;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorJuros = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @NotNull(message = "A data de vencimento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column
    private LocalDate dataPagamento;

    @NotNull(message = "A data de emissão é obrigatória")
    @Column(nullable = false)
    private LocalDate dataEmissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusContaPagar status = StatusContaPagar.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContaPagar tipo = TipoContaPagar.OPERACIONAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaContaPagar categoria = CategoriaContaPagar.OUTROS;

    @Column(length = 100)
    private String numeroDocumento;

    @Column(length = 50)
    private String formaPagamento;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_aprovacao_id")
    private Usuario usuarioAprovacao;

    @Column
    private LocalDateTime dataAprovacao;

    // ===== NOVO CAMPO =====
    @Column(name = "comprovante_path", length = 255)
    private String comprovantePath;
    
    public String getComprovantePath() {
        return comprovantePath;
    }

    public void setComprovantePath(String comprovantePath) {
        this.comprovantePath = comprovantePath;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folha_pagamento_id")
    private FolhaPagamento folhaPagamento;

    public enum StatusContaPagar {
        PENDENTE("Pendente"),
        APROVADA("Aprovada"),
        PAGA("Paga"),
        CANCELADA("Cancelada"),
        VENCIDA("Vencida");

        private final String descricao;

        StatusContaPagar(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum TipoContaPagar {
        OPERACIONAL("Operacional"),
        INVESTIMENTO("Investimento"),
        FINANCIAMENTO("Financiamento"),
        TRIBUTARIA("Tributária");

        private final String descricao;

        TipoContaPagar(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum CategoriaContaPagar {
        FORNECEDORES("Fornecedores"),
        SALARIOS("Salários"),
        IMPOSTOS("Impostos"),
        ALUGUEL("Aluguel"),
        ENERGIA("Energia"),
        TELEFONE("Telefone"),
        COMBUSTIVEL("Combustível"),
        MANUTENCAO("Manutenção"),
        MARKETING("Marketing"),
        CONSULTORIA("Consultoria"),
        OUTROS("Outros");

        private final String descricao;

        CategoriaContaPagar(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        if (status == null) {
            status = StatusContaPagar.PENDENTE;
        }
        if (tipo == null) {
            tipo = TipoContaPagar.OPERACIONAL;
        }
        if (categoria == null) {
            categoria = CategoriaContaPagar.OUTROS;
        }
        if (dataEmissao == null) {
            dataEmissao = LocalDate.now();
        }
        validarStatus();
    }

    @PreUpdate
    public void onPreUpdate() {
        validarStatus();
    }

    private void validarStatus() {
        // Auto-atualizar status baseado nas datas
        if (status == StatusContaPagar.PENDENTE || status == StatusContaPagar.APROVADA) {
            if (LocalDate.now().isAfter(dataVencimento)) {
                status = StatusContaPagar.VENCIDA;
            }
        }
    }

    // Métodos de negócio
    public BigDecimal getValorTotal() {
        return valorOriginal.add(valorJuros).add(valorMulta).subtract(valorDesconto);
    }

    public BigDecimal getSaldoDevedor() {
        return getValorTotal().subtract(valorPago);
    }

    public boolean isVencida() {
        return LocalDate.now().isAfter(dataVencimento) &&
                (status == StatusContaPagar.PENDENTE || status == StatusContaPagar.APROVADA);
    }

    public boolean isPaga() {
        return status == StatusContaPagar.PAGA;
    }

    public boolean isPendente() {
        return status == StatusContaPagar.PENDENTE;
    }

    public boolean isAprovada() {
        return status == StatusContaPagar.APROVADA;
    }

    public long getDiasAtraso() {
        if (isVencida()) {
            return LocalDate.now().toEpochDay() - dataVencimento.toEpochDay();
        }
        return 0;
    }

    public long getDiasParaVencimento() {
        if (!isPaga() && !isVencida()) {
            return dataVencimento.toEpochDay() - LocalDate.now().toEpochDay();
        }
        return 0;
    }
}