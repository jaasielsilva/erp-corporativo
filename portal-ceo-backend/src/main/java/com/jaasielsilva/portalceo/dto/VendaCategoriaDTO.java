package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VendaCategoriaDTO {
    private String categoria;
    private BigDecimal total;

}
