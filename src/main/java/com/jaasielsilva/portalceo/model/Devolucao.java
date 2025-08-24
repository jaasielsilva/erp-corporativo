package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "devolucao")
public class Devolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A venda original é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda vendaOriginal;

    @NotNull(message = "O motivo da devolução é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoDevolucao motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDevolucao status = StatusDevolucao.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDevolucao tipo = TipoDevolucao.TOTAL;

    @NotNull(message = "A data da devolução é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataDevolucao;

    @Column(length = 500)
    private String observacoes;

    @Column(length = 500)
    private String motivoDetalhado;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorEstorno = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal taxaDevolucao = BigDecimal.ZERO;

    @OneToMany(mappedBy = "devolucao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DevolucaoItem> itens;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    @ManyToOne
    @JoinColumn(name = "usuario_autorizacao_id")
    private Usuario usuarioAutorizacao;

    @Column
    private LocalDateTime dataAutorizacao;

    @Column
    private LocalDateTime dataProcessamento;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum MotivoDevolucao {
        PRODUTO_DEFEITUOSO("Produto Defeituoso"),
        PRODUTO_INCORRETO("Produto Incorreto"),
        ARREPENDIMENTO("Arrependimento do Cliente"),
        GARANTIA("Problema de Garantia"),
        PRODUTO_DANIFICADO("Produto Danificado no Transporte"),
        NAO_CONFORMIDADE("Não Conformidade com Descrição"),
        OUTROS("Outros Motivos");

        private final String descricao;

        MotivoDevolucao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusDevolucao {
        PENDENTE("Pendente Análise"),
        APROVADA("Aprovada"),
        REJEITADA("Rejeitada"),
        PROCESSADA("Processada"),
        FINALIZADA("Finalizada"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusDevolucao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum TipoDevolucao {
        TOTAL("Devolução Total"),
        PARCIAL("Devolução Parcial"),
        TROCA("Troca de Produto");

        private final String descricao;

        TipoDevolucao(String descricao) {
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
            status = StatusDevolucao.PENDENTE;
        }
        if (tipo == null) {
            tipo = TipoDevolucao.TOTAL;
        }
        if (dataDevolucao == null) {
            dataDevolucao = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Business methods
    public boolean isPendente() {
        return status == StatusDevolucao.PENDENTE;
    }

    public boolean isAprovada() {
        return status == StatusDevolucao.APROVADA;
    }

    public boolean isProcessada() {
        return status == StatusDevolucao.PROCESSADA;
    }

    public boolean isFinalizada() {
        return status == StatusDevolucao.FINALIZADA;
    }

    public boolean podeSerAprovada() {
        return status == StatusDevolucao.PENDENTE;
    }

    public boolean podeSerProcessada() {
        return status == StatusDevolucao.APROVADA;
    }

    public boolean podeSerCancelada() {
        return status == StatusDevolucao.PENDENTE || status == StatusDevolucao.APROVADA;
    }

    public BigDecimal getValorLiquido() {
        return valorTotal.subtract(taxaDevolucao);
    }

    public boolean isTotal() {
        return tipo == TipoDevolucao.TOTAL;
    }

    public boolean isParcial() {
        return tipo == TipoDevolucao.PARCIAL;
    }

    public boolean isTroca() {
        return tipo == TipoDevolucao.TROCA;
    }

    public long getDiasDesdeVenda() {
        if (vendaOriginal != null && vendaOriginal.getDataVenda() != null) {
            return java.time.Duration.between(vendaOriginal.getDataVenda(), dataDevolucao).toDays();
        }
        return 0;
    }

    public boolean isForaPrazo(int diasLimite) {
        return getDiasDesdeVenda() > diasLimite;
    }
}