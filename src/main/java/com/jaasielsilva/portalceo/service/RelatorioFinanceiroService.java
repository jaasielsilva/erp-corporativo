package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.financeiro.RelatorioDREDTO;
import com.jaasielsilva.portalceo.dto.financeiro.RelatorioFluxoCaixaDTO;
import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioFinanceiroService {

    @Autowired
    private FluxoCaixaRepository fluxoCaixaRepository;

    /**
     * Gera o DRE (Demonstrativo do Resultado do Exercício) baseado no Regime de Caixa (Fluxo Realizado)
     */
    public RelatorioDREDTO gerarDRE(LocalDate inicio, LocalDate fim) {
        RelatorioDREDTO dre = new RelatorioDREDTO();
        
        // Inicializa mapas de detalhe
        Map<String, BigDecimal> detalheReceitas = new HashMap<>();
        Map<String, BigDecimal> detalheDespesas = new HashMap<>();
        
        // Busca entradas agrupadas por categoria
        List<Object[]> entradasPorCategoria = fluxoCaixaRepository.sumValorByCategoriaAndTipoMovimento(
                FluxoCaixa.TipoMovimento.ENTRADA, inicio, fim);
        
        BigDecimal receitaBruta = BigDecimal.ZERO;
        
        for (Object[] row : entradasPorCategoria) {
            FluxoCaixa.CategoriaFluxo cat = (FluxoCaixa.CategoriaFluxo) row[0];
            BigDecimal valor = (BigDecimal) row[1];
            
            detalheReceitas.put(cat.getDescricao(), valor);
            
            // Lógica simples de mapeamento para DRE
            if (cat != FluxoCaixa.CategoriaFluxo.TRANSFERENCIA) {
                receitaBruta = receitaBruta.add(valor);
            }
        }
        
        // Busca saídas agrupadas por categoria
        List<Object[]> saidasPorCategoria = fluxoCaixaRepository.sumValorByCategoriaAndTipoMovimento(
                FluxoCaixa.TipoMovimento.SAIDA, inicio, fim);
        
        BigDecimal impostos = BigDecimal.ZERO;
        BigDecimal custosVariaveis = BigDecimal.ZERO;
        BigDecimal despesasFixas = BigDecimal.ZERO;
        
        for (Object[] row : saidasPorCategoria) {
            FluxoCaixa.CategoriaFluxo cat = (FluxoCaixa.CategoriaFluxo) row[0];
            BigDecimal valor = (BigDecimal) row[1];
            
            detalheDespesas.put(cat.getDescricao(), valor);
            
            if (cat == FluxoCaixa.CategoriaFluxo.IMPOSTOS) {
                impostos = impostos.add(valor);
            } else if (cat == FluxoCaixa.CategoriaFluxo.FORNECEDORES || cat == FluxoCaixa.CategoriaFluxo.COMBUSTIVEL) {
                custosVariaveis = custosVariaveis.add(valor);
            } else if (cat != FluxoCaixa.CategoriaFluxo.TRANSFERENCIA && cat != FluxoCaixa.CategoriaFluxo.INVESTIMENTO) {
                despesasFixas = despesasFixas.add(valor);
            }
        }
        
        dre.setReceitaBruta(receitaBruta);
        dre.setImpostos(impostos);
        dre.setReceitaLiquida(receitaBruta.subtract(impostos));
        dre.setCustosVariaveis(custosVariaveis);
        dre.setMargemContribuicao(dre.getReceitaLiquida().subtract(custosVariaveis));
        dre.setDespesasFixas(despesasFixas);
        dre.setResultadoOperacional(dre.getMargemContribuicao().subtract(despesasFixas));
        // Resultado Liquido pode incluir investimentos/financeiro depois
        dre.setResultadoLiquido(dre.getResultadoOperacional()); 
        
        dre.setDetalheReceitas(detalheReceitas);
        dre.setDetalheDespesas(detalheDespesas);
        
        return dre;
    }

    /**
     * Gera o Relatório de Fluxo de Caixa (Diário)
     */
    public RelatorioFluxoCaixaDTO gerarFluxoCaixa(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            LocalDate tmp = inicio;
            inicio = fim;
            fim = tmp;
        }
        RelatorioFluxoCaixaDTO relatorio = new RelatorioFluxoCaixaDTO();
        
        // 1. Calcular Saldo Inicial (acumulado até o dia anterior ao inicio)
        BigDecimal saldoInicial = fluxoCaixaRepository.calcularSaldoAcumuladoAte(inicio.minusDays(1));
        if (saldoInicial == null) saldoInicial = BigDecimal.ZERO;
        relatorio.setSaldoInicial(saldoInicial);
        
        // 2. Buscar totais do período
        BigDecimal totalEntradas = fluxoCaixaRepository.sumEntradasByPeriodo(inicio, fim);
        BigDecimal totalSaidas = fluxoCaixaRepository.sumSaidasByPeriodo(inicio, fim);
        
        if (totalEntradas == null) totalEntradas = BigDecimal.ZERO;
        if (totalSaidas == null) totalSaidas = BigDecimal.ZERO;
        
        relatorio.setTotalEntradas(totalEntradas);
        relatorio.setTotalSaidas(totalSaidas);
        relatorio.setSaldoFinal(saldoInicial.add(totalEntradas).subtract(totalSaidas));
        
        // 3. Buscar fluxo diário
        List<Object[]> fluxoDiarioDB = fluxoCaixaRepository.getFluxoDiario(inicio, fim);
        List<RelatorioFluxoCaixaDTO.ItemFluxoDiario> fluxoDiario = new ArrayList<>();
        
        BigDecimal saldoCorrente = saldoInicial;
        
        // Preencher dias sem movimento também? Por enquanto apenas dias com movimento
        // Idealmente iteraria por todas as datas do período
        
        // Mapa para acesso rápido
        Map<LocalDate, BigDecimal[]> movimentoPorData = new HashMap<>();
        for (Object[] row : fluxoDiarioDB) {
            LocalDate data;
            if (row[0] instanceof java.sql.Date) {
                data = ((java.sql.Date) row[0]).toLocalDate();
            } else if (row[0] instanceof LocalDate) {
                data = (LocalDate) row[0];
            } else {
                data = LocalDate.parse(row[0].toString());
            }

            BigDecimal entradas = (BigDecimal) row[1];
            BigDecimal saidas = (BigDecimal) row[2];
            movimentoPorData.put(data, new BigDecimal[]{entradas, saidas});
        }
        
        for (LocalDate date = inicio; !date.isAfter(fim); date = date.plusDays(1)) {
            BigDecimal[] mov = movimentoPorData.getOrDefault(date, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal entradas = mov[0];
            BigDecimal saidas = mov[1];
            
            saldoCorrente = saldoCorrente.add(entradas).subtract(saidas);
            
            fluxoDiario.add(new RelatorioFluxoCaixaDTO.ItemFluxoDiario(
                    date,
                    entradas,
                    saidas,
                    saldoCorrente
            ));
        }
        
        relatorio.setFluxoDiario(fluxoDiario);
        
        return relatorio;
    }
}
