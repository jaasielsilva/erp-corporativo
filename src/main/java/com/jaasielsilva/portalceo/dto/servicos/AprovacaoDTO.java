package com.jaasielsilva.portalceo.dto.servicos;

import com.jaasielsilva.portalceo.model.servicos.AprovacaoSolicitacao.StatusAprovacao;

public class AprovacaoDTO {
    private Long id;
    private Long solicitacaoId;
    private String solicitacaoTitulo;
    private String servicoNome;
    private String gestorNome;
    private String justificativa;
    private StatusAprovacao status;

    public AprovacaoDTO() {}

    public AprovacaoDTO(Long id, Long solicitacaoId, String solicitacaoTitulo, String servicoNome,
                        String gestorNome, String justificativa, StatusAprovacao status) {
        this.id = id;
        this.solicitacaoId = solicitacaoId;
        this.solicitacaoTitulo = solicitacaoTitulo;
        this.servicoNome = servicoNome;
        this.gestorNome = gestorNome;
        this.justificativa = justificativa;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSolicitacaoId() { return solicitacaoId; }
    public void setSolicitacaoId(Long solicitacaoId) { this.solicitacaoId = solicitacaoId; }
    public String getSolicitacaoTitulo() { return solicitacaoTitulo; }
    public void setSolicitacaoTitulo(String solicitacaoTitulo) { this.solicitacaoTitulo = solicitacaoTitulo; }
    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
    public String getGestorNome() { return gestorNome; }
    public void setGestorNome(String gestorNome) { this.gestorNome = gestorNome; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public StatusAprovacao getStatus() { return status; }
    public void setStatus(StatusAprovacao status) { this.status = status; }
}