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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.EstoqueService;
import com.jaasielsilva.portalceo.service.IndicadorService;
import com.jaasielsilva.portalceo.service.ProdutoService;
import com.jaasielsilva.portalceo.service.SolicitacaoAcessoService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;

import com.jaasielsilva.portalceo.service.DashboardMetricsService;
import com.jaasielsilva.portalceo.service.DashboardMetricsService.DashboardMetricsDTO;

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

    @Autowired
    private DashboardMetricsService dashboardMetricsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_EXECUTIVO_VISUALIZAR','DASHBOARD_OPERACIONAL_VISUALIZAR','DASHBOARD_FINANCEIRO_VISUALIZAR')")
    public String dashboard(Model model, Principal principal) {

        // Buscar o usuário logado
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);

        // Verificar se é ADMIN
        boolean isAdmin = usuarioLogado != null && usuarioLogado.getPerfis().stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

        // Verificar permissões
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean podeVisualizarExecutivo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("DASHBOARD_EXECUTIVO_VISUALIZAR") || a.getAuthority().equals("ROLE_ADMIN"));

        boolean podeVisualizarFinanceiro = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("DASHBOARD_FINANCEIRO_VISUALIZAR") || a.getAuthority().equals("ROLE_FINANCEIRO_READ"))
                || podeVisualizarExecutivo;

        boolean podeVisualizarOperacional = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("DASHBOARD_OPERACIONAL_VISUALIZAR"))
                || podeVisualizarExecutivo;

        boolean podeVisualizarRH = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RH_READ") || a.getAuthority().equals("MENU_RH"))
                || podeVisualizarExecutivo;

        boolean podeVisualizarJuridico = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MENU_JURIDICO_DASHBOARD") || a.getAuthority().equals("ROLE_JURIDICO_GERENTE"))
                || podeVisualizarExecutivo;

        // ===== DADOS PRINCIPAIS =====
        DashboardMetricsDTO dto = dashboardMetricsService.getMetrics();

        // ===== MÉTRICAS FINANCEIRAS =====
        String margemLucro = dto.margemLucro;
        String roiMensal = dto.roiMensal;
        String inadimplencia = dto.inadimplencia;

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
        String ticketMedioFormatado = dto.ticketMedioFormatado;

        // ===== ADICIONANDO ATRIBUTOS AO MODEL =====
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("podeVisualizarExecutivo", podeVisualizarExecutivo);
        model.addAttribute("podeVisualizarFinanceiro", podeVisualizarFinanceiro);
        model.addAttribute("podeVisualizarOperacional", podeVisualizarOperacional);
        model.addAttribute("podeVisualizarRH", podeVisualizarRH);
        model.addAttribute("podeVisualizarJuridico", podeVisualizarJuridico);

        model.addAttribute("faturamentoMensal", dto.faturamentoMensalFormatado);
        model.addAttribute("crescimentoFaturamento", dto.crescimentoFaturamento);
        model.addAttribute("totalVendas", String.format("%,d", dto.totalVendas));
        model.addAttribute("crescimentoVendas", dto.crescimentoVendas);
        model.addAttribute("totalClientes", String.format("%,d", dto.totalClientes));
        model.addAttribute("novosClientes30Dias", dto.novosClientes30Dias);
        model.addAttribute("totalProdutos", String.format("%,d", dto.totalProdutos));
        model.addAttribute("totalFuncionarios", String.format("%,d", dto.totalFuncionarios));
        model.addAttribute("contratacoes12Meses", dto.contratacoes12Meses);
        model.addAttribute("solicitacoesPendentes", String.format("%,d", dto.solicitacoesPendentes));
        model.addAttribute("produtosCriticos", String.format("%,d", dto.produtosCriticos));
        model.addAttribute("solicitacoesAtrasadas", String.format("%,d", dto.solicitacoesAtrasadas));
        model.addAttribute("processosAdesaoTotal", dto.processosAdesaoTotal);
        model.addAttribute("processosAguardandoAprovacao", dto.processosAguardandoAprovacao);
        model.addAttribute("percentualMeta", dto.percentualMeta);
        model.addAttribute("ticketMedio", ticketMedioFormatado);
        
        model.addAttribute("ultimos12MesesLabels", dto.ultimos12MesesLabels);
        model.addAttribute("ultimos12MesesValores", dto.ultimos12MesesValores);
        model.addAttribute("metaVendasMensal", dto.metaVendasMensal);
        model.addAttribute("categoriasLabels", dto.categoriasLabels);
        model.addAttribute("categoriasValores", dto.categoriasValores);
        model.addAttribute("solicitacoesStatus", dto.solicitacoesStatus);
        model.addAttribute("performanceIndicadores", dto.performanceIndicadores);
        model.addAttribute("adesaoRHLabels", dto.adesaoRHLabels);
        model.addAttribute("adesaoRHValores", dto.adesaoRHValores);

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
    @PreAuthorize("isAuthenticated()")
    public String dashboardNotificacoes(Model model) {
        return "dashboard/notificacoes";
    }

    @GetMapping("/dashboard/estatisticas")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_EXECUTIVO_VISUALIZAR','DASHBOARD_OPERACIONAL_VISUALIZAR','DASHBOARD_FINANCEIRO_VISUALIZAR')")
    public String dashboardEstatisticas(Model model) {
        return "dashboard/estatisticas";
    }

    @GetMapping("/dashboard/alertas")
    @PreAuthorize("isAuthenticated()")
    public String dashboardAlertas(Model model) {
        return "dashboard/alertas";
    }

    @GetMapping("/dashboard/api/metrics")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_EXECUTIVO_VISUALIZAR','DASHBOARD_OPERACIONAL_VISUALIZAR','DASHBOARD_FINANCEIRO_VISUALIZAR')")
    public org.springframework.http.ResponseEntity<DashboardMetricsDTO> apiMetrics(Principal principal) {
        DashboardMetricsDTO dto = dashboardMetricsService.getMetrics();
        return org.springframework.http.ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=60")
                .body(dto);
    }

    @GetMapping("/dashboard/api/system")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
