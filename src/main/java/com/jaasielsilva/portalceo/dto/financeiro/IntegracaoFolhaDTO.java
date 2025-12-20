package com.jaasielsilva.portalceo.dto.financeiro;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IntegracaoFolhaDTO {
    private Long folhaId;
    private BigDecimal valorTotal;
    private LocalDate dataVencimento;
    private String hashIntegridade;
    private String usuarioResponsavel;
    private String tipoFolha;
}
