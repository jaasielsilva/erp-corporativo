package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaItemDTO {
    private Long id;
    private Long produtoId;
    private String produtoNome;
    private String produtoEan;
    private BigDecimal produtoPreco;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}