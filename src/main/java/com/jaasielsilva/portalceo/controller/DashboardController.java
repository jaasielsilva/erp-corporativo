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
        long totalVendas = 0;
        String crescimentoVendas = "0%";
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
        Map<YearMonth, BigDecimal> vendasUltimos12Meses = java.util.Collections.emptyMap();
        List<String> ultimos12MesesLabels = new ArrayList<>();
        List<BigDecimal> ultimos12MesesValores = new ArrayList<>();
        vendasUltimos12Meses.forEach((ym, valor) -> {
            String label = ym.getMonth().name().substring(0, 3) + "/" + String.valueOf(ym.getYear()).substring(2);
            ultimos12MesesLabels.add(label);
            ultimos12MesesValores.add(valor);
        });

        List<BigDecimal> metaVendasMensal = new ArrayList<>();
        BigDecimal metaBase = faturamentoMensal.multiply(new BigDecimal("1.2"));
        for (int i = 0; i < 12; i++) {
            metaVendasMensal.add(metaBase);
        }

        // Vendas por categoria
        List<String> categoriasLabels = new ArrayList<>();
        List<BigDecimal> categoriasValores = new ArrayList<>();

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

}
