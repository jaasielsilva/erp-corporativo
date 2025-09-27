package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final ContaPagarService contaPagarService;
    private final ContaReceberService contaReceberService;
    private final FluxoCaixaService fluxoCaixaService;
    private final ClienteService clienteService;
    private final FornecedorService fornecedorService;
    private final UsuarioService usuarioService;

    // Página principal do Financeiro - Dashboard
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Financeiro - Dashboard");
        model.addAttribute("moduleCSS", "financeiro");
        
        // Dashboard statistics
        Map<String, Object> dashboardStats = fluxoCaixaService.getDashboardStatistics();
        model.addAttribute("dashboardStats", dashboardStats);
        
        // Recent transactions
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = YearMonth.from(hoje).atDay(1);
        List<FluxoCaixa> recentTransactions = fluxoCaixaService.findByPeriodo(inicioMes, hoje);
        model.addAttribute("recentTransactions", recentTransactions.stream().limit(10).toList());
        
        // Basic account information
        List<ContaReceber> contasReceberVencidas = contaReceberService.findVencidas();
        model.addAttribute("contasReceberVencidas", contasReceberVencidas.stream().limit(5).toList());
        
        return "financeiro/index";
    }

    // =============== CONTAS A PAGAR ===============
    
    @GetMapping("/contas-pagar")
    public String contasPagar(Model model,
                              @RequestParam(required = false) String status) {
        
        model.addAttribute("pageTitle", "Contas a Pagar");
        model.addAttribute("moduleCSS", "financeiro");
        
        // Simplified - load from repository
        model.addAttribute("statusOptions", ContaPagar.StatusContaPagar.values());
        model.addAttribute("categoriaOptions", ContaPagar.CategoriaContaPagar.values());
        
        return "financeiro/contas-pagar";
    }
    
    // =============== CONTAS A RECEBER ===============
    
    @GetMapping("/contas-receber")
    public String contasReceber(Model model,
                               @RequestParam(required = false) String status) {
        
        model.addAttribute("pageTitle", "Contas a Receber");
        model.addAttribute("moduleCSS", "financeiro");
        
        List<ContaReceber> contas;
        if (status != null && !status.isEmpty()) {
            contas = contaReceberService.findByStatus(ContaReceber.StatusContaReceber.valueOf(status));
        } else {
            contas = contaReceberService.findAll();
        }
        
        model.addAttribute("contas", contas);
        model.addAttribute("statusOptions", ContaReceber.StatusContaReceber.values());
        model.addAttribute("categoriaOptions", ContaReceber.CategoriaContaReceber.values());
        
        // Statistics
        Map<ContaReceber.StatusContaReceber, Long> estatisticas = contaReceberService.getEstatisticasPorStatus();
        model.addAttribute("estatisticas", estatisticas);
        
        BigDecimal totalReceber = contaReceberService.calcularTotalReceber();
        model.addAttribute("totalReceber", totalReceber);
        
        // Aging analysis
        Map<String, Object> analiseIdade = contaReceberService.getAnaliseIdade();
        model.addAttribute("analiseIdade", analiseIdade);
        
        return "financeiro/contas-receber";
    }

    // =============== FLUXO DE CAIXA ===============
    
    @GetMapping("/fluxo-caixa")
    public String fluxoCaixa(Model model,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                             @RequestParam(required = false) String tipoMovimento,
                             @RequestParam(required = false) String categoria,
                             @RequestParam(required = false) String status) {
        
        model.addAttribute("pageTitle", "Fluxo de Caixa");
        model.addAttribute("moduleCSS", "financeiro");
        
        // Default period if not specified
        if (dataInicio == null) dataInicio = LocalDate.now().withDayOfMonth(1);
        if (dataFim == null) dataFim = LocalDate.now();
        
        List<FluxoCaixa> transacoes;
        if (status != null && !status.isEmpty()) {
            transacoes = fluxoCaixaService.findByStatus(FluxoCaixa.StatusFluxo.valueOf(status), dataInicio, dataFim);
        } else if (tipoMovimento != null && !tipoMovimento.isEmpty()) {
            transacoes = fluxoCaixaService.findByTipoMovimento(FluxoCaixa.TipoMovimento.valueOf(tipoMovimento), dataInicio, dataFim);
        } else if (categoria != null && !categoria.isEmpty()) {
            transacoes = fluxoCaixaService.findByCategoria(FluxoCaixa.CategoriaFluxo.valueOf(categoria), dataInicio, dataFim);
        } else {
            transacoes = fluxoCaixaService.findByPeriodo(dataInicio, dataFim);
        }
        
        model.addAttribute("transacoes", transacoes);
        model.addAttribute("tipoMovimentoOptions", FluxoCaixa.TipoMovimento.values());
        model.addAttribute("categoriaOptions", FluxoCaixa.CategoriaFluxo.values());
        model.addAttribute("statusOptions", FluxoCaixa.StatusFluxo.values());
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        
        // Financial summary
        Map<String, BigDecimal> resumo = fluxoCaixaService.getResumoFinanceiroPeriodo(dataInicio, dataFim);
        model.addAttribute("resumoFinanceiro", resumo);
        
        // Current balance
        BigDecimal saldoAtual = fluxoCaixaService.calcularSaldoAtual();
        model.addAttribute("saldoAtual", saldoAtual);
        
        // 30-day projection
        Map<String, BigDecimal> projecao = fluxoCaixaService.getProjecaoFinanceira(30);
        model.addAttribute("projecao30dias", projecao);
        
        return "financeiro/fluxo-caixa";
    }

    // =============== RELATÓRIOS ===============
    
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("pageTitle", "Relatórios Financeiros");
        model.addAttribute("moduleCSS", "financeiro");
        return "financeiro/relatorios";
    }
    
    @GetMapping("/api/relatorios/entradas-categoria")
    @ResponseBody
    public ResponseEntity<Map<FluxoCaixa.CategoriaFluxo, BigDecimal>> getEntradasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        Map<FluxoCaixa.CategoriaFluxo, BigDecimal> entradas = fluxoCaixaService.getEntradasPorCategoria(inicio, fim);
        return ResponseEntity.ok(entradas);
    }
    
    @GetMapping("/api/relatorios/saidas-categoria")
    @ResponseBody
    public ResponseEntity<Map<FluxoCaixa.CategoriaFluxo, BigDecimal>> getSaidasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        Map<FluxoCaixa.CategoriaFluxo, BigDecimal> saidas = fluxoCaixaService.getSaidasPorCategoria(inicio, fim);
        return ResponseEntity.ok(saidas);
    }
    
    // =============== SINCRONIZAÇÃO ===============
    
    @PostMapping("/sincronizar")
    @ResponseBody
    public ResponseEntity<?> sincronizarFluxoCaixa() {
        try {
            fluxoCaixaService.sincronizarContasPagar();
            fluxoCaixaService.sincronizarContasReceber();
            return ResponseEntity.ok("Fluxo de caixa sincronizado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro na sincronização: " + e.getMessage());
        }
    }

    // Transferências
    @GetMapping("/transferencias")
    public String transferencias(Model model) {
        model.addAttribute("pageTitle", "Transferências");
        model.addAttribute("moduleCSS", "financeiro");
        return "financeiro/transferencias";
    }
}