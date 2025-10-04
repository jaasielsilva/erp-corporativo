package com.jaasielsilva.portalceo.dto;

public class CorrecaoPontoResumoDTO {
    public long pendentes;
    public long aprovadas;
    public long rejeitadas;
    public Double tempoMedioDias;

    public CorrecaoPontoResumoDTO(long pendentes, long aprovadas, long rejeitadas, Double tempoMedioDias) {
        this.pendentes = pendentes;
        this.aprovadas = aprovadas;
        this.rejeitadas = rejeitadas;
        this.tempoMedioDias = tempoMedioDias;
    }
}