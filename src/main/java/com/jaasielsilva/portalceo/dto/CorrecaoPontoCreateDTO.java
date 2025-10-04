package com.jaasielsilva.portalceo.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CorrecaoPontoCreateDTO {
    private Long colaboradorId;
    private LocalDate data;
    private String tipoRegistro; // entrada, saida-almoco, volta-almoco, saida
    private String tipoCorrecao; // mapeado para enum TipoCorrecao
    private LocalTime horarioAnterior;
    private LocalTime horarioNovo;
    private String justificativa;

    public Long getColaboradorId() { return colaboradorId; }
    public void setColaboradorId(Long colaboradorId) { this.colaboradorId = colaboradorId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(String tipoRegistro) { this.tipoRegistro = tipoRegistro; }

    public String getTipoCorrecao() { return tipoCorrecao; }
    public void setTipoCorrecao(String tipoCorrecao) { this.tipoCorrecao = tipoCorrecao; }

    public LocalTime getHorarioAnterior() { return horarioAnterior; }
    public void setHorarioAnterior(LocalTime horarioAnterior) { this.horarioAnterior = horarioAnterior; }

    public LocalTime getHorarioNovo() { return horarioNovo; }
    public void setHorarioNovo(LocalTime horarioNovo) { this.horarioNovo = horarioNovo; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}