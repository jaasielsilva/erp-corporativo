package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.financeiro.RelatorioDREDTO;
import com.jaasielsilva.portalceo.dto.financeiro.RelatorioFluxoCaixaDTO;
import com.jaasielsilva.portalceo.model.ContaContabil;
import com.jaasielsilva.portalceo.model.FluxoCaixa;
import com.jaasielsilva.portalceo.repository.ContaContabilRepository;
import com.jaasielsilva.portalceo.repository.FluxoCaixaRepository;
import com.jaasielsilva.portalceo.repository.LancamentoContabilItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RelatorioFinanceiroService {

    @Autowired
    private FluxoCaixaRepository fluxoCaixaRepository;

    @Autowired
    private LancamentoContabilItemRepository lancamentoContabilItemRepository;

    @Autowired
    private ContaContabilRepository contaContabilRepository;

    @Autowired
    private ContabilidadeService contabilidadeService;

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

    public RelatorioDREDTO gerarDREContabil(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            LocalDate tmp = inicio;
            inicio = fim;
            fim = tmp;
        }

        contabilidadeService.sincronizarLancamentosAte(fim);

        RelatorioDREDTO dre = new RelatorioDREDTO();

        Map<String, BigDecimal> detalheReceitas = new LinkedHashMap<>();
        Map<String, BigDecimal> detalheDespesas = new LinkedHashMap<>();

        List<Object[]> rows = lancamentoContabilItemRepository.sumDebitoCreditoPeriodo(inicio, fim);
        Set<Long> contaIds = rows.stream().map(r -> (Long) r[0]).collect(Collectors.toSet());

        Map<Long, ContaContabil> contas = contaContabilRepository.findAllById(contaIds).stream()
                .collect(Collectors.toMap(ContaContabil::getId, c -> c));

        BigDecimal receitaBruta = BigDecimal.ZERO;
        BigDecimal impostos = BigDecimal.ZERO;
        BigDecimal custosVariaveis = BigDecimal.ZERO;
        BigDecimal despesasFixas = BigDecimal.ZERO;

        for (Object[] r : rows) {
            Long contaId = (Long) r[0];
            BigDecimal debito = (BigDecimal) r[1];
            BigDecimal credito = (BigDecimal) r[2];

            ContaContabil conta = contas.get(contaId);
            if (conta == null) continue;

            if (conta.getTipo() == ContaContabil.TipoConta.RECEITA) {
                BigDecimal valor = credito.subtract(debito);
                detalheReceitas.put(conta.getNome(), valor);
                receitaBruta = receitaBruta.add(valor);
            } else if (conta.getTipo() == ContaContabil.TipoConta.DESPESA) {
                BigDecimal valor = debito.subtract(credito);
                detalheDespesas.put(conta.getNome(), valor);
                if (conta.getGrupo() == ContaContabil.GrupoConta.IMPOSTOS_SOBRE_VENDAS
                        || conta.getGrupo() == ContaContabil.GrupoConta.IMPOSTOS_SOBRE_LUCRO) {
                    impostos = impostos.add(valor);
                } else if (conta.getGrupo() == ContaContabil.GrupoConta.CUSTOS) {
                    custosVariaveis = custosVariaveis.add(valor);
                } else {
                    despesasFixas = despesasFixas.add(valor);
                }
            }
        }

        dre.setReceitaBruta(receitaBruta);
        dre.setImpostos(impostos);
        dre.setReceitaLiquida(receitaBruta.subtract(impostos));
        dre.setCustosVariaveis(custosVariaveis);
        dre.setMargemContribuicao(dre.getReceitaLiquida().subtract(custosVariaveis));
        dre.setDespesasFixas(despesasFixas);
        dre.setResultadoOperacional(dre.getMargemContribuicao().subtract(despesasFixas));
        dre.setResultadoLiquido(dre.getResultadoOperacional());

        dre.setDetalheReceitas(detalheReceitas);
        dre.setDetalheDespesas(detalheDespesas);

        return dre;
    }

    public Map<String, Object> gerarBalancoPatrimonial(LocalDate ate) {
        contabilidadeService.sincronizarLancamentosAte(ate);

        Map<String, BigDecimal> ativos = new LinkedHashMap<>();
        Map<String, BigDecimal> passivos = new LinkedHashMap<>();
        Map<String, BigDecimal> patrimonioLiquido = new LinkedHashMap<>();

        ativos.put("Caixa e Bancos", BigDecimal.ZERO);
        ativos.put("Contas a Receber", BigDecimal.ZERO);
        ativos.put("Estoques", BigDecimal.ZERO);
        ativos.put("Imobilizado", BigDecimal.ZERO);
        ativos.put("Outros Ativos", BigDecimal.ZERO);

        passivos.put("Contas a Pagar", BigDecimal.ZERO);
        passivos.put("Financiamentos", BigDecimal.ZERO);
        passivos.put("Obrigações Tributárias", BigDecimal.ZERO);
        passivos.put("Outros Passivos", BigDecimal.ZERO);

        patrimonioLiquido.put("Capital Social", BigDecimal.ZERO);
        patrimonioLiquido.put("Resultado do Exercício", BigDecimal.ZERO);
        patrimonioLiquido.put("Outros", BigDecimal.ZERO);

        List<Object[]> rows = lancamentoContabilItemRepository.sumDebitoCreditoAte(ate);
        Set<Long> contaIds = rows.stream().map(r -> (Long) r[0]).collect(Collectors.toSet());
        Map<Long, ContaContabil> contas = contaContabilRepository.findAllById(contaIds).stream()
                .collect(Collectors.toMap(ContaContabil::getId, c -> c));

        for (Object[] r : rows) {
            Long contaId = (Long) r[0];
            BigDecimal debito = (BigDecimal) r[1];
            BigDecimal credito = (BigDecimal) r[2];

            ContaContabil conta = contas.get(contaId);
            if (conta == null) continue;

            BigDecimal saldo = saldoNormal(conta, debito, credito);

            if (conta.getTipo() == ContaContabil.TipoConta.ATIVO) {
                String chave = switch (conta.getGrupo()) {
                    case CAIXA_BANCOS -> "Caixa e Bancos";
                    case CONTAS_RECEBER -> "Contas a Receber";
                    case ESTOQUES -> "Estoques";
                    case IMOBILIZADO -> "Imobilizado";
                    default -> "Outros Ativos";
                };
                ativos.put(chave, ativos.get(chave).add(saldo));
            } else if (conta.getTipo() == ContaContabil.TipoConta.PASSIVO) {
                String chave = switch (conta.getGrupo()) {
                    case CONTAS_PAGAR -> "Contas a Pagar";
                    case FINANCIAMENTOS -> "Financiamentos";
                    case OBRIGACOES_TRIBUTARIAS -> "Obrigações Tributárias";
                    default -> "Outros Passivos";
                };
                passivos.put(chave, passivos.get(chave).add(saldo));
            } else if (conta.getTipo() == ContaContabil.TipoConta.PATRIMONIO_LIQUIDO) {
                String chave = switch (conta.getGrupo()) {
                    case CAPITAL_SOCIAL -> "Capital Social";
                    case RESULTADO_EXERCICIO -> "Resultado do Exercício";
                    default -> "Outros";
                };
                patrimonioLiquido.put(chave, patrimonioLiquido.get(chave).add(saldo));
            }
        }

        BigDecimal totalAtivo = ativos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPassivo = passivos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPL = patrimonioLiquido.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPassivoPL = totalPassivo.add(totalPL);

        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("ativos", ativos);
        relatorio.put("passivos", passivos);
        relatorio.put("patrimonioLiquido", patrimonioLiquido);
        relatorio.put("totalAtivo", totalAtivo);
        relatorio.put("totalPassivo", totalPassivo);
        relatorio.put("totalPL", totalPL);
        relatorio.put("totalPassivoPL", totalPassivoPL);
        relatorio.put("diferenca", totalAtivo.subtract(totalPassivoPL));
        relatorio.put("dataBase", ate);
        return relatorio;
    }

    private BigDecimal saldoNormal(ContaContabil conta, BigDecimal debito, BigDecimal credito) {
        BigDecimal d = debito != null ? debito : BigDecimal.ZERO;
        BigDecimal c = credito != null ? credito : BigDecimal.ZERO;
        if (conta.getTipo() == ContaContabil.TipoConta.ATIVO || conta.getTipo() == ContaContabil.TipoConta.DESPESA) {
            return d.subtract(c);
        }
        return c.subtract(d);
    }
}
