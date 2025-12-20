package com.jaasielsilva.portalceo.controller.financeiro;

import com.jaasielsilva.portalceo.dto.financeiro.RelatorioDREDTO;
import com.jaasielsilva.portalceo.dto.financeiro.RelatorioFluxoCaixaDTO;
import com.jaasielsilva.portalceo.dto.financeiro.TransferenciaDTO;
import com.jaasielsilva.portalceo.model.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.portalceo.service.*;
import lombok.RequiredArgsConstructor;

import org.hibernate.Hibernate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

@Controller
@RequestMapping("/financeiro")
@RequiredArgsConstructor
@PreAuthorize("@globalControllerAdvice.podeAcessarFinanceiro()")
public class FinanceiroController {

    private final ContaReceberService contaReceberService;
    private final FluxoCaixaService fluxoCaixaService;
    private final ClienteService clienteService;
    private final FornecedorService fornecedorService;
    private final UsuarioService usuarioService;
    private final ContaBancariaService contaBancariaService;
    private final TransferenciaService transferenciaService;
    private final RelatorioFinanceiroService relatorioService;
    private final ContaPagarService contaPagarService;

    // -------------------- DASHBOARD --------------------
    @GetMapping
    public String index(Model model, @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("pageTitle", "Financeiro - Dashboard");
        model.addAttribute("moduleCSS", "financeiro");

        try {
            Map<String, Object> dashboardStats = fluxoCaixaService.getDashboardStatistics();
            model.addAttribute("dashboardStats", dashboardStats != null ? dashboardStats : Map.of());

            LocalDate hoje = LocalDate.now();

            List<FluxoCaixa> recentTransactions = List.of();
            try {
                LocalDate trintaDiasAtras = hoje.minusDays(30);
                List<FluxoCaixa> ultimas30Dias = fluxoCaixaService.findByPeriodo(trintaDiasAtras, hoje);
                if (ultimas30Dias != null) {
                    recentTransactions = ultimas30Dias.stream()
                            .sorted((a, b) -> b.getData().compareTo(a.getData()))
                            .limit(10)
                            .toList();

                    // Inicializar proxies para evitar LazyInitializationException
                    recentTransactions.forEach(t -> {
                        if (t.getContaPagar() != null) {
                            Hibernate.initialize(t.getContaPagar());
                            if (t.getContaPagar().getFornecedor() != null) {
                                Hibernate.initialize(t.getContaPagar().getFornecedor());
                            }
                            if (t.getContaPagar().getUsuarioCriacao() != null) {
                                Hibernate.initialize(t.getContaPagar().getUsuarioCriacao());
                            }
                        }
                        if (t.getContaReceber() != null) {
                            Hibernate.initialize(t.getContaReceber());
                            if (t.getContaReceber().getCliente() != null) {
                                Hibernate.initialize(t.getContaReceber().getCliente());
                            }
                            if (t.getContaReceber().getUsuarioCriacao() != null) {
                                Hibernate.initialize(t.getContaReceber().getUsuarioCriacao());
                            }
                        }
                        if (t.getContaBancaria() != null) Hibernate.initialize(t.getContaBancaria());
                        if (t.getTransferencia() != null) Hibernate.initialize(t.getTransferencia());
                    });
                }
                model.addAttribute("recentTransactions", recentTransactions);
                model.addAttribute("totalRecentTransactions", recentTransactions.size());
            } catch (Exception e) {
                System.err.println("Erro ao buscar transações recentes: " + e.getMessage());
                model.addAttribute("recentTransactions", List.of());
                model.addAttribute("totalRecentTransactions", 0);
            }

            List<ContaReceber> contasReceberVencidas = contaReceberService.findVencidas();
            if (contasReceberVencidas != null) {
                contasReceberVencidas = contasReceberVencidas.stream().limit(5).toList();
                // Inicializar proxies
                contasReceberVencidas.forEach(c -> {
                    if (c.getCliente() != null) Hibernate.initialize(c.getCliente());
                    if (c.getUsuarioCriacao() != null) Hibernate.initialize(c.getUsuarioCriacao());
                });
            } else {
                contasReceberVencidas = List.of();
            }
            model.addAttribute("contasReceberVencidas", contasReceberVencidas);

            BigDecimal totalReceber = contaReceberService.calcularTotalReceber();
            model.addAttribute("totalReceber", totalReceber != null ? totalReceber : BigDecimal.ZERO);

            BigDecimal totalPagar = BigDecimal.ZERO;
            try {
                totalPagar = contaPagarService.calcularValorTotalAPagar();
            } catch (Exception e) {
                System.err.println("Erro ao calcular total a pagar: " + e.getMessage());
            }
            model.addAttribute("totalPagar", totalPagar != null ? totalPagar : BigDecimal.ZERO);

            BigDecimal saldoAtual = fluxoCaixaService.calcularSaldoAtual();
            model.addAttribute("saldoAtual", saldoAtual != null ? saldoAtual : BigDecimal.ZERO);

            Map<String, BigDecimal> projecao30dias = fluxoCaixaService.getProjecaoFinanceira(30);
            model.addAttribute("projecao30dias", projecao30dias != null ? projecao30dias : Map.of());

        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard financeiro: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("recentTransactions", List.of());
            model.addAttribute("totalRecentTransactions", 0);
            model.addAttribute("contasReceberVencidas", List.of());
            model.addAttribute("totalReceber", BigDecimal.ZERO);
            model.addAttribute("saldoAtual", BigDecimal.ZERO);
            model.addAttribute("projecao30dias", Map.of());
        }

        return "financeiro/index";
    }

    // -------------------- CONTAS A RECEBER --------------------
    @GetMapping("/contas-receber")
    public String contasReceber(Model model, @RequestParam(required = false) String status) {
        try {
            model.addAttribute("pageTitle", "Contas a Receber");
            model.addAttribute("moduleCSS", "financeiro");

            List<ContaReceber> contas;
            try {
                contas = (status != null && !status.isEmpty())
                        ? contaReceberService.findByStatus(ContaReceber.StatusContaReceber.valueOf(status))
                        : contaReceberService.findAll();
                if (contas == null)
                    contas = List.of();
            } catch (Exception e) {
                System.err.println("Erro ao carregar contas: " + e.getMessage());
                contas = List.of();
            }

            BigDecimal totalReceber = BigDecimal.ZERO;
            try {
                totalReceber = contaReceberService.calcularTotalReceber();
            } catch (Exception e) {
                System.err.println("Erro ao calcular total a receber: " + e.getMessage());
            }

            Map<String, Long> estatisticasString;
            try {
                Map<ContaReceber.StatusContaReceber, Long> estatisticasEnum = contaReceberService
                        .getEstatisticasPorStatus();
                estatisticasString = estatisticasEnum.entrySet()
                        .stream()
                        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
            } catch (Exception e) {
                System.err.println("Erro ao calcular estatísticas: " + e.getMessage());
                estatisticasString = new HashMap<>();
            }

            long totalVencidas = contas.stream()
                    .filter(c -> c.getStatus() == ContaReceber.StatusContaReceber.VENCIDA)
                    .count();
            long totalInadimplentes = contas.stream()
                    .filter(c -> c.getStatus() == ContaReceber.StatusContaReceber.INADIMPLENTE)
                    .count();
            double percentualInadimplencia = contas.isEmpty() ? 0 : (totalInadimplentes * 100.0) / contas.size();
            String inadimplenciaFormatada = String.format("%.1f", percentualInadimplencia);

            model.addAttribute("contas", contas);
            model.addAttribute("totalReceber", totalReceber);
            model.addAttribute("estatisticas", estatisticasString);
            model.addAttribute("totalVencidas", totalVencidas);
            model.addAttribute("inadimplencia", inadimplenciaFormatada);
            model.addAttribute("statusOptions", ContaReceber.StatusContaReceber.values());
            model.addAttribute("categoriaOptions", ContaReceber.CategoriaContaReceber.values());

        } catch (Exception e) {
            System.err.println("Erro geral no módulo Contas a Receber: " + e.getMessage());
            model.addAttribute("contas", List.of());
            model.addAttribute("totalReceber", BigDecimal.ZERO);
            model.addAttribute("estatisticas", Map.of());
            model.addAttribute("totalVencidas", 0L);
            model.addAttribute("inadimplencia", "0.0");
        }

        return "financeiro/contas-receber/lista";
    }

    // ================= NOVA CONTA A RECEBER =================
    @GetMapping({"/contas-receber/novo", "/contas-receber/nova"})
    public String novoForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            ContaReceber contaReceber = new ContaReceber();
            contaReceber.setCliente(new Cliente()); // Inicializa cliente para evitar erro no Thymeleaf
            
            model.addAttribute("contaReceber", contaReceber);
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("categorias", ContaReceber.CategoriaContaReceber.values());
            model.addAttribute("statusOptions", ContaReceber.StatusContaReceber.values());
            model.addAttribute("pageTitle", "Nova Conta a Receber");
            model.addAttribute("moduleCSS", "financeiro");
            return "financeiro/contas-receber/conta-receber-form";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao carregar formulário: " + e.getMessage());
            return "redirect:/financeiro/contas-receber";
        }
    }

    @PostMapping("/contas-receber/salvar")
    public String salvarContaReceber(
            @Valid @ModelAttribute("contaReceber") ContaReceber contaReceber,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            @RequestAttribute("usuarioLogado") Usuario usuario) {

        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("categorias", ContaReceber.CategoriaContaReceber.values());
            model.addAttribute("statusOptions", ContaReceber.StatusContaReceber.values());
            model.addAttribute("moduleCSS", "financeiro");
            return "financeiro/contas-receber/conta-receber-form";
        }

        try {
            contaReceberService.save(contaReceber, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Conta a receber salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
        }

        return "redirect:/financeiro/contas-receber";
    }

    @GetMapping("/contas-receber/{id}")
    public String detalhesContaReceber(@PathVariable Long id, Model model) {
        try {
            ContaReceber conta = contaReceberService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

            model.addAttribute("conta", conta);
            model.addAttribute("pageTitle", "Detalhes da Conta a Receber");
            model.addAttribute("moduleCSS", "financeiro");

        } catch (Exception e) {
            System.err.println("Erro ao buscar detalhes da conta: " + e.getMessage());
            model.addAttribute("errorMessage", "Conta não encontrada ou erro ao carregar detalhes.");
            return "financeiro/contas-receber/lista";
        }

        return "financeiro/contas-receber/detalhes";
    }

    // -------------------- FLUXO DE CAIXA --------------------
    @GetMapping("/fluxo-caixa")
    public String fluxoCaixa(Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipoMovimento,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String status) {

        try {
            model.addAttribute("pageTitle", "Fluxo de Caixa");
            model.addAttribute("moduleCSS", "financeiro");

            if (dataInicio == null)
                dataInicio = LocalDate.now().withDayOfMonth(1);
            if (dataFim == null)
                dataFim = LocalDate.now();

            List<FluxoCaixa> transacoes = fluxoCaixaService.findByPeriodo(dataInicio, dataFim);
            model.addAttribute("transacoes", transacoes);
            model.addAttribute("tipoMovimentoOptions", FluxoCaixa.TipoMovimento.values());
            model.addAttribute("categoriaOptions", FluxoCaixa.CategoriaFluxo.values());
            model.addAttribute("statusOptions", FluxoCaixa.StatusFluxo.values());
            model.addAttribute("dataInicio", dataInicio);
            model.addAttribute("dataFim", dataFim);

            Map<String, BigDecimal> resumo = fluxoCaixaService.getResumoFinanceiroPeriodo(dataInicio, dataFim);
            model.addAttribute("resumoFinanceiro", resumo != null ? resumo : Map.of());

            BigDecimal saldoAtual = fluxoCaixaService.calcularSaldoAtual();
            model.addAttribute("saldoAtual", saldoAtual != null ? saldoAtual : BigDecimal.ZERO);

            Map<String, BigDecimal> projecao = fluxoCaixaService.getProjecaoFinanceira(30);
            model.addAttribute("projecao30dias", projecao != null ? projecao : Map.of());

        } catch (Exception e) {
            System.err.println("Erro geral no módulo Fluxo de Caixa: " + e.getMessage());
        }

        return "financeiro/fluxo-caixa";
    }

    // -------------------- TRANSFERÊNCIAS --------------------
    @GetMapping("/transferencias")
    public String transferencias(Model model) {
        model.addAttribute("pageTitle", "Transferências");
        model.addAttribute("moduleCSS", "financeiro");
        model.addAttribute("contas", contaBancariaService.listarContasAtivas());
        model.addAttribute("transferenciaDTO", new TransferenciaDTO());
        return "financeiro/transferencias";
    }

    @PostMapping("/transferencias/nova")
    public String realizarTransferencia(@ModelAttribute TransferenciaDTO dto, 
                                        @ModelAttribute("usuarioLogado") Usuario usuario,
                                        RedirectAttributes redirectAttributes) {
        try {
            transferenciaService.realizarTransferencia(dto, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Transferência realizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao realizar transferência: " + e.getMessage());
        }
        return "redirect:/financeiro/transferencias";
    }

    // -------------------- RELATÓRIOS --------------------
    @GetMapping("/relatorios")
    public String relatorios(Model model,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                             @RequestParam(defaultValue = "DRE") String tipo) {
        
        model.addAttribute("pageTitle", "Relatórios Financeiros");
        model.addAttribute("moduleCSS", "financeiro");
        
        if (inicio == null) inicio = LocalDate.now().withDayOfMonth(1);
        if (fim == null) fim = LocalDate.now();
        
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("tipo", tipo);
        
        if ("DRE".equals(tipo)) {
            RelatorioDREDTO dre = relatorioService.gerarDRE(inicio, fim);
            model.addAttribute("relatorio", dre);
        } else if ("FLUXO".equals(tipo)) {
            RelatorioFluxoCaixaDTO fluxo = relatorioService.gerarFluxoCaixa(inicio, fim);
            model.addAttribute("relatorio", fluxo);
        }
        
        return "financeiro/relatorios";
    }

    // -------------------- SINCRONIZAÇÃO --------------------
    @PostMapping("/sincronizar")
    @ResponseBody
    public ResponseEntity<?> sincronizarFluxoCaixa() {
        try {
            fluxoCaixaService.sincronizarContasReceber();
            return ResponseEntity.ok("Fluxo de caixa sincronizado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro na sincronização do fluxo de caixa: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro na sincronização: " + e.getMessage());
        }
    }

    // -------------------- CONTAS A RECEBER API --------------------
    @GetMapping("/api/contas-receber")
    @ResponseBody
    public List<ContaReceber> contasReceberJson(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            try {
                ContaReceber.StatusContaReceber statusEnum = ContaReceber.StatusContaReceber
                        .valueOf(status.toUpperCase());
                return contaReceberService.findByStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return contaReceberService.findAll();
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
    public ResponseEntity<ContaReceber> criarContaReceber(@RequestBody ContaReceber conta,
            @ModelAttribute("usuarioLogado") Usuario usuario) {
        try {
            return ResponseEntity.ok(contaReceberService.save(conta, usuario));
        } catch (Exception e) {
            System.err.println("Erro ao criar conta a receber: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/api/contas-receber/{id}/receber")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> receberConta(@PathVariable Long id,
            @RequestParam BigDecimal valor,
            @RequestParam(required = false) String observacoes,
            @ModelAttribute("usuarioLogado") Usuario usuario) {
        Map<String, Object> response = new HashMap<>();
        try {
            ContaReceber conta = contaReceberService.receberConta(id, valor, observacoes, usuario);

            response.put("status", "success");
            response.put("mensagem", "Pagamento registrado com sucesso");
            response.put("conta", conta);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("mensagem", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/api/contas-receber/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<ContaReceber> cancelarConta(@PathVariable Long id,
                                                      @RequestParam String motivo,
                                                      @ModelAttribute("usuarioLogado") Usuario usuario) {
        try {
            return ResponseEntity.ok(contaReceberService.cancelarConta(id, motivo, usuario));
        } catch (Exception e) {
            System.err.println("Erro ao cancelar conta: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/api/contas-receber/{id}")
    @ResponseBody
    public ResponseEntity<Void> deletarConta(@PathVariable Long id,
                                             @ModelAttribute("usuarioLogado") Usuario usuario) {
        try {
            contaReceberService.deleteById(id, usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Erro ao deletar conta: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/contas-receber/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<ContaReceber.StatusContaReceber, Long>> estatisticasPorStatus() {
        try {
            return ResponseEntity.ok(contaReceberService.getEstatisticasPorStatus());
        } catch (Exception e) {
            System.err.println("Erro ao obter estatísticas de contas: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of());
        }
    }

    @GetMapping("/api/contas-receber/total")
    @ResponseBody
    public ResponseEntity<BigDecimal> totalReceber() {
        try {
            BigDecimal total = contaReceberService.calcularTotalReceber();
            return ResponseEntity.ok(total != null ? total : BigDecimal.ZERO);
        } catch (Exception e) {
            System.err.println("Erro ao calcular total a receber: " + e.getMessage());
            return ResponseEntity.badRequest().body(BigDecimal.ZERO);
        }
    }
}
