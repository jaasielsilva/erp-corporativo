package com.jaasielsilva.portalceo.model.servicos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aprovacao_solicitacao")
public class AprovacaoSolicitacao {

    public enum StatusAprovacao { EM_APROVACAO, APROVADA, REJEITADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private SolicitacaoServico solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAprovacao status;

    @Column(length = 2000)
    private String justificativa;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(length = 120)
    private String gestorNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SolicitacaoServico getSolicitacao() { return solicitacao; }
    public void setSolicitacao(SolicitacaoServico solicitacao) { this.solicitacao = solicitacao; }
    public StatusAprovacao getStatus() { return status; }
    public void setStatus(StatusAprovacao status) { this.status = status; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public String getGestorNome() { return gestorNome; }
    public void setGestorNome(String gestorNome) { this.gestorNome = gestorNome; }
}