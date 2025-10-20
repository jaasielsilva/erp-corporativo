package com.jaasielsilva.portalceo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaPagarDto {
    public Long id;
    public String descricao;
    public Long fornecedorId;
    public BigDecimal valorOriginal;
    public LocalDate dataVencimento;
    public LocalDate dataEmissao;
    public String status;
    public String categoria;
    public String numeroDocumento;
    public String observacoes;
}
