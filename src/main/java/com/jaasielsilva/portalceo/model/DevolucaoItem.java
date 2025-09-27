package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "devolucao_item")
public class DevolucaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A devolução é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devolucao_id", nullable = false)
    private Devolucao devolucao;

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_item_id")
    private VendaItem vendaItemOriginal;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private Integer quantidadeOriginal;

    @NotNull(message = "O preço unitário é obrigatório")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusItem status = StatusItem.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column
    private CondicaoProduto condicaoProduto;

    @Column(length = 300)
    private String observacoes;

    @Column
    private Boolean reintegrouEstoque = false;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum StatusItem {
        PENDENTE("Pendente"),
        APROVADO("Aprovado"),
        REJEITADO("Rejeitado"),
        PROCESSADO("Processado");

        private final String descricao;

        StatusItem(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum CondicaoProduto {
        PERFEITO("Produto em Perfeito Estado"),
        BOM("Produto em Bom Estado"),
        USADO("Produto Usado"),
        DEFEITUOSO("Produto Defeituoso"),
        DANIFICADO("Produto Danificado");

        private final String descricao;

        CondicaoProduto(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (status == null) {
            status = StatusItem.PENDENTE;
        }
        calcularSubtotal();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularSubtotal();
    }

    private void calcularSubtotal() {
        if (precoUnitario != null && quantidade != null) {
            subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    // Business methods
    public boolean isPendente() {
        return status == StatusItem.PENDENTE;
    }

    public boolean isAprovado() {
        return status == StatusItem.APROVADO;
    }

    public boolean isRejeitado() {
        return status == StatusItem.REJEITADO;
    }

    public boolean isProcessado() {
        return status == StatusItem.PROCESSADO;
    }

    public boolean podeReintegrarEstoque() {
        return isAprovado() && !reintegrouEstoque && 
               (condicaoProduto == CondicaoProduto.PERFEITO || 
                condicaoProduto == CondicaoProduto.BOM);
    }

    public boolean isQuantidadeTotalOriginal() {
        return quantidade.equals(quantidadeOriginal);
    }

    public boolean isQuantidadeParcial() {
        return quantidade < quantidadeOriginal;
    }

    public BigDecimal getPercentualDevolucao() {
        if (quantidadeOriginal == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(quantidade)
                .divide(BigDecimal.valueOf(quantidadeOriginal), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}