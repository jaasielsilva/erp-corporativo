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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class MarketingController {

    private final CampanhaMarketingService campanhaService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    // Dashboard de Marketing
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Marketing - Dashboard");
        model.addAttribute("moduleCSS", "marketing");
        
        // Campaign statistics
        Map<CampanhaMarketing.StatusCampanha, Long> estatisticasStatus = campanhaService.getEstatisticasPorStatus();
        Map<CampanhaMarketing.TipoCampanha, Long> estatisticasTipo = campanhaService.getEstatisticasPorTipo();
        model.addAttribute("estatisticasStatus", estatisticasStatus);
        model.addAttribute("estatisticasTipo", estatisticasTipo);
        
        // Active campaigns
        List<CampanhaMarketing> campanhasAtivas = campanhaService.findCampanhasAtivas();
        model.addAttribute("campanhasAtivas", campanhasAtivas);
        
        // Recent campaigns
        List<CampanhaMarketing> campanhasRecentes = campanhaService.findByStatus(CampanhaMarketing.StatusCampanha.FINALIZADA);
        model.addAttribute("campanhasRecentes", campanhasRecentes.stream().limit(5).toList());
        
        // Performance metrics
        BigDecimal roiTotal = campanhaService.calcularROITotal();
        model.addAttribute("roiTotal", roiTotal);
        
        // Expired campaigns requiring attention
        List<CampanhaMarketing> campanhasExpiradas = campanhaService.findCampanhasExpiradas();
        model.addAttribute("campanhasExpiradas", campanhasExpiradas);
        
        return "marketing/index";
    }

    // =============== CAMPANHAS ===============
    
    @GetMapping("/campanhas")
    public String listarCampanhas(Model model,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String tipo,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        model.addAttribute("pageTitle", "Campanhas de Marketing");
        model.addAttribute("moduleCSS", "marketing");
        
        List<CampanhaMarketing> campanhas;
        if (status != null && !status.isEmpty()) {
            campanhas = campanhaService.findByStatus(CampanhaMarketing.StatusCampanha.valueOf(status));
        } else {
            campanhas = campanhaService.findAll();
        }
        
        model.addAttribute("campanhas", campanhas);
        model.addAttribute("statusOptions", CampanhaMarketing.StatusCampanha.values());
        model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
        model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
        
        return "marketing/campanhas/lista";
    }
    
    @GetMapping("/campanhas/nova")
    public String novaCampanha(Model model) {
        model.addAttribute("pageTitle", "Nova Campanha");
        model.addAttribute("campanha", new CampanhaMarketing());
        model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
        model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
        model.addAttribute("usuarios", usuarioService.findAll());
        return "marketing/campanhas/form";
    }
    
    @PostMapping("/campanhas")
    public String salvarCampanha(@Valid @ModelAttribute CampanhaMarketing campanha,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", campanha.getId() == null ? "Nova Campanha" : "Editar Campanha");
            model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
            model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
            model.addAttribute("usuarios", usuarioService.findAll());
            return "marketing/campanhas/form";
        }
        
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            campanha.setUsuarioCriacao(usuario);
            
            campanhaService.save(campanha);
            redirectAttributes.addFlashAttribute("successMessage", "Campanha salva com sucesso!");
            return "redirect:/marketing/campanhas/" + campanha.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao salvar campanha: " + e.getMessage());
            model.addAttribute("pageTitle", campanha.getId() == null ? "Nova Campanha" : "Editar Campanha");
            model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
            model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
            model.addAttribute("usuarios", usuarioService.findAll());
            return "marketing/campanhas/form";
        }
    }
    
    @GetMapping("/campanhas/{id}")
    public String visualizarCampanha(@PathVariable Long id, Model model) {
        Optional<CampanhaMarketing> campanhaOpt = campanhaService.findById(id);
        if (campanhaOpt.isEmpty()) {
            return "redirect:/marketing/campanhas";
        }
        
        CampanhaMarketing campanha = campanhaOpt.get();
        
        model.addAttribute("pageTitle", "Campanha: " + campanha.getNome());
        model.addAttribute("campanha", campanha);
        
        // Action permissions
        model.addAttribute("podeEditar", campanha.podeSerEditada());
        model.addAttribute("podeIniciar", campanha.podeSerEnviada());
        model.addAttribute("podePausar", campanha.podeSerPausada());
        model.addAttribute("podeRetomar", campanha.podeSerRetomada());
        model.addAttribute("podeCancelar", campanha.podeSerCancelada());
        
        // Performance metrics
        model.addAttribute("roi", campanha.calcularROI());
        model.addAttribute("ctr", campanha.calcularCTR());
        model.addAttribute("taxaConversao", campanha.calcularTaxaConversao());
        model.addAttribute("custoAquisicao", campanha.calcularCustoAquisicao());
        model.addAttribute("ticketMedio", campanha.calcularTicketMedio());
        
        return "marketing/campanhas/detalhes";
    }
    
    @GetMapping("/campanhas/{id}/editar")
    public String editarCampanha(@PathVariable Long id, Model model) {
        Optional<CampanhaMarketing> campanhaOpt = campanhaService.findById(id);
        if (campanhaOpt.isEmpty()) {
            return "redirect:/marketing/campanhas";
        }
        
        CampanhaMarketing campanha = campanhaOpt.get();
        if (!campanha.podeSerEditada()) {
            return "redirect:/marketing/campanhas/" + id;
        }
        
        model.addAttribute("pageTitle", "Editar Campanha");
        model.addAttribute("campanha", campanha);
        model.addAttribute("tipoOptions", CampanhaMarketing.TipoCampanha.values());
        model.addAttribute("objetivoOptions", CampanhaMarketing.ObjetivoCampanha.values());
        model.addAttribute("usuarios", usuarioService.findAll());
        
        return "marketing/campanhas/form";
    }

    // =============== AÇÕES DA CAMPANHA ===============
    
    @PostMapping("/campanhas/{id}/iniciar")
    @ResponseBody
    public ResponseEntity<?> iniciarCampanha(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            campanhaService.iniciarCampanha(id, usuario);
            return ResponseEntity.ok("Campanha iniciada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/campanhas/{id}/pausar")
    @ResponseBody
    public ResponseEntity<?> pausarCampanha(@PathVariable Long id,
                                           @RequestParam(required = false) String motivo) {
        try {
            campanhaService.pausarCampanha(id, motivo);
            return ResponseEntity.ok("Campanha pausada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/campanhas/{id}/retomar")
    @ResponseBody
    public ResponseEntity<?> retomarCampanha(@PathVariable Long id) {
        try {
            campanhaService.retomarCampanha(id);
            return ResponseEntity.ok("Campanha retomada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/campanhas/{id}/finalizar")
    @ResponseBody
    public ResponseEntity<?> finalizarCampanha(@PathVariable Long id,
                                              @RequestParam(required = false) String observacoes) {
        try {
            campanhaService.finalizarCampanha(id, observacoes);
            return ResponseEntity.ok("Campanha finalizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/campanhas/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarCampanha(@PathVariable Long id,
                                             @RequestParam String motivo,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioService.findByUsuario(userDetails.getUsername()).orElse(null);
            campanhaService.cancelarCampanha(id, motivo, usuario);
            return ResponseEntity.ok("Campanha cancelada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============== PÚBLICO-ALVO ===============
    
    @GetMapping("/campanhas/{id}/publico-alvo")
    public String gerenciarPublicoAlvo(@PathVariable Long id, Model model) {
        Optional<CampanhaMarketing> campanhaOpt = campanhaService.findById(id);
        if (campanhaOpt.isEmpty()) {
            return "redirect:/marketing/campanhas";
        }
        
        CampanhaMarketing campanha = campanhaOpt.get();
        
        model.addAttribute("pageTitle", "Público-Alvo - " + campanha.getNome());
        model.addAttribute("campanha", campanha);
        model.addAttribute("clientes", clienteService.buscarTodos());
        model.addAttribute("podeEditar", campanha.podeSerEditada());
        
        return "marketing/campanhas/publico-alvo";
    }
    
    @PostMapping("/campanhas/{id}/publico-alvo/adicionar")
    @ResponseBody
    public ResponseEntity<?> adicionarClientesPublicoAlvo(@PathVariable Long id,
                                                          @RequestParam List<Long> clienteIds) {
        try {
            campanhaService.adicionarClientesPublicoAlvo(id, clienteIds);
            return ResponseEntity.ok("Clientes adicionados ao público-alvo com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/campanhas/{id}/publico-alvo/segmento")
    @ResponseBody
    public ResponseEntity<?> definirPublicoAlvoPorSegmento(@PathVariable Long id,
                                                          @RequestParam String segmento) {
        try {
            campanhaService.definirPublicoAlvoPorSegmento(id, segmento);
            return ResponseEntity.ok("Público-alvo definido por segmento com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============== RELATÓRIOS ===============
    
    @GetMapping("/relatorios")
    public String relatorios(Model model,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        model.addAttribute("pageTitle", "Relatórios de Marketing");
        
        // Default period
        if (dataInicio == null) dataInicio = LocalDate.now().withDayOfMonth(1);
        if (dataFim == null) dataFim = LocalDate.now();
        
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        
        // Performance by type
        List<Object[]> performancePorTipo = campanhaService.getPerformancePorTipo(dataInicio, dataFim);
        model.addAttribute("performancePorTipo", performancePorTipo);
        
        // Most profitable campaigns
        List<CampanhaMarketing> campanhasMaisLucrativas = campanhaService.findCampanhasMaisLucrativas();
        model.addAttribute("campanhasMaisLucrativas", campanhasMaisLucrativas.stream().limit(10).toList());
        
        // ROI total
        BigDecimal roiTotal = campanhaService.calcularROITotal();
        model.addAttribute("roiTotal", roiTotal);
        
        return "marketing/relatorios";
    }

    // =============== API ENDPOINTS ===============
    
    @GetMapping("/api/estatisticas-status")
    @ResponseBody
    public ResponseEntity<Map<CampanhaMarketing.StatusCampanha, Long>> getEstatisticasStatus() {
        return ResponseEntity.ok(campanhaService.getEstatisticasPorStatus());
    }
    
    @GetMapping("/api/estatisticas-tipo")
    @ResponseBody
    public ResponseEntity<Map<CampanhaMarketing.TipoCampanha, Long>> getEstatisticasTipo() {
        return ResponseEntity.ok(campanhaService.getEstatisticasPorTipo());
    }
    
    @GetMapping("/api/performance-tipo")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getPerformancePorTipo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        return ResponseEntity.ok(campanhaService.getPerformancePorTipo(inicio, fim));
    }
    
    @GetMapping("/api/campanhas-ativas")
    @ResponseBody
    public ResponseEntity<List<CampanhaMarketing>> getCampanhasAtivas() {
        return ResponseEntity.ok(campanhaService.findCampanhasAtivas());
    }

    // =============== AUTOMAÇÃO ===============
    
    @PostMapping("/api/processar-agendadas")
    @ResponseBody
    public ResponseEntity<?> processarCampanhasAgendadas() {
        try {
            campanhaService.processarCampanhasAgendadas();
            return ResponseEntity.ok("Campanhas agendadas processadas com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar campanhas: " + e.getMessage());
        }
    }
    
    @PostMapping("/api/finalizar-expiradas")
    @ResponseBody
    public ResponseEntity<?> finalizarCampanhasExpiradas() {
        try {
            campanhaService.finalizarCampanhasExpiradas();
            return ResponseEntity.ok("Campanhas expiradas finalizadas com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao finalizar campanhas: " + e.getMessage());
        }
    }
}