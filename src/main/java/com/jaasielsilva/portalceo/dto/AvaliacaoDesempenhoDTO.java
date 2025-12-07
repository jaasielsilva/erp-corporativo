package com.jaasielsilva.portalceo.dto;

import java.time.LocalDate;

public class AvaliacaoDesempenhoDTO {
    private Long id;
    private String colaboradorNome;
    private String avaliadorEmail;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private String status;
    private Double nota;
    private String feedback;

    public AvaliacaoDesempenhoDTO() {}

    public AvaliacaoDesempenhoDTO(Long id, String colaboradorNome, String avaliadorEmail,
                                  LocalDate periodoInicio, LocalDate periodoFim, String status,
                                  Double nota, String feedback) {
        this.id = id;
        this.colaboradorNome = colaboradorNome;
        this.avaliadorEmail = avaliadorEmail;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.status = status;
        this.nota = nota;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getColaboradorNome() { return colaboradorNome; }
    public void setColaboradorNome(String colaboradorNome) { this.colaboradorNome = colaboradorNome; }
    public String getAvaliadorEmail() { return avaliadorEmail; }
    public void setAvaliadorEmail(String avaliadorEmail) { this.avaliadorEmail = avaliadorEmail; }
    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate periodoInicio) { this.periodoInicio = periodoInicio; }
    public LocalDate getPeriodoFim() { return periodoFim; }
    public void setPeriodoFim(LocalDate periodoFim) { this.periodoFim = periodoFim; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}

