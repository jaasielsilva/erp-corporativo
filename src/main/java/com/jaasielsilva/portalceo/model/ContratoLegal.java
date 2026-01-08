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
@Table(name = "contrato_legal")
public class ContratoLegal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O número do contrato é obrigatório")
    @Size(min = 3, max = 50, message = "O número deve ter entre 3 e 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String numeroContrato;

    @NotNull(message = "O título do contrato é obrigatório")
    @Size(min = 5, max = 200, message = "O título deve ter entre 5 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContrato tipo = TipoContrato.PRESTACAO_SERVICOS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusContrato status = StatusContrato.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeContrato prioridade = PrioridadeContrato.MEDIA;

    @NotNull(message = "A data de criação é obrigatória")
    @Column(nullable = false)
    private LocalDate dataCriacao;

    @Column
    private LocalDate dataAssinatura;

    @NotNull(message = "A data de início é obrigatória")
    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column
    private LocalDate dataFim;

    @Column
    private LocalDate dataVencimento;

    @Column
    private LocalDate dataRenovacao;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorContrato = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal valorMensal = BigDecimal.ZERO;

    @Column
    private Integer duracaoMeses = 12;

    @Column
    private Boolean renovacaoAutomatica = false;

    @Column
    private Integer prazoNotificacao = 30; // dias

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(length = 200)
    private String parteContratante;

    @Column(length = 200)
    private String parteContratada;

    @Column(length = 100)
    private String responsavelLegal;

    @Column(length = 100)
    private String testemunha1;

    @Column(length = 100)
    private String testemunha2;

    @Column(length = 500)
    private String caminhoArquivo;

    @Column(length = 100)
    private String autentiqueId;

    @Column(length = 500)
    private String linkAssinatura;

    @Column(length = 100)
    private String versaoContrato = "1.0";

    @Column(length = 2000)
    private String clausulasEspeciais;

    @Column(length = 1000)
    private String condicoesRescisao;

    @Column(length = 500)
    private String penalidades;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoAditivo> aditivos;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoAlerta> alertas;

    @ManyToOne
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    private LocalDateTime dataUltimaEdicao;
    private LocalDateTime dataUltimaRevisao;

    public enum TipoContrato {
        PRESTACAO_SERVICOS("Prestação de Serviços"),
        FORNECIMENTO_PRODUTOS("Fornecimento de Produtos"),
        LOCACAO("Locação"),
        COMPRA_VENDA("Compra e Venda"),
        PARCERIA("Parceria Comercial"),
        LICENCIAMENTO("Licenciamento"),
        MANUTENCAO("Manutenção"),
        CONSULTORIA("Consultoria"),
        DESENVOLVIMENTO("Desenvolvimento"),
        MARKETING("Marketing e Publicidade"),
        TERCEIRIZACAO("Terceirização"),
        FRANQUIA("Franquia"),
        DISTRIBUICAO("Distribuição"),
        TRABALHO("Contrato de Trabalho"),
        CONFIDENCIALIDADE("Acordo de Confidencialidade"),
        OUTROS("Outros");

        private final String descricao;

        TipoContrato(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusContrato {
        RASCUNHO("Rascunho"),
        EM_ANALISE("Em Análise"),
        APROVADO("Aprovado"),
        ASSINADO("Assinado"),
        ATIVO("Ativo"),
        SUSPENSO("Suspenso"),
        VENCIDO("Vencido"),
        RESCINDIDO("Rescindido"),
        FINALIZADO("Finalizado"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusContrato(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum PrioridadeContrato {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta"),
        CRITICA("Crítica");

        private final String descricao;

        PrioridadeContrato(String descricao) {
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
            status = StatusContrato.RASCUNHO;
        }
        if (tipo == null) {
            tipo = TipoContrato.PRESTACAO_SERVICOS;
        }
        if (prioridade == null) {
            prioridade = PrioridadeContrato.MEDIA;
        }
        if (versaoContrato == null) {
            versaoContrato = "1.0";
        }
        if (duracaoMeses == null) {
            duracaoMeses = 12;
        }
        if (prazoNotificacao == null) {
            prazoNotificacao = 30;
        }
        if (renovacaoAutomatica == null) {
            renovacaoAutomatica = false;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Business methods
    public boolean isRascunho() {
        return status == StatusContrato.RASCUNHO;
    }

    public boolean isEmAnalise() {
        return status == StatusContrato.EM_ANALISE;
    }

    public boolean isAprovado() {
        return status == StatusContrato.APROVADO;
    }

    public boolean isAssinado() {
        return status == StatusContrato.ASSINADO;
    }

    public boolean isAtivo() {
        return status == StatusContrato.ATIVO;
    }

    public boolean isRescindido() {
        return status == StatusContrato.RESCINDIDO;
    }

    public boolean isFinalizado() {
        return status == StatusContrato.FINALIZADO;
    }

    public boolean podeSerEditado() {
        return status == StatusContrato.RASCUNHO || status == StatusContrato.EM_ANALISE;
    }

    public boolean podeSerAprovado() {
        return status == StatusContrato.EM_ANALISE;
    }

    public boolean podeSerAssinado() {
        return status == StatusContrato.APROVADO;
    }

    public boolean podeSerAtivado() {
        return status == StatusContrato.ASSINADO;
    }

    public boolean podeSerRescindido() {
        return status == StatusContrato.ATIVO || status == StatusContrato.SUSPENSO;
    }

    public boolean podeSerSuspenso() {
        return status == StatusContrato.ATIVO;
    }

    public boolean podeSerReativado() {
        return status == StatusContrato.SUSPENSO;
    }

    public boolean isVencendoEm(int dias) {
        if (dataVencimento == null) {
            return false;
        }
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return dataVencimento.isBefore(dataLimite) || dataVencimento.isEqual(dataLimite);
    }

    public boolean isVencido() {
        return dataVencimento != null && dataVencimento.isBefore(LocalDate.now());
    }

    public long getDiasParaVencimento() {
        if (dataVencimento == null) {
            return Long.MAX_VALUE;
        }
        return LocalDate.now().until(dataVencimento).getDays();
    }

    public long getDiasDeAtraso() {
        if (dataVencimento == null || !isVencido()) {
            return 0;
        }
        return dataVencimento.until(LocalDate.now()).getDays();
    }

    public BigDecimal calcularValorTotal() {
        if (valorMensal != null && duracaoMeses != null) {
            return valorMensal.multiply(BigDecimal.valueOf(duracaoMeses));
        }
        return valorContrato != null ? valorContrato : BigDecimal.ZERO;
    }

    public LocalDate calcularProximaRenovacao() {
        if (dataInicio != null && duracaoMeses != null) {
            return dataInicio.plusMonths(duracaoMeses);
        }
        return null;
    }

    public boolean precisaNotificacaoRenovacao() {
        if (!renovacaoAutomatica || dataVencimento == null) {
            return false;
        }
        LocalDate dataNotificacao = dataVencimento.minusDays(prazoNotificacao);
        return LocalDate.now().isAfter(dataNotificacao) || LocalDate.now().isEqual(dataNotificacao);
    }

    public String getDescricaoCompleta() {
        return String.format("%s - %s (%s)", numeroContrato, titulo, tipo.getDescricao());
    }

    public String getNomeContraparte() {
        if (cliente != null) {
            return cliente.getNome();
        } else if (fornecedor != null) {
            return fornecedor.getNomeFantasia() != null ? fornecedor.getNomeFantasia() : fornecedor.getRazaoSocial();
        } else {
            return parteContratante != null ? parteContratante : "Não informado";
        }
    }

    public boolean temAditivos() {
        return aditivos != null && !aditivos.isEmpty();
    }

    public int getQuantidadeAditivos() {
        return aditivos != null ? aditivos.size() : 0;
    }

    public boolean temAlertas() {
        return alertas != null && !alertas.isEmpty();
    }

    public int getQuantidadeAlertasAtivos() {
        if (alertas == null) {
            return 0;
        }
        return (int) alertas.stream().filter(alerta -> !alerta.isResolvido()).count();
    }
}