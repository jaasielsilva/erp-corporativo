package com.jaasielsilva.portalceo.dto.financeiro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioDREDTO {
    private BigDecimal receitaBruta;
    private BigDecimal impostos;
    private BigDecimal receitaLiquida;
    private BigDecimal custosVariaveis;
    private BigDecimal margemContribuicao;
    private BigDecimal despesasFixas;
    private BigDecimal resultadoOperacional; // EBIT
    private BigDecimal resultadoLiquido;
    
    // Detalhamento por categoria
    private Map<String, BigDecimal> detalheReceitas;
    private Map<String, BigDecimal> detalheDespesas;
}
