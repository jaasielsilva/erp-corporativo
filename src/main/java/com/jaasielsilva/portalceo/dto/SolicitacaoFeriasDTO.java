package com.jaasielsilva.portalceo.dto;

import java.time.LocalDate;

public class SolicitacaoFeriasDTO {
    private Long id;
    private String colaboradorNome;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private String status;
    private String observacoes;

    public SolicitacaoFeriasDTO() {}

    public SolicitacaoFeriasDTO(Long id, String colaboradorNome, LocalDate periodoInicio, LocalDate periodoFim, String status, String observacoes) {
        this.id = id;
        this.colaboradorNome = colaboradorNome;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.status = status;
        this.observacoes = observacoes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getColaboradorNome() { return colaboradorNome; }
    public void setColaboradorNome(String colaboradorNome) { this.colaboradorNome = colaboradorNome; }

    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate periodoInicio) { this.periodoInicio = periodoInicio; }

    public LocalDate getPeriodoFim() { return periodoFim; }
    public void setPeriodoFim(LocalDate periodoFim) { this.periodoFim = periodoFim; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}

