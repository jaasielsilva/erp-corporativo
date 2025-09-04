package com.jaasielsilva.portalceo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para estatísticas do resumo mensal do Vale Transporte
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumoValeTransporteDTO {
    
    private Integer totalColaboradoresAtivos;
    private Integer totalColaboradores;
    private BigDecimal custoTotalMes;
    private BigDecimal totalDescontoColaboradores;
    private BigDecimal totalSubsidioEmpresa;
    private Integer mesReferencia;
    private Integer anoReferencia;
    private Double percentualDescontoMedio;
    private Integer totalValesAtivos;
    private Integer totalValesSuspensos;
    private Integer totalValesCancelados;
    
    // Métodos de conveniência
    public String getMesAnoFormatado() {
        if (mesReferencia != null && anoReferencia != null) {
            String[] meses = {"", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                              "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
            return meses[mesReferencia] + "/" + anoReferencia;
        }
        return "";
    }
    
    public Double getPercentualDesconto() {
        if (custoTotalMes != null && custoTotalMes.compareTo(BigDecimal.ZERO) > 0 && totalDescontoColaboradores != null) {
            return totalDescontoColaboradores.divide(custoTotalMes, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }
    
    public Double getPercentualSubsidio() {
        if (custoTotalMes != null && custoTotalMes.compareTo(BigDecimal.ZERO) > 0 && totalSubsidioEmpresa != null) {
            return totalSubsidioEmpresa.divide(custoTotalMes, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }
}