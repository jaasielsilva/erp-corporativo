package com.jaasielsilva.portalceo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para configurações do Vale Transporte
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoValeTransporteDTO {
    
    private BigDecimal percentualDesconto = BigDecimal.valueOf(6.0); // 6% padrão
    private BigDecimal valorPassagem = BigDecimal.valueOf(4.40); // Valor padrão da passagem
    private Integer diasUteisPadrao = 22; // Dias úteis padrão por mês
    private BigDecimal percentualMaximoDesconto = BigDecimal.valueOf(6.0); // Máximo legal
    
    // Validações
    public boolean isPercentualValido() {
        return percentualDesconto != null && 
               percentualDesconto.compareTo(BigDecimal.ZERO) >= 0 && 
               percentualDesconto.compareTo(percentualMaximoDesconto) <= 0;
    }
    
    public boolean isValorPassagemValido() {
        return valorPassagem != null && valorPassagem.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isDiasUteisValido() {
        return diasUteisPadrao != null && diasUteisPadrao > 0 && diasUteisPadrao <= 31;
    }
}