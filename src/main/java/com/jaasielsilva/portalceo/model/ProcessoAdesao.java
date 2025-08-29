package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "processo_adesao")
public class ProcessoAdesao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "nome_colaborador", nullable = false)
    private String nomeColaborador;

    @Column(name = "email_colaborador", nullable = false)
    private String emailColaborador;

    @Column(name = "cpf_colaborador", nullable = false)
    private String cpfColaborador;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "data_admissao")
    private LocalDateTime dataAdmissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProcesso status;

    @Column(name = "etapa_atual")
    private String etapaAtual;

    @Column(name = "dados_pessoais", columnDefinition = "TEXT")
    private String dadosPessoaisJson;

    @Column(name = "documentos", columnDefinition = "TEXT")
    private String documentosJson;

    @Column(name = "beneficios", columnDefinition = "TEXT")
    private String beneficiosJson;

    @Column(name = "custo_total_mensal")
    private Double custoTotalMensal;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "aprovado_por")
    private String aprovadoPor;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "salario")
    private BigDecimal salario;

    @Column(name = "motivo_rejeicao", columnDefinition = "TEXT")
    private String motivoRejeicao;

    @OneToMany(mappedBy = "processoAdesao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistoricoProcessoAdesao> historico = new ArrayList<>();

    // Construtores
    public ProcessoAdesao() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusProcesso.INICIADO;
        this.etapaAtual = "dados-pessoais";
    }

    public ProcessoAdesao(String sessionId, String nomeColaborador, String emailColaborador, String cpfColaborador) {
        this();
        this.sessionId = sessionId;
        this.nomeColaborador = nomeColaborador;
        this.emailColaborador = emailColaborador;
        this.cpfColaborador = cpfColaborador;
    }

    // Métodos de negócio
    public void atualizarEtapa(String novaEtapa) {
        this.etapaAtual = novaEtapa;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void finalizar(String observacoes) {
        this.status = StatusProcesso.AGUARDANDO_APROVACAO;
        this.etapaAtual = "finalizado";
        this.dataFinalizacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.observacoes = observacoes;
    }

    public void aprovar(String aprovadoPor, String comentario) {
        this.status = StatusProcesso.APROVADO;
        this.aprovadoPor = aprovadoPor;
        this.observacoes = comentario;
        this.dataAprovacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void rejeitar(String motivoRejeicao) {
        this.status = StatusProcesso.REJEITADO;
        this.motivoRejeicao = motivoRejeicao;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void cancelar(String motivo) {
        this.status = StatusProcesso.CANCELADO;
        this.observacoes = motivo;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public boolean isFinalizavel() {
        return this.status == StatusProcesso.EM_ANDAMENTO &&
                "revisao".equals(this.etapaAtual);
    }

    public boolean isAprovavel() {
        return this.status == StatusProcesso.AGUARDANDO_APROVACAO;
    }

    public boolean isEditavel() {
        return this.status == StatusProcesso.INICIADO ||
                this.status == StatusProcesso.EM_ANDAMENTO;
    }

    // Enum para Status do Processo
    public enum StatusProcesso {
        INICIADO("Iniciado"),
        EM_ANDAMENTO("Em Andamento"),
        AGUARDANDO_APROVACAO("Aguardando Aprovação"),
        APROVADO("Aprovado"),
        REJEITADO("Rejeitado"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusProcesso(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}