package com.jaasielsilva.portalceo.dto.financeiro;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransferenciaDTO {
    private Long contaOrigemId;
    private Long contaDestinoId;
    private Long clienteDestinoId;
    private Boolean pagamentoCliente;
    private BigDecimal valor;
    private LocalDate dataTransferencia;
    private String descricao;
    private String observacoes;
}
