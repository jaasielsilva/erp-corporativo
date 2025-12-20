package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "fluxo_caixa")
public class FluxoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A descrição é obrigatória")
    @Column(nullable = false, length = 255)
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimento tipoMovimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaFluxo categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFluxo status = StatusFluxo.REALIZADO;

    @Column(length = 100)
    private String numeroDocumento;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_pagar_id")
    private ContaPagar contaPagar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_receber_id")
    private ContaReceber contaReceber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id")
    private ContaBancaria contaBancaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id")
    private Transferencia transferencia;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoMovimento {
        ENTRADA("Entrada"),
        SAIDA("Saída");

        private final String descricao;

        TipoMovimento(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum CategoriaFluxo {
        VENDAS("Vendas"),
        SERVICOS("Serviços"),
        JUROS_RECEBIDOS("Juros Recebidos"),
        OUTRAS_RECEITAS("Outras Receitas"),
        FORNECEDORES("Fornecedores"),
        SALARIOS("Salários"),
        IMPOSTOS("Impostos"),
        ALUGUEL("Aluguel"),
        ENERGIA("Energia"),
        TELEFONE("Telefone"),
        COMBUSTIVEL("Combustível"),
        MANUTENCAO("Manutenção"),
        MARKETING("Marketing"),
        FINANCIAMENTO("Financiamento"),
        INVESTIMENTO("Investimento"),
        TRANSFERENCIA("Transferência"),
        OUTRAS_DESPESAS("Outras Despesas");

        private final String descricao;

        CategoriaFluxo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        public boolean isReceita() {
            return this == VENDAS || this == SERVICOS || this == JUROS_RECEBIDOS || this == OUTRAS_RECEITAS;
        }

        public boolean isDespesa() {
            return !isReceita();
        }
    }

    public enum StatusFluxo {
        PREVISTO("Previsto"),
        REALIZADO("Realizado"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusFluxo(String descricao) {
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
            status = StatusFluxo.REALIZADO;
        }
        if (data == null) {
            data = LocalDate.now();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Métodos de negócio
    public BigDecimal getValorEntrada() {
        return tipoMovimento == TipoMovimento.ENTRADA ? valor : BigDecimal.ZERO;
    }

    public BigDecimal getValorSaida() {
        return tipoMovimento == TipoMovimento.SAIDA ? valor : BigDecimal.ZERO;
    }

    public boolean isEntrada() {
        return tipoMovimento == TipoMovimento.ENTRADA;
    }

    public boolean isSaida() {
        return tipoMovimento == TipoMovimento.SAIDA;
    }

    public boolean isPrevisto() {
        return status == StatusFluxo.PREVISTO;
    }

    public boolean isRealizado() {
        return status == StatusFluxo.REALIZADO;
    }

    public boolean isCancelado() {
        return status == StatusFluxo.CANCELADO;
    }
}