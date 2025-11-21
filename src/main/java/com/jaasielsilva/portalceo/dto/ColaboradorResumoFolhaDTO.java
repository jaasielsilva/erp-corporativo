package com.jaasielsilva.portalceo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorResumoFolhaDTO {
    private Long id;
    private String nome;
    private String cargoNome;
    private String departamentoNome;
    private BigDecimal salario;
    private Integer diasTrabalhados;
    private Integer diasMes;
    private Integer diasUteisMes;
    private String status;
    private Boolean temEscala;
}