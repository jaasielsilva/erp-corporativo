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
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campanha_marketing")
public class CampanhaMarketing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O nome da campanha é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCampanha tipo = TipoCampanha.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCampanha status = StatusCampanha.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjetivoCampanha objetivo = ObjetivoCampanha.VENDAS;

    @NotNull(message = "A data de início é obrigatória")
    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column
    private LocalDate dataFim;

    private String observacoes;

    @Column(precision = 15, scale = 2)
    private BigDecimal orcamentoPlanejado = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal orcamentoGasto = BigDecimal.ZERO;

    @Column
    private Integer publicoAlvo = 0;

    @Column
    private Integer alcanceEfetivo = 0;

    @Column
    private Integer cliques = 0;

    @Column
    private Integer conversoes = 0;

    @Column
    private Integer vendas = 0;

    @Column(precision = 15, scale = 2)
    private BigDecimal receitaGerada = BigDecimal.ZERO;

    @Column(length = 1000)
    private String conteudo;

    @Column(length = 200)
    private String assunto;

    @Column(length = 500)
    private String linkDestino;

    @OneToMany(mappedBy = "campanha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CampanhaCliente> clientes;

    @OneToMany(mappedBy = "campanha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CampanhaMetrica> metricas;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;
    private LocalDateTime dataEnvio;

    public enum TipoCampanha {
        EMAIL("E-mail Marketing"),
        SMS("SMS Marketing"),
        WHATSAPP("WhatsApp Marketing"),
        REDES_SOCIAIS("Redes Sociais"),
        GOOGLE_ADS("Google Ads"),
        FACEBOOK_ADS("Facebook Ads"),
        INSTAGRAM_ADS("Instagram Ads"),
        IMPRESSO("Material Impresso"),
        RADIO("Rádio"),
        TV("Televisão"),
        OUTDOOR("Outdoor"),
        EVENTO("Evento"),
        PROMOCAO("Promoção"),
        DESCONTO("Desconto"),
        FIDELIDADE("Programa de Fidelidade");

        private final String descricao;

        TipoCampanha(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusCampanha {
        RASCUNHO("Rascunho"),
        AGENDADA("Agendada"),
        EM_ANDAMENTO("Em Andamento"),
        PAUSADA("Pausada"),
        FINALIZADA("Finalizada"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusCampanha(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum ObjetivoCampanha {
        VENDAS("Aumentar Vendas"),
        LEADS("Gerar Leads"),
        AWARENESS("Conscientização de Marca"),
        ENGAGEMENT("Engajamento"),
        RETENCAO("Retenção de Clientes"),
        FIDELIZACAO("Fidelização"),
        LANCAMENTO("Lançamento de Produto"),
        PROMOCAO("Promoção Especial"),
        REATIVACAO("Reativação de Clientes");

        private final String descricao;

        ObjetivoCampanha(String descricao) {
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
            status = StatusCampanha.RASCUNHO;
        }
        if (tipo == null) {
            tipo = TipoCampanha.EMAIL;
        }
        if (objetivo == null) {
            objetivo = ObjetivoCampanha.VENDAS;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Business methods
    public boolean isRascunho() {
        return status == StatusCampanha.RASCUNHO;
    }

    public boolean isAgendada() {
        return status == StatusCampanha.AGENDADA;
    }

    public boolean isEmAndamento() {
        return status == StatusCampanha.EM_ANDAMENTO;
    }

    public boolean isPausada() {
        return status == StatusCampanha.PAUSADA;
    }

    public boolean isFinalizada() {
        return status == StatusCampanha.FINALIZADA;
    }

    public boolean isCancelada() {
        return status == StatusCampanha.CANCELADA;
    }

    public boolean podeSerEditada() {
        return status == StatusCampanha.RASCUNHO || status == StatusCampanha.AGENDADA;
    }

    public boolean podeSerEnviada() {
        return status == StatusCampanha.RASCUNHO || status == StatusCampanha.AGENDADA;
    }

    public boolean podeSerPausada() {
        return status == StatusCampanha.EM_ANDAMENTO;
    }

    public boolean podeSerRetomada() {
        return status == StatusCampanha.PAUSADA;
    }

    public boolean podeSerCancelada() {
        return status == StatusCampanha.RASCUNHO || status == StatusCampanha.AGENDADA || 
               status == StatusCampanha.EM_ANDAMENTO || status == StatusCampanha.PAUSADA;
    }

    public BigDecimal calcularROI() {
        if (orcamentoGasto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return receitaGerada.subtract(orcamentoGasto)
                .divide(orcamentoGasto, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularCTR() {
        if (alcanceEfetivo == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cliques)
                .divide(BigDecimal.valueOf(alcanceEfetivo), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularTaxaConversao() {
        if (cliques == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(conversoes)
                .divide(BigDecimal.valueOf(cliques), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularCustoAquisicao() {
        if (conversoes == 0) {
            return BigDecimal.ZERO;
        }
        return orcamentoGasto.divide(BigDecimal.valueOf(conversoes), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calcularTicketMedio() {
        if (vendas == 0) {
            return BigDecimal.ZERO;
        }
        return receitaGerada.divide(BigDecimal.valueOf(vendas), 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean isAtiva() {
        LocalDate hoje = LocalDate.now();
        return !isCancelada() && !isFinalizada() && 
               (dataInicio.isBefore(hoje) || dataInicio.isEqual(hoje)) &&
               (dataFim == null || dataFim.isAfter(hoje) || dataFim.isEqual(hoje));
    }

    public boolean isExpirada() {
        return dataFim != null && dataFim.isBefore(LocalDate.now()) && !isFinalizada();
    }

    public long getDuracaoEmDias() {
        if (dataFim == null) {
            return LocalDate.now().toEpochDay() - dataInicio.toEpochDay() + 1;
        }
        return dataFim.toEpochDay() - dataInicio.toEpochDay() + 1;
    }
}