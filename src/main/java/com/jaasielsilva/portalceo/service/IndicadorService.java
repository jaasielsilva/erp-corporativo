package com.jaasielsilva.portalceo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.indicadores.ViewMargemLucro;
import com.jaasielsilva.portalceo.repository.indicadores.ViewMargemLucroRepository;

@Service
public class IndicadorService {

    private final VendaService vendaService;
    private final FinanceiroService financeiroService;
    private final ViewMargemLucroRepository margemLucroRepository;

    @Autowired
    public IndicadorService(VendaService vendaService,
                            FinanceiroService financeiroService,
                            ViewMargemLucroRepository margemLucroRepository) {
        this.vendaService = vendaService;
        this.financeiroService = financeiroService;
        this.margemLucroRepository = margemLucroRepository;
    }

    /**
     * Retorna a margem de lucro a partir da view SQL (view_margem_lucro)
     */
    public BigDecimal getMargemLucro() {
        return margemLucroRepository.findById(1)
                .map(ViewMargemLucro::getMargemLucro)
                .orElse(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o ticket médio das vendas
     */
    public BigDecimal getTicketMedio() {
        BigDecimal totalVendas = vendaService.calcularTotalDeVendas();
        long quantidadeVendas = vendaService.contarTotalVendas();

        if (quantidadeVendas == 0) {
            return BigDecimal.ZERO;
        }

        return totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o ROI mensal com base nos investimentos e retornos
     */
    public BigDecimal getRoiMensal() {
        BigDecimal investimentos = financeiroService.calcularTotalInvestimentos();
        BigDecimal retornos = financeiroService.calcularTotalRetornos();

        if (investimentos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return retornos.subtract(investimentos)
                .multiply(BigDecimal.valueOf(100))
                .divide(investimentos, 1, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a taxa de inadimplência
     */
    public BigDecimal getInadimplencia() {
        BigDecimal totalVencido = financeiroService.calcularTotalContasVencidas();
        BigDecimal totalReceber = financeiroService.calcularTotalContasReceber();

        if (totalReceber.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalVencido.multiply(BigDecimal.valueOf(100))
                .divide(totalReceber, 1, RoundingMode.HALF_UP);
    }

    /**
     * Formata um valor BigDecimal como percentual (ex: "40.0%")
     */
    public String formatarPercentual(BigDecimal valor) {
        if (valor == null) {
            return "0.0%";
        }
        return valor.setScale(1, RoundingMode.HALF_UP) + "%";
    }

    /**
     * Formata um valor BigDecimal como moeda brasileira (ex: "R$ 32.299,15")
     */
    public String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(valor);
    }
}
