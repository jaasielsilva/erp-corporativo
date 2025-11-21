package com.jaasielsilva.portalceo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.EstoqueService;
import com.jaasielsilva.portalceo.service.IndicadorService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.SolicitacaoAcessoService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;

@Controller
public class DashboardController {

    @Autowired
    UsuarioService usuarioService;

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
    private com.jaasielsilva.portalceo.service.ContaReceberService contaReceberService;

    @Autowired
    private org.springframework.core.env.Environment environment;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        // Buscar o usuário logado
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);

        // Verificar se é ADMIN
        boolean isAdmin = usuarioLogado != null && usuarioLogado.getPerfis().stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

        // ===== DADOS PRINCIPAIS =====
        long totalClientes = clienteService.contarTotal();
        long novosClientes30Dias = clienteService.contarNovosPorPeriodo(30);
        java.time.YearMonth agoraYm = java.time.YearMonth.now();
        java.time.LocalDate inicio12 = agoraYm.minusMonths(11).atDay(1);
        java.time.LocalDate fim12 = agoraYm.atEndOfMonth();
        long totalVendas = 0;
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

        String percentualMeta = "87%";

        // GRÁFICOS DE VENDAS
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

        // Vendas por categoria
        List<String> categoriasLabels = new ArrayList<>();
        List<BigDecimal> categoriasValores = new ArrayList<>();
        java.util.Map<com.jaasielsilva.portalceo.model.ContaReceber.CategoriaContaReceber, BigDecimal> porCategoria =
                contaReceberService.getRecebimentosPorCategoria(inicio12, fim12);
        for (var e : porCategoria.entrySet()) {
            categoriasLabels.add(e.getKey() != null ? e.getKey().name() : "—");
            categoriasValores.add(e.getValue() != null ? e.getValue() : BigDecimal.ZERO);
        }

        List<Long> solicitacoesStatusLong = solicitacaoAcessoService.obterValoresGraficoStatus();
        List<Integer> solicitacoesStatus = new ArrayList<>();
        for (Long valor : solicitacoesStatusLong) {
            solicitacoesStatus.add(valor.intValue());
        }

        List<Integer> performanceIndicadores = Arrays.asList(
                75,
                solicitacaoAcessoService.calcularPerformanceAtendimento(),
                estoqueService.calcularPerformanceLogistica(),
                clienteService.calcularPerformanceQualidade(),
                usuarioService.calcularPerformanceFinanceiro());

        // ===== MÉTRICAS FINANCEIRAS =====
        String margemLucro = indicadorService.formatarPercentual(indicadorService.getMargemLucro());
        String roiMensal = indicadorService.formatarPercentual(indicadorService.getRoiMensal());
        String inadimplencia = indicadorService.formatarPercentual(indicadorService.getInadimplencia());

        // ===== MÉTRICAS DE RH =====
        String taxaRetencao = "94,2%";
        String produtividadeMedia = "87,3%";
        String horasExtras = "234h";
        String satisfacaoInterna = "8,7/10";

        // ===== MÉTRICAS OPERACIONAIS =====
        String giroEstoque = "4,2x";
        String tempoEntrega = "2,3 dias";
        String taxaDevolucao = "1,8%";
        String eficienciaLogistica = "91,5%";

        // ===== TICKET MÉDIO FORMATADO =====
        String ticketMedioFormatado = indicadorService.formatarMoeda(indicadorService.getTicketMedio());

        // ===== ADICIONANDO ATRIBUTOS AO MODEL =====
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("faturamentoMensal", indicadorService.formatarMoeda(faturamentoMensal));
        model.addAttribute("crescimentoFaturamento", crescimentoFaturamento);
        model.addAttribute("totalVendas", String.format("%,d", totalVendas));
        model.addAttribute("crescimentoVendas", crescimentoVendas);
        model.addAttribute("totalClientes", String.format("%,d", totalClientes));
        model.addAttribute("novosClientes30Dias", novosClientes30Dias);
        model.addAttribute("totalProdutos", String.format("%,d", produtosEstoque));
        model.addAttribute("totalFuncionarios", String.format("%,d", totalFuncionarios));
        model.addAttribute("contratacoes12Meses", contratacoes12Meses);
        model.addAttribute("solicitacoesPendentes", String.format("%,d", solicitacoesPendentes));
        model.addAttribute("produtosCriticos", String.format("%,d", produtosCriticos));
        model.addAttribute("solicitacoesAtrasadas", String.format("%,d", solicitacoesAtrasadas));
        model.addAttribute("processosAdesaoTotal", processosAdesaoTotal);
        model.addAttribute("processosAguardandoAprovacao", processosAguardandoAprovacao);
        model.addAttribute("percentualMeta", percentualMeta);
        model.addAttribute("ticketMedio", ticketMedioFormatado);
        
        model.addAttribute("ultimos12MesesLabels", ultimos12MesesLabels);
        model.addAttribute("ultimos12MesesValores", ultimos12MesesValores);
        model.addAttribute("metaVendasMensal", metaVendasMensal);
        model.addAttribute("categoriasLabels", categoriasLabels);
        model.addAttribute("categoriasValores", categoriasValores);
        model.addAttribute("solicitacoesStatus", solicitacoesStatus);
        model.addAttribute("performanceIndicadores", performanceIndicadores);
        model.addAttribute("adesaoRHLabels", adesaoRHLabels);
        model.addAttribute("adesaoRHValores", adesaoRHValores);

        model.addAttribute("margemLucro", margemLucro);
        model.addAttribute("roiMensal", roiMensal);
        model.addAttribute("inadimplencia", inadimplencia);
        model.addAttribute("taxaRetencao", taxaRetencao);
        model.addAttribute("produtividadeMedia", produtividadeMedia);
        model.addAttribute("horasExtras", horasExtras);
        model.addAttribute("satisfacaoInterna", satisfacaoInterna);
        model.addAttribute("giroEstoque", giroEstoque);
        model.addAttribute("tempoEntrega", tempoEntrega);
        model.addAttribute("taxaDevolucao", taxaDevolucao);
        model.addAttribute("eficienciaLogistica", eficienciaLogistica);

        System.out.println("Margem Lucro: " + margemLucro);
        System.out.println("ROI Mensal: " + roiMensal);
        System.out.println("Inadimplência: " + inadimplencia);
        System.out.println("Ticket Médio: " + ticketMedioFormatado);

        return "dashboard/index";
    }

    // Método utilitário para formatação monetária
    private String formatarMoeda(BigDecimal valor) {
        if (valor == null)
            return "R$ 0,00";
        return "R$ " + String.format("%,.2f", valor).replace(".", ",");
    }

    @GetMapping("/dashboard/**")
    public String dashboardCatchAll() {
        return "error/404";
    }

    @GetMapping("/dashboard/notificacoes")
    public String dashboardNotificacoes(Model model) {
        return "dashboard/notificacoes";
    }

    @GetMapping("/dashboard/estatisticas")
    public String dashboardEstatisticas(Model model) {
        return "dashboard/estatisticas";
    }

    @GetMapping("/dashboard/alertas")
    public String dashboardAlertas(Model model) {
        return "dashboard/alertas";
    }

    @GetMapping("/dashboard/api/metrics")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> apiMetrics(Principal principal) {
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        long totalClientes = clienteService.contarTotal();
        long novosClientes30Dias = clienteService.contarNovosPorPeriodo(30);
        long totalVendas = 0;
        String crescimentoVendas = "0%";
        long produtosEstoque = produtoService.somarQuantidadeEstoque();
        java.math.BigDecimal faturamentoMensal = indicadorService.getRoiMensal();
        String crescimentoFaturamento = "0%";
        long produtosCriticos = produtoService.contarProdutosCriticos();
        long totalFuncionarios = colaboradorService.contarAtivos();
        long contratacoes12Meses = colaboradorService.contarContratacaosPorPeriodo(12);
        long solicitacoesPendentes = solicitacaoAcessoService.contarSolicitacoesPendentes();
        long solicitacoesAtrasadas = solicitacaoAcessoService.contarSolicitacoesAtrasadas();
        WorkflowAdesaoService.DashboardEstatisticas estatisticasAdesao = workflowAdesaoService.obterEstatisticas();
        long processosAdesaoTotal = estatisticasAdesao.getProcessosPorStatus().values().stream().mapToLong(Long::longValue).sum();
        long processosAguardandoAprovacao = estatisticasAdesao.getProcessosAguardandoAprovacao();
        java.util.List<String> adesaoRHLabels = workflowAdesaoService.obterLabelsUltimos6Meses();
        java.util.List<Integer> adesaoRHValores = workflowAdesaoService.obterDadosAdesaoUltimos6Meses();
        java.util.List<Long> solicitacoesStatusLong = solicitacaoAcessoService.obterValoresGraficoStatus();
        java.util.List<Integer> solicitacoesStatus = new java.util.ArrayList<>();
        for (Long v : solicitacoesStatusLong) solicitacoesStatus.add(v.intValue());
        java.time.YearMonth agoraYmApi = java.time.YearMonth.now();
        java.time.LocalDate inicio12Api = agoraYmApi.minusMonths(11).atDay(1);
        java.time.LocalDate fim12Api = agoraYmApi.atEndOfMonth();
        java.util.Map<java.time.YearMonth, java.math.BigDecimal> vendas12 = contaReceberService.getRecebimentosPorMes(inicio12Api, fim12Api);
        java.util.List<String> ultimos12MesesLabels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> ultimos12MesesValores = new java.util.ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            java.time.YearMonth ym = agoraYmApi.minusMonths(i);
            java.math.BigDecimal valor = vendas12.getOrDefault(ym, java.math.BigDecimal.ZERO);
            String label = ym.getMonth().name().substring(0, 3) + "/" + String.valueOf(ym.getYear()).substring(2);
            ultimos12MesesLabels.add(label);
            ultimos12MesesValores.add(valor);
        }
        java.util.List<java.math.BigDecimal> metaVendasMensal = new java.util.ArrayList<>();
        java.math.BigDecimal metaBase = faturamentoMensal.multiply(new java.math.BigDecimal("1.2"));
        for (int i = 0; i < 12; i++) metaVendasMensal.add(metaBase);
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        java.util.Map<String, Object> totais = new java.util.HashMap<>();
        totais.put("usuario", usuarioLogado != null ? usuarioLogado.getEmail() : null);
        totais.put("totalClientes", totalClientes);
        totais.put("novosClientes30Dias", novosClientes30Dias);
        totais.put("totalVendas", totalVendas);
        totais.put("crescimentoVendas", crescimentoVendas);
        totais.put("totalProdutos", produtosEstoque);
        totais.put("produtosCriticos", produtosCriticos);
        totais.put("totalFuncionarios", totalFuncionarios);
        totais.put("contratacoes12Meses", contratacoes12Meses);
        totais.put("solicitacoesPendentes", solicitacoesPendentes);
        totais.put("solicitacoesAtrasadas", solicitacoesAtrasadas);
        totais.put("processosAdesaoTotal", processosAdesaoTotal);
        totais.put("processosAguardandoAprovacao", processosAguardandoAprovacao);
        totais.put("faturamentoMensal", indicadorService.formatarMoeda(faturamentoMensal));
        totais.put("crescimentoFaturamento", crescimentoFaturamento);
        totais.put("margemLucro", indicadorService.formatarPercentual(indicadorService.getMargemLucro()));
        totais.put("roiMensal", indicadorService.formatarPercentual(indicadorService.getRoiMensal()));
        totais.put("inadimplencia", indicadorService.formatarPercentual(indicadorService.getInadimplencia()));
        totais.put("ticketMedio", indicadorService.formatarMoeda(indicadorService.getTicketMedio()));
        totais.put("taxaRetencao", "94,2%");
        totais.put("produtividadeMedia", "87,3%");
        totais.put("horasExtras", "234h");
        totais.put("satisfacaoInterna", "8,7/10");
        totais.put("giroEstoque", "4,2x");
        totais.put("tempoEntrega", "2,3 dias");
        totais.put("taxaDevolucao", "1,8%");
        totais.put("eficienciaLogistica", "91,5%");
        java.util.Map<String, Object> graficos = new java.util.HashMap<>();
        java.util.Map<String, Object> vendas = new java.util.HashMap<>();
        vendas.put("labels", ultimos12MesesLabels);
        vendas.put("valores", ultimos12MesesValores);
        vendas.put("meta", metaVendasMensal);
        java.util.Map<String, Object> categorias = new java.util.HashMap<>();
        java.util.List<String> catLabels = new java.util.ArrayList<>();
        java.util.List<java.math.BigDecimal> catValores = new java.util.ArrayList<>();
        java.util.Map<com.jaasielsilva.portalceo.model.ContaReceber.CategoriaContaReceber, java.math.BigDecimal> porCat =
                contaReceberService.getRecebimentosPorCategoria(inicio12Api, fim12Api);
        for (var e : porCat.entrySet()) {
            catLabels.add(e.getKey() != null ? e.getKey().name() : "—");
            catValores.add(e.getValue() != null ? e.getValue() : java.math.BigDecimal.ZERO);
        }
        categorias.put("labels", catLabels);
        categorias.put("valores", catValores);
        java.util.Map<String, Object> solicitacoes = new java.util.HashMap<>();
        solicitacoes.put("valores", solicitacoesStatus);
        java.util.Map<String, Object> adesao = new java.util.HashMap<>();
        adesao.put("labels", adesaoRHLabels);
        adesao.put("valores", adesaoRHValores);
        graficos.put("vendas", vendas);
        graficos.put("categorias", categorias);
        graficos.put("solicitacoes", solicitacoes);
        graficos.put("adesao", adesao);
        graficos.put("performance", java.util.Arrays.asList(
                75,
                solicitacaoAcessoService.calcularPerformanceAtendimento(),
                estoqueService.calcularPerformanceLogistica(),
                clienteService.calcularPerformanceQualidade(),
                usuarioService.calcularPerformanceFinanceiro()
        ));
        payload.put("totais", totais);
        payload.put("graficos", graficos);
        return org.springframework.http.ResponseEntity.ok(payload);
    }

    @GetMapping("/dashboard/api/system")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> apiSystem() {
        Runtime rt = Runtime.getRuntime();
        java.lang.management.RuntimeMXBean mx = java.lang.management.ManagementFactory.getRuntimeMXBean();
        String version = DashboardController.class.getPackage().getImplementationVersion();
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        java.util.Map<String, Object> app = new java.util.HashMap<>();
        app.put("name", "ERP Corporativo");
        app.put("version", version);
        payload.put("app", app);
        java.util.Map<String, Object> os = new java.util.HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("arch", System.getProperty("os.arch"));
        os.put("version", System.getProperty("os.version"));
        payload.put("os", os);
        payload.put("java", System.getProperty("java.version"));
        payload.put("activeProfiles", java.util.Arrays.asList(environment.getActiveProfiles()));
        java.util.Map<String, Object> mem = new java.util.HashMap<>();
        mem.put("max", rt.maxMemory());
        mem.put("total", rt.totalMemory());
        mem.put("free", rt.freeMemory());
        mem.put("used", rt.totalMemory() - rt.freeMemory());
        payload.put("memory", mem);
        payload.put("uptimeMillis", mx.getUptime());
        payload.put("startTimeMillis", mx.getStartTime());
        return org.springframework.http.ResponseEntity.ok(payload);
    }

}
