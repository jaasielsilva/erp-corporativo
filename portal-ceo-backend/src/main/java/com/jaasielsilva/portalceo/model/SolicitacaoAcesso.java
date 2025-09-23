package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "solicitacoes_acesso")
public class SolicitacaoAcesso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "protocolo", unique = true, nullable = false)
    private String protocolo;
    
    // Dados do Solicitante
    @NotBlank(message = "Nome do solicitante é obrigatório")
    @Column(name = "solicitante_nome", nullable = false)
    private String solicitanteNome;
    
    @NotBlank(message = "Cargo do solicitante é obrigatório")
    @Column(name = "solicitante_cargo", nullable = false)
    private String solicitanteCargo;
    
    @NotBlank(message = "Departamento do solicitante é obrigatório")
    @Column(name = "solicitante_departamento", nullable = false)
    private String solicitanteDepartamento;
    
    @Email(message = "Email inválido")
    @NotBlank(message = "Email do solicitante é obrigatório")
    @Column(name = "solicitante_email", nullable = false)
    private String solicitanteEmail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_usuario_id")
    @JsonIgnoreProperties({"solicitacoes", "colaborador"})
    private Usuario solicitanteUsuario;
    
    // Dados do Futuro Usuário
    @NotNull(message = "Colaborador é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    @JsonIgnoreProperties({"supervisor", "cargo", "departamento"})
    private Colaborador colaborador;
    
    @NotNull(message = "Tipo de usuário é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;
    
    // Módulos e Permissões
    @ElementCollection(targetClass = ModuloSistema.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "solicitacao_modulos", joinColumns = @JoinColumn(name = "solicitacao_id"))
    @Column(name = "modulo")
    private Set<ModuloSistema> modulos;
    
    @NotNull(message = "Nível de acesso é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_solicitado", nullable = false)
    private NivelAcesso nivelSolicitado;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "prazo_acesso")
    private PrazoAcesso prazoAcesso = PrazoAcesso.PERMANENTE;
    
    @Column(name = "data_inicio")
    private LocalDate dataInicio;
    
    @Column(name = "data_fim")
    private LocalDate dataFim;
    
    // Justificativa e Detalhes
    @NotBlank(message = "Justificativa é obrigatória")
    @Size(min = 50, message = "Justificativa deve ter pelo menos 50 caracteres")
    @Column(name = "justificativa", nullable = false, columnDefinition = "TEXT")
    private String justificativa;
    
    @Column(name = "sistemas_especificos", columnDefinition = "TEXT")
    private String sistemasEspecificos;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade")
    private Prioridade prioridade = Prioridade.MEDIA;
    
    @Column(name = "data_limite")
    private LocalDate dataLimite;
    
    // Controle de Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;
    
    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;
    
    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;
    
    // Dados do Aprovador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovador_usuario_id")
    @JsonIgnoreProperties({"solicitacoes", "colaborador"})
    private Usuario aprovadorUsuario;
    
    @Column(name = "aprovador_nome")
    private String aprovadorNome;
    
    @Column(name = "observacoes_aprovador", columnDefinition = "TEXT")
    private String observacoesAprovador;
    
    // Configurações Aprovadas
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_aprovado")
    private NivelAcesso nivelAprovado;
    
    @ElementCollection(targetClass = ModuloSistema.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "solicitacao_modulos_aprovados", joinColumns = @JoinColumn(name = "solicitacao_id"))
    @Column(name = "modulo")
    private Set<ModuloSistema> modulosAprovados;
    
    @Column(name = "email_corporativo")
    private String emailCorporativo;
    
    // Usuário criado após aprovação
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criado_id")
    @JsonIgnoreProperties({"solicitacoes", "colaborador"})
    private Usuario usuarioCriado;
    
    // Construtores
    public SolicitacaoAcesso() {
        this.dataSolicitacao = LocalDateTime.now();
    }
    
    @PrePersist
    private void prePersist() {
        if (this.protocolo == null || this.protocolo.isEmpty()) {
            this.protocolo = gerarProtocolo();
        }
        if (this.dataSolicitacao == null) {
            this.dataSolicitacao = LocalDateTime.now();
        }
    }
    
    // Método para gerar protocolo único
    private String gerarProtocolo() {
        return "SOL" + System.currentTimeMillis();
    }
    
    // Enums
    public enum TipoUsuario {
        FUNCIONARIO("Funcionário CLT"),
        TERCEIRIZADO("Terceirizado"),
        ESTAGIARIO("Estagiário"),
        CONSULTOR("Consultor"),
        REPRESENTANTE("Representante");
        
        private final String descricao;
        
        TipoUsuario(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public enum ModuloSistema {
        RH("Recursos Humanos"),
        FINANCEIRO("Financeiro"),
        VENDAS("Vendas"),
        COMPRAS("Compras"),
        ESTOQUE("Estoque"),
        MARKETING("Marketing"),
        TI("TI"),
        JURIDICO("Jurídico");
        
        private final String descricao;
        
        ModuloSistema(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public enum NivelAcesso {
        CONSULTA("Consulta"),
        OPERACIONAL("Operacional"),
        SUPERVISAO("Supervisão"),
        COORDENACAO("Coordenação"),
        GERENCIAL("Gerencial"),
        ADMINISTRATIVO("Administrativo");
        
        private final String descricao;
        
        NivelAcesso(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public enum PrazoAcesso {
        PERMANENTE("Permanente"),
        TEMPORARIO("Temporário");
        
        private final String descricao;
        
        PrazoAcesso(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public enum Prioridade {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta"),
        URGENTE("Urgente");
        
        private final String descricao;
        
        Prioridade(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    public enum StatusSolicitacao {
        PENDENTE("Pendente"),
        EM_ANALISE("Em Análise"),
        APROVADO("Aprovado"),
        REJEITADO("Rejeitado"),
        APROVADO_PARCIAL("Aprovado Parcialmente"),
        USUARIO_CRIADO("Usuário Criado"),
        CANCELADO("Cancelado");
        
        private final String descricao;
        
        StatusSolicitacao(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    // Métodos de conveniência
    public boolean isPendente() {
        return this.status == StatusSolicitacao.PENDENTE;
    }
    
    public boolean isAprovado() {
        return this.status == StatusSolicitacao.APROVADO || this.status == StatusSolicitacao.APROVADO_PARCIAL;
    }
    
    public boolean isRejeitado() {
        return this.status == StatusSolicitacao.REJEITADO;
    }
    
    public boolean isTemporario() {
        return this.prazoAcesso == PrazoAcesso.TEMPORARIO;
    }
    
    public boolean isUrgente() {
        return this.prioridade == Prioridade.URGENTE;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProtocolo() {
        return protocolo;
    }
    
    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }
    
    public String getSolicitanteNome() {
        return solicitanteNome;
    }
    
    public void setSolicitanteNome(String solicitanteNome) {
        this.solicitanteNome = solicitanteNome;
    }
    
    public String getSolicitanteCargo() {
        return solicitanteCargo;
    }
    
    public void setSolicitanteCargo(String solicitanteCargo) {
        this.solicitanteCargo = solicitanteCargo;
    }
    
    public String getSolicitanteDepartamento() {
        return solicitanteDepartamento;
    }
    
    public void setSolicitanteDepartamento(String solicitanteDepartamento) {
        this.solicitanteDepartamento = solicitanteDepartamento;
    }
    
    public String getSolicitanteEmail() {
        return solicitanteEmail;
    }
    
    public void setSolicitanteEmail(String solicitanteEmail) {
        this.solicitanteEmail = solicitanteEmail;
    }
    
    public Usuario getSolicitanteUsuario() {
        return solicitanteUsuario;
    }
    
    public void setSolicitanteUsuario(Usuario solicitanteUsuario) {
        this.solicitanteUsuario = solicitanteUsuario;
    }
    
    public Colaborador getColaborador() {
        return colaborador;
    }
    
    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }
    
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    
    public Set<ModuloSistema> getModulos() {
        return modulos;
    }
    
    public void setModulos(Set<ModuloSistema> modulos) {
        this.modulos = modulos;
    }
    
    public NivelAcesso getNivelSolicitado() {
        return nivelSolicitado;
    }
    
    public void setNivelSolicitado(NivelAcesso nivelSolicitado) {
        this.nivelSolicitado = nivelSolicitado;
    }
    
    public PrazoAcesso getPrazoAcesso() {
        return prazoAcesso;
    }
    
    public void setPrazoAcesso(PrazoAcesso prazoAcesso) {
        this.prazoAcesso = prazoAcesso;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
    
    public String getJustificativa() {
        return justificativa;
    }
    
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
    
    public String getSistemasEspecificos() {
        return sistemasEspecificos;
    }
    
    public void setSistemasEspecificos(String sistemasEspecificos) {
        this.sistemasEspecificos = sistemasEspecificos;
    }
    
    public Prioridade getPrioridade() {
        return prioridade;
    }
    
    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }
    
    public LocalDate getDataLimite() {
        return dataLimite;
    }
    
    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }
    
    public StatusSolicitacao getStatus() {
        return status;
    }
    
    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }
    
    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }
    
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }
    
    public LocalDateTime getDataAprovacao() {
        return dataAprovacao;
    }
    
    public void setDataAprovacao(LocalDateTime dataAprovacao) {
        this.dataAprovacao = dataAprovacao;
    }
    
    public Usuario getAprovadorUsuario() {
        return aprovadorUsuario;
    }
    
    public void setAprovadorUsuario(Usuario aprovadorUsuario) {
        this.aprovadorUsuario = aprovadorUsuario;
    }
    
    public String getAprovadorNome() {
        return aprovadorNome;
    }
    
    public void setAprovadorNome(String aprovadorNome) {
        this.aprovadorNome = aprovadorNome;
    }
    
    public String getObservacoesAprovador() {
        return observacoesAprovador;
    }
    
    public void setObservacoesAprovador(String observacoesAprovador) {
        this.observacoesAprovador = observacoesAprovador;
    }
    
    public NivelAcesso getNivelAprovado() {
        return nivelAprovado;
    }
    
    public void setNivelAprovado(NivelAcesso nivelAprovado) {
        this.nivelAprovado = nivelAprovado;
    }
    
    public Set<ModuloSistema> getModulosAprovados() {
        return modulosAprovados;
    }
    
    public void setModulosAprovados(Set<ModuloSistema> modulosAprovados) {
        this.modulosAprovados = modulosAprovados;
    }
    
    public String getEmailCorporativo() {
        return emailCorporativo;
    }
    
    public void setEmailCorporativo(String emailCorporativo) {
        this.emailCorporativo = emailCorporativo;
    }
    
    public Usuario getUsuarioCriado() {
        return usuarioCriado;
    }
    
    public void setUsuarioCriado(Usuario usuarioCriado) {
        this.usuarioCriado = usuarioCriado;
    }
}