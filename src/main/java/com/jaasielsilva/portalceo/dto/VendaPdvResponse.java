package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VendaPdvResponse {
    private Long Id;
    private String mensagem;
    private boolean sucesso;
    private String NumeroVenda;
    private LocalDateTime DataVenda;
    private BigDecimal Total;
    private BigDecimal Troco;

}