package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "conta_receber")
public class ContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A descrição é obrigatória")
    @Column(nullable = false, length = 255)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorOriginal;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorRecebido = BigDecimal.ZERO;

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
    private LocalDate dataRecebimento;

    @NotNull(message = "A data de emissão é obrigatória")
    @Column(nullable = false)
    private LocalDate dataEmissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusContaReceber status = StatusContaReceber.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContaReceber tipo = TipoContaReceber.VENDA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaContaReceber categoria = CategoriaContaReceber.PRODUTO;

    @Column(length = 100)
    private String numeroDocumento;

    @Column(length = 50)
    private String formaRecebimento;

    @Column(length = 500)
    private String observacoes;

    @Column
    private Integer parcelaAtual;

    @Column
    private Integer totalParcelas;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum StatusContaReceber {
        PENDENTE("Pendente"),
        RECEBIDA("Recebida"),
        PARCIAL("Parcialmente Recebida"),
        CANCELADA("Cancelada"),
        VENCIDA("Vencida"),
        INADIMPLENTE("Inadimplente");

        private final String descricao;

        StatusContaReceber(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum TipoContaReceber {
        VENDA("Venda"),
        SERVICO("Serviço"),
        JUROS("Juros"),
        OUTROS("Outros");

        private final String descricao;

        TipoContaReceber(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum CategoriaContaReceber {
        PRODUTO("Venda de Produtos"),
        SERVICO("Prestação de Serviços"),
        CONSULTORIA("Consultoria"),
        MANUTENCAO("Manutenção"),
        OUTROS("Outros");

        private final String descricao;

        CategoriaContaReceber(String descricao) {
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
            status = StatusContaReceber.PENDENTE;
        }
        if (tipo == null) {
            tipo = TipoContaReceber.VENDA;
        }
        if (categoria == null) {
            categoria = CategoriaContaReceber.PRODUTO;
        }
        if (dataEmissao == null) {
            dataEmissao = LocalDate.now();
        }
        validarStatus();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        validarStatus();
    }

    private void validarStatus() {
        // Auto-atualizar status baseado nas datas e valores
        if (status == StatusContaReceber.PENDENTE || status == StatusContaReceber.PARCIAL) {
            if (LocalDate.now().isAfter(dataVencimento)) {
                long diasAtraso = LocalDate.now().toEpochDay() - dataVencimento.toEpochDay();
                if (diasAtraso > 30) {
                    status = StatusContaReceber.INADIMPLENTE;
                } else {
                    status = StatusContaReceber.VENCIDA;
                }
            }
        }

        // Verificar se foi paga parcialmente ou totalmente
        if (valorRecebido.compareTo(BigDecimal.ZERO) > 0) {
            if (valorRecebido.compareTo(getValorTotal()) >= 0) {
                status = StatusContaReceber.RECEBIDA;
                if (dataRecebimento == null) {
                    dataRecebimento = LocalDate.now();
                }
            } else {
                status = StatusContaReceber.PARCIAL;
            }
        }
    }

    // Métodos de negócio
    public BigDecimal getValorTotal() {
        return valorOriginal.add(valorJuros).add(valorMulta).subtract(valorDesconto);
    }

    public BigDecimal getSaldoReceber() {
        return getValorTotal().subtract(valorRecebido);
    }

    public boolean isVencida() {
        return LocalDate.now().isAfter(dataVencimento) && 
               (status == StatusContaReceber.PENDENTE || status == StatusContaReceber.PARCIAL);
    }

    public boolean isRecebida() {
        return status == StatusContaReceber.RECEBIDA;
    }

    public boolean isPendente() {
        return status == StatusContaReceber.PENDENTE;
    }

    public boolean isInadimplente() {
        return status == StatusContaReceber.INADIMPLENTE;
    }

    public long getDiasAtraso() {
        if (isVencida() || isInadimplente()) {
            return LocalDate.now().toEpochDay() - dataVencimento.toEpochDay();
        }
        return 0;
    }

    public long getDiasParaVencimento() {
        if (!isRecebida() && !isVencida() && !isInadimplente()) {
            return dataVencimento.toEpochDay() - LocalDate.now().toEpochDay();
        }
        return 0;
    }

    public BigDecimal getPercentualRecebido() {
        if (getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorRecebido.divide(getValorTotal(), 4, java.math.RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100));
    }

    public String getDescricaoParcela() {
        if (parcelaAtual != null && totalParcelas != null) {
            return parcelaAtual + "/" + totalParcelas;
        }
        return "";
    }
}