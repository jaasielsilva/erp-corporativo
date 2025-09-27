package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contrato_alerta")
public class ContratoAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoLegal contrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeAlerta prioridade = PrioridadeAlerta.MEDIA;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataAlerta;

    @Column
    private LocalDate dataVencimento;

    @Column
    private LocalDate dataResolucao;

    @Column(nullable = false)
    private Boolean resolvido = false;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 500)
    private String acaoRecomendada;

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    @ManyToOne
    @JoinColumn(name = "usuario_resolucao_id")
    private Usuario usuarioResolucao;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaEdicao;

    public enum TipoAlerta {
        VENCIMENTO_CONTRATO("Vencimento de Contrato"),
        RENOVACAO_CONTRATO("Renovação de Contrato"),
        PAGAMENTO_PENDENTE("Pagamento Pendente"),
        REVISAO_CLAUSULAS("Revisão de Cláusulas"),
        COMPLIANCE("Conformidade Legal"),
        DOCUMENTACAO_PENDENTE("Documentação Pendente"),
        RESCISAO_PROGRAMADA("Rescisão Programada"),
        ADITIVO_NECESSARIO("Aditivo Necessário"),
        MULTA_APLICADA("Multa Aplicada"),
        GARANTIA_VENCENDO("Garantia Vencendo"),
        LICENCA_VENCENDO("Licença Vencendo"),
        AUDITORIA_PROGRAMADA("Auditoria Programada"),
        OUTROS("Outros");

        private final String descricao;

        TipoAlerta(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum PrioridadeAlerta {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta"),
        CRITICA("Crítica");

        private final String descricao;

        PrioridadeAlerta(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    public void onPrePersist() {
        dataCriacao = LocalDateTime.now();
        if (dataAlerta == null) {
            dataAlerta = LocalDate.now();
        }
        if (prioridade == null) {
            prioridade = PrioridadeAlerta.MEDIA;
        }
        if (resolvido == null) {
            resolvido = false;
        }
        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
        if (resolvido && dataResolucao == null) {
            dataResolucao = LocalDate.now();
        }
    }

    // Business methods
    public boolean isResolvido() {
        return resolvido != null && resolvido;
    }

    public boolean isAtivo() {
        return ativo != null && ativo && !isResolvido();
    }

    public boolean isVencido() {
        return dataVencimento != null && dataVencimento.isBefore(LocalDate.now()) && !isResolvido();
    }

    public boolean isVencendoEm(int dias) {
        if (dataVencimento == null || isResolvido()) {
            return false;
        }
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return dataVencimento.isBefore(dataLimite) || dataVencimento.isEqual(dataLimite);
    }

    public long getDiasParaVencimento() {
        if (dataVencimento == null || isResolvido()) {
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

    public void marcarComoResolvido(String observacoes, Usuario usuario) {
        this.resolvido = true;
        this.dataResolucao = LocalDate.now();
        this.usuarioResolucao = usuario;
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            this.observacoes = observacoes;
        }
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
        this.resolvido = false;
        this.dataResolucao = null;
        this.usuarioResolucao = null;
    }

    public String getDescricaoCompleta() {
        return String.format("%s - %s", tipo.getDescricao(), titulo);
    }

    public String getStatusDescricao() {
        if (isResolvido()) {
            return "Resolvido";
        } else if (isVencido()) {
            return "Vencido";
        } else if (isVencendoEm(7)) {
            return "Vencendo";
        } else if (isAtivo()) {
            return "Ativo";
        } else {
            return "Inativo";
        }
    }

    public String getCssClass() {
        if (isResolvido()) {
            return "success";
        } else if (isVencido() || prioridade == PrioridadeAlerta.CRITICA) {
            return "danger";
        } else if (isVencendoEm(7) || prioridade == PrioridadeAlerta.ALTA) {
            return "warning";
        } else {
            return "info";
        }
    }

    public boolean necessitaAcaoImediata() {
        return isVencido() || (prioridade == PrioridadeAlerta.CRITICA && isAtivo());
    }
}