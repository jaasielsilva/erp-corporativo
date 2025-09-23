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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/vendas/devolucoes")
@RequiredArgsConstructor
public class DevolucaoController {

    private final DevolucaoService devolucaoService;
    private final VendaService vendaService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    // Dashboard de Devoluções
    @GetMapping
    public String index(Model model,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        model.addAttribute("pageTitle", "Gestão de Devoluções");
        model.addAttribute("moduleCSS", "vendas");
        
        // Default period if not specified
        if (dataInicio == null) dataInicio = LocalDate.now().withDayOfMonth(1);
        if (dataFim == null) dataFim = LocalDate.now();
        
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        
        List<Devolucao> devolucoes;
        if (status != null && !status.isEmpty()) {
            devolucoes = devolucaoService.findByStatus(Devolucao.StatusDevolucao.valueOf(status));
        } else {
            devolucoes = devolucaoService.findByPeriodo(inicio, fim);
        }
        
        model.addAttribute("devolucoes", devolucoes);
        model.addAttribute("statusOptions", Devolucao.StatusDevolucao.values());
        model.addAttribute("motivoOptions", Devolucao.MotivoDevolucao.values());
        model.addAttribute("tipoOptions", Devolucao.TipoDevolucao.values());
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        
        // Statistics
        Map<Devolucao.StatusDevolucao, Long> estatisticasStatus = devolucaoService.getEstatisticasPorStatus();
        Map<Devolucao.MotivoDevolucao, Long> estatisticasMotivo = devolucaoService.getEstatisticasPorMotivo();
        model.addAttribute("estatisticasStatus", estatisticasStatus);
        model.addAttribute("estatisticasMotivo", estatisticasMotivo);
        
        // Total value
        BigDecimal valorTotal = devolucaoService.calcularValorTotalDevolucoes(inicio, fim);
        model.addAttribute("valorTotalDevolucoes", valorTotal);
        
        // Pending returns requiring attention
        List<Devolucao> pendentesAntigas = devolucaoService.findPendentesAntigas(7);
        model.addAttribute("pendentesAntigas", pendentesAntigas);
        
        return "vendas/devolucoes/index";
    }

    // Nova Devolução - Buscar Venda
    @GetMapping("/nova")
    public String novaD​evolucao(Model model) {
        model.addAttribute("pageTitle", "Nova Devolução");
        model.addAttribute("motivoOptions", Devolucao.MotivoDevolucao.values());
        return "vendas/devolucoes/buscar-venda";
    }

    // Buscar venda para devolução
    @GetMapping("/buscar-venda")
    @ResponseBody
    public ResponseEntity<?> buscarVenda(@RequestParam String numeroVenda) {
        try {
            Optional<Venda> vendaOpt = vendaService.buscarPorNumeroVenda(numeroVenda);
            if (vendaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Venda não encontrada");
            }
            
            Venda venda = vendaOpt.get();
            
            // Check if sale is eligible for return
            LocalDateTime agora = LocalDateTime.now();
            long diasDesdeVenda = java.time.Duration.between(venda.getDataVenda(), agora).toDays();
            
            if (diasDesdeVenda > 30) {
                return ResponseEntity.badRequest().body("Prazo para devolução expirado (máximo 30 dias)");
            }
            
            // Check if there's already a pending return
            List<Devolucao> devolucoes = devolucaoService.findByVenda(venda);
            boolean temPendente = devolucoes.stream()
                .anyMatch(d -> d.isPendente() || d.isAprovada());
            
            if (temPendente) {
                return ResponseEntity.badRequest().body("Já existe uma devolução pendente para esta venda");
            }
            
            return ResponseEntity.ok(venda);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao buscar venda: " + e.getMessage());
        }
    }

    // Criar devolução da venda
    @GetMapping("/nova/{vendaId}")
    public String criarDevolucao(@PathVariable Long vendaId, Model model) {
        Optional<Venda> vendaOpt = vendaService.buscarPorId(vendaId);
        if (vendaOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Venda não encontrada");
            return "redirect:/vendas/devolucoes";
        }
        
        Venda venda = vendaOpt.get();
        
        model.addAttribute("pageTitle", "Nova Devolução - Venda #" + venda.getNumeroVenda());
        model.addAttribute("venda", venda);
        model.addAttribute("devolucao", new Devolucao());
        model.addAttribute("motivoOptions", Devolucao.MotivoDevolucao.values());
        model.addAttribute("tipoOptions", Devolucao.TipoDevolucao.values());
        model.addAttribute("condicaoOptions", DevolucaoItem.CondicaoProduto.values());
        
        return "vendas/devolucoes/form";
    }

    // Salvar nova devolução
    @PostMapping("/salvar")
    public String salvarDevolucao(@RequestParam Long vendaId,
                                  @RequestParam Devolucao.MotivoDevolucao motivo,
                                  @RequestParam(required = false) String observacoes,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            
            Devolucao devolucao = devolucaoService.criarDevolucaoDeVenda(vendaId, motivo, observacoes, usuario);
            devolucaoService.save(devolucao);
            
            redirectAttributes.addFlashAttribute("successMessage", "Devolução criada com sucesso!");
            return "redirect:/vendas/devolucoes/" + devolucao.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar devolução: " + e.getMessage());
            return "redirect:/vendas/devolucoes/nova";
        }
    }

    // Visualizar devolução
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Optional<Devolucao> devolucaoOpt = devolucaoService.findById(id);
        if (devolucaoOpt.isEmpty()) {
            return "redirect:/vendas/devolucoes";
        }
        
        Devolucao devolucao = devolucaoOpt.get();
        
        model.addAttribute("pageTitle", "Devolução #" + id);
        model.addAttribute("devolucao", devolucao);
        model.addAttribute("podeAprovar", devolucao.podeSerAprovada());
        model.addAttribute("podeProcessar", devolucao.podeSerProcessada());
        model.addAttribute("podeCancelar", devolucao.podeSerCancelada());
        
        return "vendas/devolucoes/detalhes";
    }

    // Aprovar devolução
    @PostMapping("/{id}/aprovar")
    @ResponseBody
    public ResponseEntity<?> aprovar(@PathVariable Long id,
                                    @RequestParam(required = false) String observacoes,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            devolucaoService.aprovarDevolucao(id, observacoes, usuario);
            return ResponseEntity.ok("Devolução aprovada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rejeitar devolução
    @PostMapping("/{id}/rejeitar")
    @ResponseBody
    public ResponseEntity<?> rejeitar(@PathVariable Long id,
                                     @RequestParam String motivoRejeicao,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            devolucaoService.rejeitarDevolucao(id, motivoRejeicao, usuario);
            return ResponseEntity.ok("Devolução rejeitada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Processar devolução
    @PostMapping("/{id}/processar")
    @ResponseBody
    public ResponseEntity<?> processar(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            devolucaoService.processarDevolucao(id, usuario);
            return ResponseEntity.ok("Devolução processada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Finalizar devolução
    @PostMapping("/{id}/finalizar")
    @ResponseBody
    public ResponseEntity<?> finalizar(@PathVariable Long id,
                                      @RequestParam(required = false) String observacoes) {
        try {
            devolucaoService.finalizarDevolucao(id, observacoes);
            return ResponseEntity.ok("Devolução finalizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cancelar devolução
    @PostMapping("/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelar(@PathVariable Long id,
                                     @RequestParam String motivoCancelamento,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            devolucaoService.cancelarDevolucao(id, motivoCancelamento, usuario);
            return ResponseEntity.ok("Devolução cancelada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Relatórios de devoluções
    @GetMapping("/relatorios")
    public String relatorios(Model model,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        model.addAttribute("pageTitle", "Relatórios de Devoluções");
        
        // Default period
        if (dataInicio == null) dataInicio = LocalDate.now().withDayOfMonth(1);
        if (dataFim == null) dataFim = LocalDate.now();
        
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        
        // Reports data
        List<Object[]> clientesComMaisDevolucoes = devolucaoService.getClientesComMaisDevolucoes(inicio, fim);
        List<Object[]> produtosComMaisDevolucoes = devolucaoService.getProdutosComMaisDevolucoes(inicio, fim);
        List<Object[]> devolucoesPorDia = devolucaoService.getDevolucoesPorDia(inicio, fim);
        
        model.addAttribute("clientesComMaisDevolucoes", clientesComMaisDevolucoes);
        model.addAttribute("produtosComMaisDevolucoes", produtosComMaisDevolucoes);
        model.addAttribute("devolucoesPorDia", devolucoesPorDia);
        
        // KPIs
        BigDecimal valorTotalDevolucoes = devolucaoService.calcularValorTotalDevolucoes(inicio, fim);
        Double tempoMedioProcessamento = devolucaoService.getTempoMedioProcessamento();
        Map<Devolucao.MotivoDevolucao, Long> motivosPrincipais = devolucaoService.getEstatisticasPorMotivo();
        
        model.addAttribute("valorTotalDevolucoes", valorTotalDevolucoes);
        model.addAttribute("tempoMedioProcessamento", tempoMedioProcessamento);
        model.addAttribute("motivosPrincipais", motivosPrincipais);
        
        return "vendas/devolucoes/relatorios";
    }

    // API endpoints for charts and reports
    @GetMapping("/api/estatisticas-motivo")
    @ResponseBody
    public ResponseEntity<Map<Devolucao.MotivoDevolucao, Long>> getEstatisticasMotivo() {
        return ResponseEntity.ok(devolucaoService.getEstatisticasPorMotivo());
    }

    @GetMapping("/api/devolucoes-dia")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getDevolucoesPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.atTime(23, 59, 59);
        
        return ResponseEntity.ok(devolucaoService.getDevolucoesPorDia(inicioDateTime, fimDateTime));
    }

    @GetMapping("/api/produtos-mais-devolvidos")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getProdutosMaisDevolvidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.atTime(23, 59, 59);
        
        return ResponseEntity.ok(devolucaoService.getProdutosComMaisDevolucoes(inicioDateTime, fimDateTime));
    }
}