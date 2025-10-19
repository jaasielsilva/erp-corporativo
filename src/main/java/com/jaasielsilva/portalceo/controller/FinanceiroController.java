package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.ContaPagarRepository;
import com.jaasielsilva.portalceo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;

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
    private final ContaPagarRepository contaPagarRepository;

    private static final int PAGE_SIZE = 10;

    // -------------------- DASHBOARD --------------------
    @GetMapping
    public String index(Model model, @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("pageTitle", "Financeiro - Dashboard");
        model.addAttribute("moduleCSS", "financeiro");

        try {
            // Estatísticas principais
            Map<String, Object> dashboardStats = fluxoCaixaService.getDashboardStatistics();
            model.addAttribute("dashboardStats", dashboardStats != null ? dashboardStats : Map.of());

            LocalDate hoje = LocalDate.now();

            // Transações recentes dos últimos 30 dias
            List<FluxoCaixa> recentTransactions = List.of();
            try {
                LocalDate trintaDiasAtras = hoje.minusDays(30);
                List<FluxoCaixa> ultimas30Dias = fluxoCaixaService.findByPeriodo(trintaDiasAtras, hoje);
                if (ultimas30Dias != null) {
                    // Ordenar por data decrescente e limitar a 10
                    recentTransactions = ultimas30Dias.stream()
                            .sorted(Comparator.comparing(FluxoCaixa::getData).reversed())
                            .limit(10)
                            .toList();
                }
                model.addAttribute("recentTransactions", recentTransactions);
                model.addAttribute("totalRecentTransactions", recentTransactions.size());
            } catch (Exception e) {
                System.err.println("Erro ao buscar transações recentes dos últimos 30 dias: " + e.getMessage());
                model.addAttribute("recentTransactions", List.of());
                model.addAttribute("totalRecentTransactions", 0);
            }

            // Contas a receber vencidas
            List<ContaReceber> contasReceberVencidas = contaReceberService.findVencidas();
            if (contasReceberVencidas != null)
                contasReceberVencidas = contasReceberVencidas.stream().limit(5).toList();
            else
                contasReceberVencidas = List.of();
            model.addAttribute("contasReceberVencidas", contasReceberVencidas);

            // Totais
            BigDecimal totalReceber = contaReceberService.calcularTotalReceber();
            model.addAttribute("totalReceber", totalReceber != null ? totalReceber : BigDecimal.ZERO);

            BigDecimal saldoAtual = fluxoCaixaService.calcularSaldoAtual();
            model.addAttribute("saldoAtual", saldoAtual != null ? saldoAtual : BigDecimal.ZERO);

            Map<String, BigDecimal> projecao30dias = fluxoCaixaService.getProjecaoFinanceira(30);
            model.addAttribute("projecao30dias", projecao30dias != null ? projecao30dias : Map.of());

            // Paginação segura
            Page<ContaReceber> contasPage = contaReceberService.findAllPaged(page, PAGE_SIZE);
            int totalPages = (contasPage != null) ? contasPage.getTotalPages() : 0;

            if (page >= totalPages && totalPages > 0) {
                page = totalPages - 1;
                contasPage = contaReceberService.findAllPaged(page, PAGE_SIZE);
            } else if (page < 0) {
                page = 0;
                contasPage = contaReceberService.findAllPaged(page, PAGE_SIZE);
            }

            model.addAttribute("contasPage", contasPage);
            model.addAttribute("contas", contasPage != null ? contasPage.getContent() : List.of());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);

        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard financeiro: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("recentTransactions", List.of());
            model.addAttribute("totalRecentTransactions", 0);
            model.addAttribute("contasReceberVencidas", List.of());
            model.addAttribute("totalReceber", BigDecimal.ZERO);
            model.addAttribute("saldoAtual", BigDecimal.ZERO);
            model.addAttribute("projecao30dias", Map.of());
            model.addAttribute("contas", List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
        }

        return "financeiro/index";
    }

    // -------------------- CONTAS A PAGAR --------------------
    @GetMapping("/contas-pagar")
    public String contasPagar(Model model, @RequestParam(required = false) String status) {
        List<ContaPagar> contas;
        try {
            if (status != null && !status.isEmpty()) {
                try {
                    ContaPagar.StatusContaPagar statusEnum = ContaPagar.StatusContaPagar.valueOf(status.toUpperCase());
                    contas = contaPagarRepository.findByStatusOrderByDataVencimento(statusEnum);
                } catch (IllegalArgumentException e) {
                    contas = List.of();
                }
            } else {
                contas = Optional.ofNullable(contaPagarRepository.findAll()).orElse(List.of());
            }
            model.addAttribute("contasPagar", contas);
            model.addAttribute("pageTitle", "Contas a Pagar");
            model.addAttribute("moduleCSS", "financeiro");
            model.addAttribute("statusOptions", ContaPagar.StatusContaPagar.values());
            model.addAttribute("categoriaOptions", ContaPagar.CategoriaContaPagar.values());
        } catch (Exception e) {
            System.err.println("Erro ao carregar contas a pagar: " + e.getMessage());
            model.addAttribute("contasPagar", List.of());
        }

        return "financeiro/contas-pagar";
    }

    // -------------------- CONTAS A RECEBER --------------------
    @GetMapping("/contas-receber")
    public String contasReceber(Model model, @RequestParam(required = false) String status) {
        try {
            model.addAttribute("pageTitle", "Contas a Receber");
            model.addAttribute("moduleCSS", "financeiro");

            // Buscar contas
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

            // Total a receber
            BigDecimal totalReceber;
            try {
                totalReceber = contaReceberService.calcularTotalReceber();
            } catch (Exception e) {
                System.err.println("Erro ao calcular total a receber: " + e.getMessage());
                totalReceber = BigDecimal.ZERO;
            }

            // Estatísticas por status
            // Estatísticas por status
            Map<String, Long> estatisticasString;
            try {
                Map<ContaReceber.StatusContaReceber, Long> estatisticasEnum = contaReceberService
                        .getEstatisticasPorStatus();
                estatisticasString = estatisticasEnum.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().name(), // converte enum para string maiúscula
                                Map.Entry::getValue));
                if (estatisticasString == null)
                    estatisticasString = new HashMap<>();
            } catch (Exception e) {
                System.err.println("Erro ao calcular estatísticas: " + e.getMessage());
                estatisticasString = new HashMap<>();
            }

            

            // Contas vencidas
            long totalVencidas = contas.stream()
                    .filter(c -> c.getStatus() == ContaReceber.StatusContaReceber.VENCIDA)
                    .count();

            // Inadimplência (percentual de contas com status INADIMPLENTE)
            long totalInadimplentes = contas.stream()
                    .filter(c -> c.getStatus() == ContaReceber.StatusContaReceber.INADIMPLENTE)
                    .count();
            double percentualInadimplencia = contas.isEmpty() ? 0 : (totalInadimplentes * 100.0) / contas.size();
            String inadimplenciaFormatada = String.format("%.1f", percentualInadimplencia);

            // Passar atributos para a view
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

        return "financeiro/contas-receber";
    }

    // -------------------- DETALHES DA CONTA A RECEBER --------------------
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
            return "financeiro/contas-receber";
        }

        return "financeiro/detalhes-conta-receber";
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

            List<FluxoCaixa> transacoes = List.of();
            try {
                if (status != null && !status.isEmpty()) {
                    transacoes = fluxoCaixaService.findByStatus(FluxoCaixa.StatusFluxo.valueOf(status), dataInicio,
                            dataFim);
                } else if (tipoMovimento != null && !tipoMovimento.isEmpty()) {
                    transacoes = fluxoCaixaService.findByTipoMovimento(FluxoCaixa.TipoMovimento.valueOf(tipoMovimento),
                            dataInicio, dataFim);
                } else if (categoria != null && !categoria.isEmpty()) {
                    transacoes = fluxoCaixaService.findByCategoria(FluxoCaixa.CategoriaFluxo.valueOf(categoria),
                            dataInicio, dataFim);
                } else {
                    transacoes = fluxoCaixaService.findByPeriodo(dataInicio, dataFim);
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar fluxo de caixa: " + e.getMessage());
            }

            model.addAttribute("transacoes", transacoes);
            model.addAttribute("tipoMovimentoOptions", FluxoCaixa.TipoMovimento.values());
            model.addAttribute("categoriaOptions", FluxoCaixa.CategoriaFluxo.values());
            model.addAttribute("statusOptions", FluxoCaixa.StatusFluxo.values());
            model.addAttribute("dataInicio", dataInicio);
            model.addAttribute("dataFim", dataFim);

            try {
                Map<String, BigDecimal> resumo = fluxoCaixaService.getResumoFinanceiroPeriodo(dataInicio, dataFim);
                model.addAttribute("resumoFinanceiro", resumo != null ? resumo : Map.of());
            } catch (Exception e) {
                System.err.println("Erro ao gerar resumo financeiro: " + e.getMessage());
            }

            try {
                BigDecimal saldoAtual = fluxoCaixaService.calcularSaldoAtual();
                model.addAttribute("saldoAtual", saldoAtual != null ? saldoAtual : BigDecimal.ZERO);
            } catch (Exception e) {
                System.err.println("Erro ao calcular saldo atual: " + e.getMessage());
            }

            try {
                Map<String, BigDecimal> projecao = fluxoCaixaService.getProjecaoFinanceira(30);
                model.addAttribute("projecao30dias", projecao != null ? projecao : Map.of());
            } catch (Exception e) {
                System.err.println("Erro ao gerar projeção financeira: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Erro geral no módulo Fluxo de Caixa: " + e.getMessage());
        }

        return "financeiro/fluxo-caixa";
    }

    // -------------------- TRANSFERÊNCIAS --------------------
    @GetMapping("/transferencias")
    public String transferencias(Model model) {
        try {
            model.addAttribute("pageTitle", "Transferências");
            model.addAttribute("moduleCSS", "financeiro");
        } catch (Exception e) {
            System.err.println("Erro ao carregar transferências: " + e.getMessage());
        }
        return "financeiro/transferencias";
    }

    // -------------------- RELATÓRIOS --------------------
    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        try {
            model.addAttribute("pageTitle", "Relatórios Financeiros");
            model.addAttribute("moduleCSS", "financeiro");
        } catch (Exception e) {
            System.err.println("Erro ao carregar relatórios: " + e.getMessage());
        }
        return "financeiro/relatorios";
    }

    @GetMapping("/api/relatorios/entradas-categoria")
    @ResponseBody
    public ResponseEntity<Map<FluxoCaixa.CategoriaFluxo, BigDecimal>> getEntradasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            return ResponseEntity.ok(fluxoCaixaService.getEntradasPorCategoria(inicio, fim));
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de entradas: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of());
        }
    }

    @GetMapping("/api/relatorios/saidas-categoria")
    @ResponseBody
    public ResponseEntity<Map<FluxoCaixa.CategoriaFluxo, BigDecimal>> getSaidasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            return ResponseEntity.ok(fluxoCaixaService.getSaidasPorCategoria(inicio, fim));
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de saídas: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of());
        }
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
            System.err.println("Erro na sincronização do fluxo de caixa: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro na sincronização: " + e.getMessage());
        }
    }

    // -------------------- CONTAS A RECEBER API --------------------
    @GetMapping("/api/contas-receber")
    @ResponseBody
    public List<ContaReceber> contasReceberJson(@RequestParam(required = false) String status) {
        try {
            if (status != null && !status.isEmpty()) {
                try {
                    ContaReceber.StatusContaReceber statusEnum = ContaReceber.StatusContaReceber
                            .valueOf(status.toUpperCase());
                    return contaReceberService.findByStatus(statusEnum);
                } catch (IllegalArgumentException e) {
                    return List.of();
                }
            } else {
                return contaReceberService.findAll();
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar contas a receber via API: " + e.getMessage());
            return List.of();
        }
    }

    @GetMapping("/api/contas-receber/{id}")
    @ResponseBody
    public ResponseEntity<ContaReceber> getContaReceberPorId(@PathVariable Long id) {
        try {
            return contaReceberService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Erro ao buscar conta a receber por ID: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/contas-receber")
    @ResponseBody
    public ResponseEntity<ContaReceber> criarContaReceber(@RequestBody ContaReceber conta) {
        try {
            ContaReceber salva = contaReceberService.save(conta);
            return ResponseEntity.ok(salva);
        } catch (Exception e) {
            System.err.println("Erro ao criar conta a receber: " + e.getMessage());
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
            Usuario usuario = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            ContaReceber recebida = contaReceberService.receberConta(id, valor, observacoes, usuario);
            return ResponseEntity.ok(recebida);
        } catch (Exception e) {
            System.err.println("Erro ao receber conta: " + e.getMessage());
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
            Usuario usuario = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            ContaReceber cancelada = contaReceberService.cancelarConta(id, motivo, usuario);
            return ResponseEntity.ok(cancelada);
        } catch (Exception e) {
            System.err.println("Erro ao cancelar conta: " + e.getMessage());
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

    @GetMapping("/api/contas-receber/aging")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analiseAging() {
        try {
            return ResponseEntity.ok(contaReceberService.getAnaliseIdade());
        } catch (Exception e) {
            System.err.println("Erro ao gerar análise aging: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of());
        }
    }

    @PutMapping("/api/contas-receber/{id}/registrar-pagamento")
    @ResponseBody
    public ResponseEntity<?> registrarPagamento(
            @PathVariable Long id,
            @RequestParam BigDecimal valorPago,
            @RequestParam(required = false) Long usuarioId) {
        try {
            if (usuarioId == null)
                usuarioId = 1L;
            Usuario usuario = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            ContaReceber conta = contaReceberService.receberConta(id, valorPago, "Pagamento registrado via sistema",
                    usuario);

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Pagamento registrado com sucesso!",
                    "contaId", conta.getId(),
                    "valorRecebido", conta.getValorRecebido(),
                    "status", conta.getStatus().name()));
        } catch (Exception e) {
            System.err.println("Erro ao registrar pagamento: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Erro ao registrar pagamento: " + e.getMessage()));
        }
    }
}
