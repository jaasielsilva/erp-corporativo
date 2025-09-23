package com.jaasielsilva.portalceo.model.ti;

import com.jaasielsilva.portalceo.model.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_suporte")
public class TicketSuporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O número do ticket é obrigatório")
    @Size(min = 5, max = 20, message = "O número deve ter entre 5 e 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String numeroTicket;

    @NotNull(message = "O título é obrigatório")
    @Size(min = 5, max = 200, message = "O título deve ter entre 5 e 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotNull(message = "A descrição é obrigatória")
    @Size(min = 10, max = 2000, message = "A descrição deve ter entre 10 e 2000 caracteres")
    @Column(nullable = false, length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTicket tipo = TipoTicket.INCIDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeTicket prioridade = PrioridadeTicket.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTicket status = StatusTicket.ABERTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaTicket categoria = CategoriaTicket.HARDWARE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_solicitante_id", nullable = false)
    private Usuario usuarioSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @Column
    private LocalDateTime dataAtribuicao;

    @Column
    private LocalDateTime dataFechamento;

    @Column
    private LocalDateTime dataLimite;

    @Column(length = 500)
    private String solucao;

    @Column(length = 1000)
    private String observacoes;

    @Column
    private Integer tempoGastoMinutos = 0;

    @Column
    private Integer slaMinutos = 240; // 4 horas default

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketInteracao> interacoes;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketAnexo> anexos;

    private LocalDateTime dataUltimaEdicao;

    public enum TipoTicket {
        INCIDENTE("Incidente"),
        SOLICITACAO("Solicitação"),
        PROBLEMA("Problema"),
        MUDANCA("Mudança"),
        CONSULTA("Consulta"),
        MANUTENCAO("Manutenção");

        private final String descricao;

        TipoTicket(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum PrioridadeTicket {
        BAIXA("Baixa", 480), // 8 horas
        MEDIA("Média", 240), // 4 horas
        ALTA("Alta", 120),   // 2 horas
        CRITICA("Crítica", 60); // 1 hora

        private final String descricao;
        private final int slaMinutos;

        PrioridadeTicket(String descricao, int slaMinutos) {
            this.descricao = descricao;
            this.slaMinutos = slaMinutos;
        }

        public String getDescricao() {
            return descricao;
        }

        public int getSlaMinutos() {
            return slaMinutos;
        }
    }

    public enum StatusTicket {
        ABERTO("Aberto"),
        ATRIBUIDO("Atribuído"),
        EM_ANDAMENTO("Em Andamento"),
        AGUARDANDO_USUARIO("Aguardando Usuário"),
        AGUARDANDO_TERCEIROS("Aguardando Terceiros"),
        RESOLVIDO("Resolvido"),
        FECHADO("Fechado"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusTicket(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum CategoriaTicket {
        HARDWARE("Hardware"),
        SOFTWARE("Software"),
        REDE("Rede"),
        EMAIL("E-mail"),
        TELEFONIA("Telefonia"),
        IMPRESSORA("Impressora"),
        ACESSO("Acesso/Permissões"),
        BACKUP("Backup"),
        SEGURANCA("Segurança"),
        INSTALACAO("Instalação"),
        TREINAMENTO("Treinamento"),
        OUTROS("Outros");

        private final String descricao;

        CategoriaTicket(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        if (numeroTicket == null || numeroTicket.isEmpty()) {
            numeroTicket = gerarNumeroTicket();
        }
        if (slaMinutos == null && prioridade != null) {
            slaMinutos = prioridade.getSlaMinutos();
        }
        if (dataLimite == null && slaMinutos != null) {
            dataLimite = dataAbertura.plusMinutes(slaMinutos);
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        
        if (status == StatusTicket.ATRIBUIDO && dataAtribuicao == null) {
            dataAtribuicao = LocalDateTime.now();
        }
        
        if ((status == StatusTicket.FECHADO || status == StatusTicket.RESOLVIDO) && dataFechamento == null) {
            dataFechamento = LocalDateTime.now();
        }
    }

    // Business methods
    public boolean isAberto() {
        return status == StatusTicket.ABERTO || status == StatusTicket.ATRIBUIDO || status == StatusTicket.EM_ANDAMENTO;
    }

    public boolean isFechado() {
        return status == StatusTicket.FECHADO || status == StatusTicket.RESOLVIDO;
    }

    public boolean isVencido() {
        return dataLimite != null && LocalDateTime.now().isAfter(dataLimite) && isAberto();
    }

    public boolean isVencendoEm(int minutos) {
        if (dataLimite == null || isFechado()) {
            return false;
        }
        LocalDateTime limite = LocalDateTime.now().plusMinutes(minutos);
        return dataLimite.isBefore(limite) || dataLimite.isEqual(limite);
    }

    public long getMinutosParaVencimento() {
        if (dataLimite == null || isFechado()) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(LocalDateTime.now(), dataLimite).toMinutes();
    }

    public long getMinutosDeAtraso() {
        if (dataLimite == null || !isVencido()) {
            return 0;
        }
        return java.time.Duration.between(dataLimite, LocalDateTime.now()).toMinutes();
    }

    public long getTempoResolucaoMinutos() {
        if (dataFechamento == null) {
            return java.time.Duration.between(dataAbertura, LocalDateTime.now()).toMinutes();
        }
        return java.time.Duration.between(dataAbertura, dataFechamento).toMinutes();
    }

    public String getCssClassPrioridade() {
        return switch (prioridade) {
            case CRITICA -> "danger";
            case ALTA -> "warning";
            case MEDIA -> "info";
            case BAIXA -> "secondary";
        };
    }

    public String getCssClassStatus() {
        return switch (status) {
            case ABERTO -> "primary";
            case ATRIBUIDO, EM_ANDAMENTO -> "warning";
            case AGUARDANDO_USUARIO, AGUARDANDO_TERCEIROS -> "info";
            case RESOLVIDO, FECHADO -> "success";
            case CANCELADO -> "secondary";
        };
    }

    public boolean podeSerAtribuido() {
        return status == StatusTicket.ABERTO;
    }

    public boolean podeSerIniciadoTrabalho() {
        return status == StatusTicket.ATRIBUIDO;
    }

    public boolean podeSerResolvido() {
        return status == StatusTicket.EM_ANDAMENTO || status == StatusTicket.AGUARDANDO_USUARIO || status == StatusTicket.AGUARDANDO_TERCEIROS;
    }

    public boolean podeSerFechado() {
        return status == StatusTicket.RESOLVIDO;
    }

    public boolean podeSerCancelado() {
        return isAberto();
    }

    private String gerarNumeroTicket() {
        return String.format("TK%d%04d", 
                LocalDateTime.now().getYear(), 
                (int) (Math.random() * 10000));
    }

    public String getDescricaoCompleta() {
        return String.format("%s - %s", numeroTicket, titulo);
    }

    public boolean necessitaAcaoImediata() {
        return (prioridade == PrioridadeTicket.CRITICA && isAberto()) || isVencido();
    }

    public int getQuantidadeInteracoes() {
        return interacoes != null ? interacoes.size() : 0;
    }

    public int getQuantidadeAnexos() {
        return anexos != null ? anexos.size() : 0;
    }
}