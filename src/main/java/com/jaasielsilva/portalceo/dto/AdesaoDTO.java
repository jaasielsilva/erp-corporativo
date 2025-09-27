package com.jaasielsilva.portalceo.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AdesaoDTO {
    private Long colaboradorId;
    private String planoNome;
    private int quantidadeDependentes;
    private Long planoId;
    private String valorTotal;
    private LocalDate dataVigencia;
    private String tipoAdesao;
    private String observacoes;
    private boolean processarImediatamente;
    
}
