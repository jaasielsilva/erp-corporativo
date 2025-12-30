package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DashboardMetricsService {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProdutoService produtoService;
    @Autowired
    private ColaboradorService colaboradorService;
    @Autowired
    private SolicitacaoAcessoService solicitacaoAcessoService;
    @Autowired
    private EstoqueService estoqueService;
    @Autowired
    private WorkflowAdesaoService workflowAdesaoService;
    @Autowired
    private IndicadorService indicadorService;
    @Autowired
    private ContaReceberService contaReceberService;
    @Autowired
    private UsuarioService usuarioService;

    @Cacheable(cacheNames = "dashboardMetrics", key = "'global'")
    public DashboardMetricsDTO getMetrics() {
        long totalClientes = clienteService.contarTotal();
        long novosClientes30Dias = clienteService.contarNovosPorPeriodo(30);
        YearMonth agoraYm = YearMonth.now();
        java.time.LocalDate inicio12 = agoraYm.minusMonths(11).atDay(1);
        java.time.LocalDate fim12 = agoraYm.atEndOfMonth();
        long totalVendas;
        try {
            totalVendas = contaReceberService.contarRecebimentosPeriodo(java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now());
        } catch (Exception e) {
            totalVendas = 0;
        }
        long totalVendasAnterior = contaReceberService.contarRecebimentosPeriodo(java.time.LocalDate.now().minusDays(60), java.time.LocalDate.now().minusDays(31));
        String crescimentoVendas = totalVendasAnterior == 0 ? "+0%" :
                String.format("%+,.1f%%", ((double)(totalVendas - totalVendasAnterior) / (double) totalVendasAnterior) * 100.0);
        long produtosEstoque = produtoService.somarQuantidadeEstoque();
        BigDecimal faturamentoMensal = indicadorService.getRoiMensal();
        String crescimentoFaturamento = "0%";
        long produtosCriticos = produtoService.contarProdutosCriticos();
        long totalFuncionarios = colaboradorService.contarAtivos();
        long contratacoes12Meses = colaboradorService.contarContratacaosPorPeriodo(12);
        long solicitacoesPendentes = solicitacaoAcessoService.contarSolicitacoesPendentes();
        long solicitacoesAtrasadas = solicitacaoAcessoService.contarSolicitacoesAtrasadas();
        WorkflowAdesaoService.DashboardEstatisticas estatisticasAdesao = workflowAdesaoService.obterEstatisticas();
        long processosAdesaoTotal = estatisticasAdesao.getProcessosPorStatus().values().stream()
                .mapToLong(Long::longValue).sum();
        long processosAguardandoAprovacao = estatisticasAdesao.getProcessosAguardandoAprovacao();
        List<String> adesaoRHLabels = workflowAdesaoService.obterLabelsUltimos6Meses();
        List<Integer> adesaoRHValores = workflowAdesaoService.obterDadosAdesaoUltimos6Meses();

        List<Long> solicitacoesStatusLong = solicitacaoAcessoService.obterValoresGraficoStatus();
        List<Integer> solicitacoesStatus = new ArrayList<>();
        for (Long valor : solicitacoesStatusLong) {
            solicitacoesStatus.add(valor.intValue());
        }

        Map<YearMonth, BigDecimal> vendasUltimos12Meses = contaReceberService.getRecebimentosPorMes(inicio12, fim12);
        List<String> ultimos12MesesLabels = new ArrayList<>();
        List<BigDecimal> ultimos12MesesValores = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = agoraYm.minusMonths(i);
            BigDecimal valor = vendasUltimos12Meses.getOrDefault(ym, BigDecimal.ZERO);
            String label = ym.getMonth().name().substring(0, 3) + "/" + String.valueOf(ym.getYear()).substring(2);
            ultimos12MesesLabels.add(label);
            ultimos12MesesValores.add(valor);
        }
        List<BigDecimal> metaVendasMensal = new ArrayList<>();
        BigDecimal metaBase = faturamentoMensal.multiply(new BigDecimal("1.2"));
        for (int i = 0; i < 12; i++) {
            metaVendasMensal.add(metaBase);
        }

        Map<com.jaasielsilva.portalceo.model.ContaReceber.CategoriaContaReceber, BigDecimal> porCategoria =
                contaReceberService.getRecebimentosPorCategoria(inicio12, fim12);
        List<String> categoriasLabels = new ArrayList<>();
        List<BigDecimal> categoriasValores = new ArrayList<>();
        for (var e : porCategoria.entrySet()) {
            categoriasLabels.add(e.getKey() != null ? e.getKey().name() : "—");
            categoriasValores.add(e.getValue() != null ? e.getValue() : BigDecimal.ZERO);
        }

        List<Integer> performanceIndicadores = java.util.Arrays.asList(
                75,
                solicitacaoAcessoService.calcularPerformanceAtendimento(),
                estoqueService.calcularPerformanceLogistica(),
                clienteService.calcularPerformanceQualidade(),
                usuarioService.calcularPerformanceFinanceiro());

        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        dto.totalClientes = totalClientes;
        dto.novosClientes30Dias = novosClientes30Dias;
        dto.totalVendas = totalVendas;
        dto.crescimentoVendas = crescimentoVendas;
        dto.totalProdutos = produtosEstoque;
        dto.faturamentoMensalFormatado = indicadorService.formatarMoeda(faturamentoMensal);
        dto.crescimentoFaturamento = crescimentoFaturamento;
        dto.produtosCriticos = produtosCriticos;
        dto.totalFuncionarios = totalFuncionarios;
        dto.contratacoes12Meses = contratacoes12Meses;
        dto.solicitacoesPendentes = solicitacoesPendentes;
        dto.solicitacoesAtrasadas = solicitacoesAtrasadas;
        dto.processosAdesaoTotal = processosAdesaoTotal;
        dto.processosAguardandoAprovacao = processosAguardandoAprovacao;
        dto.percentualMeta = "87%";
        dto.ticketMedioFormatado = indicadorService.formatarMoeda(indicadorService.getTicketMedio());
        dto.ultimos12MesesLabels = ultimos12MesesLabels;
        dto.ultimos12MesesValores = ultimos12MesesValores;
        dto.metaVendasMensal = metaVendasMensal;
        dto.categoriasLabels = categoriasLabels;
        dto.categoriasValores = categoriasValores;
        dto.solicitacoesStatus = solicitacoesStatus;
        dto.performanceIndicadores = performanceIndicadores;
        dto.margemLucro = indicadorService.formatarPercentual(indicadorService.getMargemLucro());
        dto.roiMensal = indicadorService.formatarPercentual(indicadorService.getRoiMensal());
        dto.inadimplencia = indicadorService.formatarPercentual(indicadorService.getInadimplencia());
        dto.adesaoRHLabels = adesaoRHLabels;
        dto.adesaoRHValores = adesaoRHValores;
        dto.giroEstoque = "4,2x";
        dto.tempoEntrega = "2,3 dias";
        dto.taxaDevolucao = "1,8%";
        dto.eficienciaLogistica = "91,5%";
        return dto;
    }

    public static class DashboardMetricsDTO {
        public long totalClientes;
        public long novosClientes30Dias;
        public long totalVendas;
        public String crescimentoVendas;
        public long totalProdutos;
        public String faturamentoMensalFormatado;
        public String crescimentoFaturamento;
        public long produtosCriticos;
        public long totalFuncionarios;
        public long contratacoes12Meses;
        public long solicitacoesPendentes;
        public long solicitacoesAtrasadas;
        public long processosAdesaoTotal;
        public long processosAguardandoAprovacao;
        public String percentualMeta;
        public String ticketMedioFormatado;
        public java.util.List<String> ultimos12MesesLabels;
        public java.util.List<java.math.BigDecimal> ultimos12MesesValores;
        public java.util.List<java.math.BigDecimal> metaVendasMensal;
        public java.util.List<String> categoriasLabels;
        public java.util.List<java.math.BigDecimal> categoriasValores;
        public java.util.List<Integer> solicitacoesStatus;
        public java.util.List<Integer> performanceIndicadores;
        public String margemLucro;
        public String roiMensal;
        public String inadimplencia;
        public java.util.List<String> adesaoRHLabels;
        public java.util.List<Integer> adesaoRHValores;
        public String giroEstoque;
        public String tempoEntrega;
        public String taxaDevolucao;
        public String eficienciaLogistica;
    }
}
