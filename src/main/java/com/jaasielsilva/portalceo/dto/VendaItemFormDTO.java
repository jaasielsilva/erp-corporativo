package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaItemFormDTO {
    private Long produtoId;
    private int quantidade;
    private BigDecimal precoUnitario;
}