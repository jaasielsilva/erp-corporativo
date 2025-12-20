package com.jaasielsilva.portalceo.dto.financeiro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioFluxoCaixaDTO {
    private BigDecimal saldoInicial;
    private BigDecimal totalEntradas;
    private BigDecimal totalSaidas;
    private BigDecimal saldoFinal;
    
    private List<ItemFluxoDiario> fluxoDiario;
    
    @Data
    @AllArgsConstructor
    public static class ItemFluxoDiario {
        private LocalDate data;
        private BigDecimal entradas;
        private BigDecimal saidas;
        private BigDecimal saldoAcumulado;
    }
}
