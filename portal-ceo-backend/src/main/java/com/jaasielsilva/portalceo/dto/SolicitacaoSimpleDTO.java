package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SolicitacaoSimpleDTO {
    private Long id;
    private String protocolo;
    private String solicitanteNome;
    private String colaboradorNome;
    private String status;
    private String prioridade;
    private String nivelSolicitado;
    private LocalDateTime dataSolicitacao;
    private LocalDate dataLimite;
    private String justificativa;
    
    public SolicitacaoSimpleDTO() {}
    
    public SolicitacaoSimpleDTO(SolicitacaoAcesso solicitacao) {
        this.id = solicitacao.getId();
        this.protocolo = solicitacao.getProtocolo();
        this.solicitanteNome = solicitacao.getSolicitanteNome();
        this.colaboradorNome = solicitacao.getColaborador() != null ? solicitacao.getColaborador().getNome() : "N/A";
        this.status = solicitacao.getStatus() != null ? solicitacao.getStatus().toString() : "N/A";
        this.prioridade = solicitacao.getPrioridade() != null ? solicitacao.getPrioridade().toString() : "N/A";
        this.nivelSolicitado = solicitacao.getNivelSolicitado() != null ? solicitacao.getNivelSolicitado().getDescricao() : "N/A";
        this.dataSolicitacao = solicitacao.getDataSolicitacao();
        this.dataLimite = solicitacao.getDataLimite();
        this.justificativa = solicitacao.getJustificativa();
    }
    
    // Método para criar DTO a partir de Object[] (consulta nativa)
    public static SolicitacaoSimpleDTO fromObjectArray(Object[] row) {
        SolicitacaoSimpleDTO dto = new SolicitacaoSimpleDTO();
        dto.setId(((Number) row[0]).longValue());
        dto.setProtocolo((String) row[1]);
        dto.setSolicitanteNome((String) row[2]);
        dto.setStatus((String) row[3]);
        dto.setPrioridade((String) row[4]);
        dto.setDataSolicitacao((LocalDateTime) row[5]);
        dto.setDataLimite((LocalDate) row[6]);
        dto.setJustificativa((String) row[7]);
        dto.setColaboradorNome((String) row[8]);
        return dto;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    
    public String getSolicitanteNome() { return solicitanteNome; }
    public void setSolicitanteNome(String solicitanteNome) { this.solicitanteNome = solicitanteNome; }
    
    public String getColaboradorNome() { return colaboradorNome; }
    public void setColaboradorNome(String colaboradorNome) { this.colaboradorNome = colaboradorNome; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    
    public LocalDate getDataLimite() { return dataLimite; }
    public void setDataLimite(LocalDate dataLimite) { this.dataLimite = dataLimite; }
    
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    
    public String getNivelSolicitado() { return nivelSolicitado; }
    public void setNivelSolicitado(String nivelSolicitado) { this.nivelSolicitado = nivelSolicitado; }
}