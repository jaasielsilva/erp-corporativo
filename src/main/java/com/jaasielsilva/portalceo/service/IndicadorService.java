package com.jaasielsilva.portalceo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class IndicadorService {

    private final VendaService vendaService;

    public IndicadorService(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    public BigDecimal getTicketMedio() {
        BigDecimal totalVendas = vendaService.calcularTotalDeVendas();
        long quantidadeVendas = vendaService.contarTotalVendas();

        if (quantidadeVendas == 0) return BigDecimal.ZERO;

        return totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }
}
