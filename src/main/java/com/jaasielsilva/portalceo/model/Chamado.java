package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", unique = true, nullable = false)
    private String numero;

    @NotBlank(message = "Assunto é obrigatório")
    @Size(min = 5, max = 200, message = "Assunto deve ter entre 5 e 200 caracteres")
    @Column(name = "assunto", nullable = false)
    private String assunto;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, message = "Descrição deve ter pelo menos 10 caracteres")
    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Prioridade é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false)
    private Prioridade prioridade;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusChamado status = StatusChamado.ABERTO;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_inicio_atendimento")
    private LocalDateTime dataInicioAtendimento;

    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "sla_vencimento")
    private LocalDateTime slaVencimento;

    @Column(name = "tecnico_responsavel")
    private String tecnicoResponsavel;

    @Column(name = "solicitante_nome")
    private String solicitanteNome;

    @Column(name = "solicitante_email")
    private String solicitanteEmail;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "subcategoria")
    private String subcategoria;

    @Column(name = "tempo_resolucao_minutos")
    private Integer tempoResolucaoMinutos;

    @Column(name = "avaliacao")
    private Integer avaliacao; // 1-5 estrelas

    @Column(name = "comentario_avaliacao", columnDefinition = "TEXT")
    private String comentarioAvaliacao;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // Campo transiente para SLA restante (calculado dinamicamente)
    @Transient
    private Long slaRestante;

    // Construtores
    public Chamado() {
        this.dataAbertura = LocalDateTime.now();
        this.status = StatusChamado.ABERTO;
    }

    public Chamado(String assunto, String descricao, Prioridade prioridade) {
        this();
        this.assunto = assunto;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.numero = gerarNumero();
    }

    // Métodos de ciclo de vida
    @PrePersist
    protected void onCreate() {
        if (this.dataAbertura == null) {
            this.dataAbertura = LocalDateTime.now();
        }
        if (this.numero == null) {
            this.numero = gerarNumero();
        }
    }

    // Método para gerar número do chamado
    private String gerarNumero() {
        return "CH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // Métodos de negócio
    public boolean isAberto() {
        return this.status == StatusChamado.ABERTO;
    }

    public boolean isEmAndamento() {
        return this.status == StatusChamado.EM_ANDAMENTO;
    }

    public boolean isResolvido() {
        return this.status == StatusChamado.RESOLVIDO;
    }

    public boolean isFechado() {
        return this.status == StatusChamado.FECHADO;
    }

    public void resolver() {
        this.status = StatusChamado.RESOLVIDO;
        this.dataResolucao = LocalDateTime.now();
        if (this.dataInicioAtendimento != null) {
            this.tempoResolucaoMinutos = (int) java.time.Duration.between(this.dataInicioAtendimento, this.dataResolucao).toMinutes();
        }
    }

    public void fechar() {
        this.status = StatusChamado.FECHADO;
        if (this.dataResolucao == null) {
            this.dataResolucao = LocalDateTime.now();
        }
        this.dataFechamento = LocalDateTime.now();
    }

    public void iniciarAtendimento(String tecnico) {
        this.status = StatusChamado.EM_ANDAMENTO;
        this.tecnicoResponsavel = tecnico;
        this.dataInicioAtendimento = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public String getTecnicoResponsavel() {
        return tecnicoResponsavel;
    }

    public void setTecnicoResponsavel(String tecnicoResponsavel) {
        this.tecnicoResponsavel = tecnicoResponsavel;
    }

    public String getSolicitanteNome() {
        return solicitanteNome;
    }

    public void setSolicitanteNome(String solicitanteNome) {
        this.solicitanteNome = solicitanteNome;
    }

    public String getSolicitanteEmail() {
        return solicitanteEmail;
    }

    public void setSolicitanteEmail(String solicitanteEmail) {
        this.solicitanteEmail = solicitanteEmail;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Long getSlaRestante() {
        return slaRestante;
    }

    public void setSlaRestante(Long slaRestante) {
        this.slaRestante = slaRestante;
    }

    public LocalDateTime getDataInicioAtendimento() {
        return dataInicioAtendimento;
    }

    public void setDataInicioAtendimento(LocalDateTime dataInicioAtendimento) {
        this.dataInicioAtendimento = dataInicioAtendimento;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public LocalDateTime getSlaVencimento() {
        return slaVencimento;
    }

    public void setSlaVencimento(LocalDateTime slaVencimento) {
        this.slaVencimento = slaVencimento;
    }

    public String getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }

    public Integer getTempoResolucaoMinutos() {
        return tempoResolucaoMinutos;
    }

    public void setTempoResolucaoMinutos(Integer tempoResolucaoMinutos) {
        this.tempoResolucaoMinutos = tempoResolucaoMinutos;
    }

    public Integer getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Integer avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getComentarioAvaliacao() {
        return comentarioAvaliacao;
    }

    public void setComentarioAvaliacao(String comentarioAvaliacao) {
        this.comentarioAvaliacao = comentarioAvaliacao;
    }

    // Enums
    public enum Prioridade {
        BAIXA("Baixa", 72),
        MEDIA("Média", 48),
        ALTA("Alta", 24),
        URGENTE("Urgente", 8);

        private final String descricao;
        private final int horasUteis;

        Prioridade(String descricao, int horasUteis) {
            this.descricao = descricao;
            this.horasUteis = horasUteis;
        }

        public String getDescricao() {
            return descricao;
        }

        public int getHorasUteis() {
            return horasUteis;
        }
    }

    public enum StatusChamado {
        ABERTO("Aberto"),
        EM_ANDAMENTO("Em Andamento"),
        RESOLVIDO("Resolvido"),
        FECHADO("Fechado");

        private final String descricao;

        StatusChamado(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @Override
    public String toString() {
        return "Chamado{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", assunto='" + assunto + '\'' +
                ", prioridade=" + prioridade +
                ", status=" + status +
                ", dataAbertura=" + dataAbertura +
                '}';
    }
}