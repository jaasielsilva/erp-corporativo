package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "contrato_aditivo")
public class ContratoAditivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O contrato é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoLegal contrato;

    @NotNull(message = "O número do aditivo é obrigatório")
    @Size(min = 1, max = 20, message = "O número deve ter entre 1 e 20 caracteres")
    @Column(nullable = false, length = 20)
    private String numeroAditivo;

    @NotNull(message = "O tipo do aditivo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAditivo tipo;

    @NotNull(message = "O título é obrigatório")
    @Size(min = 5, max = 200, message = "O título deve ter entre 5 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    @Column(length = 1000)
    private String descricao;

    @NotNull(message = "A data de criação é obrigatória")
    @Column(nullable = false)
    private LocalDate dataCriacao;

    @Column
    private LocalDate dataAssinatura;

    @Column
    private LocalDate dataVigencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAditivo status = StatusAditivo.RASCUNHO;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorAnterior;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorNovo;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorDiferenca;

    @Column
    private LocalDate dataVencimentoAnterior;

    @Column
    private LocalDate dataVencimentoNova;

    @Column
    private Integer duracaoAnterior;

    @Column
    private Integer duracaoNova;

    @Column(length = 500)
    private String clausulasAlteradas;

    @Column(length = 1000)
    private String justificativa;

    @Column(length = 500)
    private String caminhoArquivo;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    @ManyToOne
    @JoinColumn(name = "usuario_aprovacao_id")
    private Usuario usuarioAprovacao;

    @Column
    private LocalDateTime dataAprovacao;

    private LocalDateTime dataUltimaEdicao;

    public enum TipoAditivo {
        PRAZO("Aditivo de Prazo"),
        VALOR("Aditivo de Valor"),
        ESCOPO("Aditivo de Escopo"),
        CLAUSULA("Alteração de Cláusula"),
        RESCISAO("Aditivo de Rescisão"),
        RENOVACAO("Aditivo de Renovação"),
        SUSPENSAO("Aditivo de Suspensão"),
        REATIVACAO("Aditivo de Reativação"),
        OUTROS("Outros");

        private final String descricao;

        TipoAditivo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusAditivo {
        RASCUNHO("Rascunho"),
        EM_ANALISE("Em Análise"),
        APROVADO("Aprovado"),
        ASSINADO("Assinado"),
        VIGENTE("Vigente"),
        REJEITADO("Rejeitado"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusAditivo(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDate.now();
        }
        if (status == null) {
            status = StatusAditivo.RASCUNHO;
        }
        calcularDiferenca();
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        calcularDiferenca();
    }

    private void calcularDiferenca() {
        if (valorAnterior != null && valorNovo != null) {
            valorDiferenca = valorNovo.subtract(valorAnterior);
        }
    }

    // Business methods
    public boolean isRascunho() {
        return status == StatusAditivo.RASCUNHO;
    }

    public boolean isEmAnalise() {
        return status == StatusAditivo.EM_ANALISE;
    }

    public boolean isAprovado() {
        return status == StatusAditivo.APROVADO;
    }

    public boolean isAssinado() {
        return status == StatusAditivo.ASSINADO;
    }

    public boolean isVigente() {
        return status == StatusAditivo.VIGENTE;
    }

    public boolean isRejeitado() {
        return status == StatusAditivo.REJEITADO;
    }

    public boolean isCancelado() {
        return status == StatusAditivo.CANCELADO;
    }

    public boolean podeSerEditado() {
        return status == StatusAditivo.RASCUNHO;
    }

    public boolean podeSerAprovado() {
        return status == StatusAditivo.EM_ANALISE;
    }

    public boolean podeSerAssinado() {
        return status == StatusAditivo.APROVADO;
    }

    public boolean podeSerCancelado() {
        return status == StatusAditivo.RASCUNHO || status == StatusAditivo.EM_ANALISE || status == StatusAditivo.APROVADO;
    }

    public boolean isAumentoValor() {
        return valorDiferenca != null && valorDiferenca.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isReducaoValor() {
        return valorDiferenca != null && valorDiferenca.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isAlteracaoPrazo() {
        return tipo == TipoAditivo.PRAZO && duracaoAnterior != null && duracaoNova != null;
    }

    public boolean isProrrogacao() {
        return isAlteracaoPrazo() && duracaoNova > duracaoAnterior;
    }

    public boolean isReducaoPrazo() {
        return isAlteracaoPrazo() && duracaoNova < duracaoAnterior;
    }

    public long getDiferencaPrazo() {
        if (duracaoAnterior != null && duracaoNova != null) {
            return duracaoNova - duracaoAnterior;
        }
        return 0;
    }

    public BigDecimal getPercentualAlteracaoValor() {
        if (valorAnterior == null || valorAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorDiferenca.divide(valorAnterior, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public String getDescricaoCompleta() {
        return String.format("Aditivo %s - %s (%s)", numeroAditivo, titulo, tipo.getDescricao());
    }

    public boolean necessitaAprovacao() {
        // Define criteria for when an addendum requires approval
        if (tipo == TipoAditivo.VALOR && valorDiferenca != null) {
            return valorDiferenca.abs().compareTo(BigDecimal.valueOf(10000)) > 0; // Above R$ 10,000
        }
        if (tipo == TipoAditivo.PRAZO && duracaoAnterior != null && duracaoNova != null) {
            return Math.abs(duracaoNova - duracaoAnterior) > 6; // More than 6 months
        }
        return tipo == TipoAditivo.ESCOPO || tipo == TipoAditivo.RESCISAO;
    }
}