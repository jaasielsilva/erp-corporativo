package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaDTO {
    private Long id;
    private String numeroVenda;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private String formaPagamento;
    private Integer parcelas;
    private BigDecimal valorPago;
    private BigDecimal troco;
    private LocalDateTime dataVenda;
    private Long clienteId;
    private String clienteNome;
    private Long usuarioId;
    private Long caixaId;
    private String status;
    private String observacoes;
    private String cupomFiscal;
    private List<VendaItemDTO> itens = new ArrayList<>();
}