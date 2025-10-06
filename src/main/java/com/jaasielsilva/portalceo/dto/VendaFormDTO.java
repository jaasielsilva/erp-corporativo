package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaFormDTO {
    private Long clienteId;
    private LocalDate dataVenda;
    private BigDecimal desconto = BigDecimal.ZERO;
    private String formaPagamento;
    private Integer parcelas = 1;
    private BigDecimal valorPago;
    private List<VendaItemFormDTO> itens = new ArrayList<>();
}