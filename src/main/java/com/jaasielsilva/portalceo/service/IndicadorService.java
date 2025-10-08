package com.jaasielsilva.portalceo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndicadorService {

    private final VendaService vendaService;
    
    @Autowired
    private FinanceiroService financeiroService;

    public IndicadorService(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    /**
     * Calcula o ticket médio das vendas
     * @return Valor do ticket médio
     */
    public BigDecimal getTicketMedio() {
        BigDecimal totalVendas = vendaService.calcularTotalDeVendas();
        long quantidadeVendas = vendaService.contarTotalVendas();

        if (quantidadeVendas == 0) return BigDecimal.ZERO;

        return totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula a margem de lucro com base nas vendas e custos
     * @return Percentual de margem de lucro
     */
    public BigDecimal getMargemLucro() {
        BigDecimal totalVendas = vendaService.calcularTotalDeVendas();
        BigDecimal totalCustos = vendaService.calcularTotalDeCustos();
        
        if (totalVendas.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        BigDecimal lucro = totalVendas.subtract(totalCustos);
        return lucro.multiply(new BigDecimal("100"))
                .divide(totalVendas, 1, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula o ROI mensal com base nos investimentos e retornos
     * @return Percentual de ROI mensal
     */
    public BigDecimal getRoiMensal() {
        BigDecimal investimentos = financeiroService.calcularTotalInvestimentos();
        BigDecimal retornos = financeiroService.calcularTotalRetornos();
        
        if (investimentos.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        BigDecimal roi = retornos.subtract(investimentos)
                .multiply(new BigDecimal("100"))
                .divide(investimentos, 1, RoundingMode.HALF_UP);
        
        return roi;
    }
    
    /**
     * Calcula a taxa de inadimplência
     * @return Percentual de inadimplência
     */
    public BigDecimal getInadimplencia() {
        BigDecimal totalVencido = financeiroService.calcularTotalContasVencidas();
        BigDecimal totalReceber = financeiroService.calcularTotalContasReceber();
        
        if (totalReceber.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        return totalVencido.multiply(new BigDecimal("100"))
                .divide(totalReceber, 1, RoundingMode.HALF_UP);
    }
    
    /**
     * Formata um valor BigDecimal como percentual
     * @param valor Valor a ser formatado
     * @return String formatada com o percentual
     */
    public String formatarPercentual(BigDecimal valor) {
        if (valor == null) return "0.0%";
        return valor.setScale(1, RoundingMode.HALF_UP) + "%";
    }
}
