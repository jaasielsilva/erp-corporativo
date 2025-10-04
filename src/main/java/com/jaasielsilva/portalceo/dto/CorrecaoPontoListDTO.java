package com.jaasielsilva.portalceo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CorrecaoPontoListDTO {
    public Long id;
    public String protocolo;
    public Long colaboradorId;
    public String colaboradorNome;
    public LocalDate data;
    public String tipo;
    public LocalTime horarioAnterior;
    public LocalTime horarioNovo;
    public String justificativaResumo;
    public String status;
    public LocalDateTime solicitadoEm;

    public CorrecaoPontoListDTO(Long id, String protocolo, Long colaboradorId, String colaboradorNome,
                                LocalDate data, String tipo, LocalTime horarioAnterior, LocalTime horarioNovo,
                                String justificativaResumo, String status, LocalDateTime solicitadoEm) {
        this.id = id;
        this.protocolo = protocolo;
        this.colaboradorId = colaboradorId;
        this.colaboradorNome = colaboradorNome;
        this.data = data;
        this.tipo = tipo;
        this.horarioAnterior = horarioAnterior;
        this.horarioNovo = horarioNovo;
        this.justificativaResumo = justificativaResumo;
        this.status = status;
        this.solicitadoEm = solicitadoEm;
    }
}