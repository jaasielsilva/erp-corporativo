package com.jaasielsilva.portalceo.dto.servicos;

import com.jaasielsilva.portalceo.model.servicos.Prioridade;
import com.jaasielsilva.portalceo.model.servicos.StatusSolicitacao;
import java.time.LocalDateTime;

public class SolicitacaoServicoDTO {
    private Long id;
    private Long servicoId;
    private String servicoNome;
    private String titulo;
    private Prioridade prioridade;
    private StatusSolicitacao status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public SolicitacaoServicoDTO() {}

    public SolicitacaoServicoDTO(Long id, Long servicoId, String servicoNome, String titulo,
                                 Prioridade prioridade, StatusSolicitacao status,
                                 LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.servicoId = servicoId;
        this.servicoNome = servicoNome;
        this.titulo = titulo;
        this.prioridade = prioridade;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}