package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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

    // -------------------- DASHBOARD --------------------
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Financeiro - Dashboard");
        model.addAttribute("moduleCSS", "financeiro");

        try {
            Map<String, Object> dashboardStats = fluxoCaixaService.getDashboardStatistics();
            model.addAttribute("dashboardStats", dashboardStats);

            LocalDate hoje = LocalDate.now();
            LocalDate inicioMes = YearMonth.from(hoje).atDay(1);
            List<FluxoCaixa> recentTransactions = fluxoCaixaService.findByPeriodo(inicioMes, hoje);
            model.addAttribute("recentTransactions", recentTransactions.stream().limit(10).toList());

            List<ContaReceber> contasReceberVencidas = contaReceberService.findVencidas();
            model.addAttribute("contasReceberVencidas", contasReceberVencidas.stream().limit(5).toList());

        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard financeiro: " + e.getMessage());
        }

        return "financeiro/index";
    }

    // -------------------- CONTAS A PAGAR --------------------
    @GetMapping("/contas-pagar")
    public String contasPagar(Model model, @RequestParam(required = false) String status) {
        model.addAttribute("pageTitle", "Contas a Pagar");
        model.addAttribute("moduleCSS", "financeiro");
        model.addAttribute("statusOptions", ContaPagar.StatusContaPagar.values());
        model.addAttribute("categoriaOptions", ContaPagar.CategoriaContaPagar.values());
        return "financeiro/contas-pagar";
    }

    // -------------------- CONTAS A RECEBER --------------------
    @GetMapping("/contas-receber")
    public String contasReceber(Model model, @RequestParam(required = false) String status) {
        model.addAttribute("pageTitle", "Contas a Receber");
        model.addAttribute("moduleCSS", "financeiro");

        List<ContaReceber> contas = List.of();
        Map<ContaReceber.StatusContaReceber, Long> estatisticas = Map.of();
        BigDecimal totalReceber = BigDecimal.ZERO;
        Map<String, Object> analiseIdade = Map.of();

        try {
            if (status != null && !status.isEmpty()) {
                contas = contaReceberService.findByStatus(ContaReceber.StatusContaReceber.valueOf(status));
            } else {
                contas = contaReceberService.findAll();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar contas: " + e.getMessage());
        }

        try {
            estatisticas = contaReceberService.getEstatisticasPorStatus();
        } catch (Exception e) {
            System.err.println("Erro ao calcular estatísticas: " + e.getMessage());
        }

        try {
            totalReceber = contaReceberService.calcularTotalReceber();
        } catch (Exception e) {
            System.err.println("Erro ao calcular total a receber: " + e.getMessage());
        }

        try {
            analiseIdade = contaReceberService.getAnaliseIdade();
        } catch (Exception e) {
            System.err.println("Erro ao gerar análise de idade das contas: " + e.getMessage());
        }

        model.addAttribute("contas", contas);
        model.addAttribute("statusOptions", ContaReceber.StatusContaReceber.values());
        model.addAttribute("categoriaOptions", ContaReceber.CategoriaContaReceber.values());
        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("totalReceber", totalReceber);
        model.addAttribute("analiseIdade", analiseIdade);

        return "financeiro/contas-receber";
    }

    // -------------------- FLUXO DE CAIXA --------------------
    @GetMapping("/fluxo-caixa")
    public String fluxoCaixa(Model model,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                             @RequestParam(required = false) String tipoMovimento,
                             @RequestParam(required = false) String categoria,
                             @RequestParam(required = false) String status) {

        model.addAttribute("pageTitle", "Fluxo de Caixa");
        model.addAttribute("moduleCSS", "financeiro");

        if (dataInicio == null) dataInicio = LocalDate.now().withDayOfMonth(1);
        if (dataFim == null) dataFim = LocalDate.now();

        List<FluxoCaixa> transacoes;
        try {
            if (status != null && !status.isEmpty()) {
                transacoes = fluxoCaixaService.findByStatus(FluxoCaixa.StatusFluxo.valueOf(status), dataInicio, dataFim);
            } else if (tipoMovimento != null && !tipoMovimento.isEmpty()) {
                transacoes = fluxoCaixaService.findByTipoMovimento(FluxoCaixa.TipoMovimento.valueOf(tipoMovimento), dataInicio, dataFim);
            } else if (categoria != null && !categoria.isEmpty()) {
                transacoes = fluxoCaixaService.findByCategoria(FluxoCaixa.CategoriaFluxo.valueOf(categoria), dataInicio, dataFim);
            } else {
                transacoes = fluxoCaixaService.findByPeriodo(dataInicio, dataFim);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar fluxo de caixa: " + e.getMessage());
            transacoes = List.of();
        }

        model.addAttribute("transacoes", transacoes);
        model.addAttribute("tipoMovimentoOptions", FluxoCaixa.TipoMovimento.values());
        model.addAttribute("categoriaOptions", FluxoCaixa.CategoriaFluxo.values());
        model.addAttribute("statusOptions", FluxoCaixa.StatusFluxo.values());
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);

        try {
            Map<String, BigDecimal> resumo = fluxoCaixaService.getResumoFinanceiroPeriodo(dataInicio, dataFim);
            model.addAttribute("resumoFinanceiro", resumo);
        } catch (Exception e) {
            System.err.println("Erro ao gerar resumo financeiro: " + e.getMessage());
        }

        try {
            BigDecimal saldoAtual = fluxoCaixaService.calcularSaldoAtual();
            model.addAttribute("saldoAtual", saldoAtual);
        } catch (Exception e) {
            System.err.println("Erro ao calcular saldo atual: " + e.getMessage());
        }

        try {
            Map<String, BigDecimal> projecao = fluxoCaixaService.getProjecaoFinanceira(30);
            model.addAttribute("projecao30dias", projecao);
        } catch (Exception e) {
            System.err.println("Erro ao gerar projeção financeira: " + e.getMessage());
        }

        return "financeiro/fluxo-caixa";
    }

    // -------------------- RELATÓRIOS --------------------
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
        return ResponseEntity.ok(fluxoCaixaService.getEntradasPorCategoria(inicio, fim));
    }

    @GetMapping("/api/relatorios/saidas-categoria")
    @ResponseBody
    public ResponseEntity<Map<FluxoCaixa.CategoriaFluxo, BigDecimal>> getSaidasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(fluxoCaixaService.getSaidasPorCategoria(inicio, fim));
    }

    // -------------------- SINCRONIZAÇÃO --------------------
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

    // -------------------- TRANSFERÊNCIAS --------------------
    @GetMapping("/transferencias")
    public String transferencias(Model model) {
        model.addAttribute("pageTitle", "Transferências");
        model.addAttribute("moduleCSS", "financeiro");
        return "financeiro/transferencias";
    }

    // -------------------- API CONTAS A RECEBER --------------------
    @GetMapping("/api/contas-receber")
    @ResponseBody
    public List<ContaReceber> contasReceberJson(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            try {
                ContaReceber.StatusContaReceber statusEnum = ContaReceber.StatusContaReceber.valueOf(status.toUpperCase());
                return contaReceberService.findByStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        } else {
            return contaReceberService.findAll();
        }
    }

    @GetMapping("/api/contas-receber/{id}")
    @ResponseBody
    public ResponseEntity<ContaReceber> getContaReceberPorId(@PathVariable Long id) {
        return contaReceberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/contas-receber")
    @ResponseBody
    public ResponseEntity<ContaReceber> criarContaReceber(@RequestBody ContaReceber conta) {
        try {
            ContaReceber salva = contaReceberService.save(conta);
            return ResponseEntity.ok(salva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/api/contas-receber/{id}/receber")
    @ResponseBody
    public ResponseEntity<ContaReceber> receberConta(
            @PathVariable Long id,
            @RequestParam BigDecimal valor,
            @RequestParam(required = false) String observacoes,
            @RequestParam Long usuarioId) {

        try {
            Usuario usuario = usuarioService.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            ContaReceber recebida = contaReceberService.receberConta(id, valor, observacoes, usuario);
            return ResponseEntity.ok(recebida);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/api/contas-receber/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<ContaReceber> cancelarConta(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestParam Long usuarioId) {

        try {
            Usuario usuario = usuarioService.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            ContaReceber cancelada = contaReceberService.cancelarConta(id, motivo, usuario);
            return ResponseEntity.ok(cancelada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/api/contas-receber/{id}")
    @ResponseBody
    public ResponseEntity<Void> deletarConta(@PathVariable Long id) {
        try {
            contaReceberService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/contas-receber/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<ContaReceber.StatusContaReceber, Long>> estatisticasPorStatus() {
        return ResponseEntity.ok(contaReceberService.getEstatisticasPorStatus());
    }

    @GetMapping("/api/contas-receber/total")
    @ResponseBody
    public ResponseEntity<BigDecimal> totalReceber() {
        return ResponseEntity.ok(contaReceberService.calcularTotalReceber());
    }

    @GetMapping("/api/contas-receber/aging")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analiseAging() {
        return ResponseEntity.ok(contaReceberService.getAnaliseIdade());
    }
}
