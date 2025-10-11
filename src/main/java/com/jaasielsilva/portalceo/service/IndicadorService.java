package com.jaasielsilva.portalceo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.indicadores.ViewMargemLucro;
import com.jaasielsilva.portalceo.model.indicadores.ViewInadimplencia;
import com.jaasielsilva.portalceo.repository.indicadores.ViewMargemLucroRepository;
import com.jaasielsilva.portalceo.repository.indicadores.ViewInadimplenciaRepository;

@Service
public class IndicadorService {

    private final VendaService vendaService;
    private final FinanceiroService financeiroService;
    private final ViewMargemLucroRepository margemLucroRepository;
    private final ViewInadimplenciaRepository inadimplenciaRepository;

    @Autowired
    public IndicadorService(
            VendaService vendaService,
            FinanceiroService financeiroService,
            ViewMargemLucroRepository margemLucroRepository,
            ViewInadimplenciaRepository inadimplenciaRepository) {
        this.vendaService = vendaService;
        this.financeiroService = financeiroService;
        this.margemLucroRepository = margemLucroRepository;
        this.inadimplenciaRepository = inadimplenciaRepository;
    }

    /** Margem de lucro (view SQL) */
    public BigDecimal getMargemLucro() {
        return margemLucroRepository.findById(1)
                .map(ViewMargemLucro::getMargemLucro)
                .orElse(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);
    }

    /** Taxa de inadimplência (via view) */
    public BigDecimal getInadimplenciaView() {
        return inadimplenciaRepository.findById(1)
                .map(ViewInadimplencia::getTaxaInadimplencia)
                .orElse(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);
    }

    /** Ticket médio das vendas */
    public BigDecimal getTicketMedio() {
        BigDecimal totalVendas = vendaService.calcularTotalDeVendas();
        long quantidadeVendas = vendaService.contarTotalVendas();

        if (quantidadeVendas == 0) {
            return BigDecimal.ZERO;
        }

        return totalVendas.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }

    /** ROI mensal */
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

    /** Inadimplência com fallback manual */
    public BigDecimal getInadimplencia() {
        try {
            BigDecimal valorView = inadimplenciaRepository.findAll()
                    .stream()
                    .findFirst()
                    .map(ViewInadimplencia::getTaxaInadimplencia)

                    .orElse(BigDecimal.ZERO);

            if (valorView.compareTo(BigDecimal.ZERO) > 0) {
                return valorView;
            }

        } catch (Exception e) {
            System.err.println("⚠️ Falha ao buscar view_inadimplencia: " + e.getMessage());
        }

        BigDecimal totalVencido = financeiroService.calcularTotalContasVencidas();
        BigDecimal totalReceber = financeiroService.calcularTotalContasReceber();

        if (totalReceber.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalVencido.multiply(BigDecimal.valueOf(100))
                .divide(totalReceber, 1, RoundingMode.HALF_UP);
    }

    /** Formata percentual */
    public String formatarPercentual(BigDecimal valor) {
        if (valor == null) {
            return "0,0%";
        }
        return valor.setScale(1, RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",") + "%";
    }

    /** Formata moeda BR */
    public String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(valor);
    }
}
