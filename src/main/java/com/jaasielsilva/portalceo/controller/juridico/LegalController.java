package com.jaasielsilva.portalceo.controller.juridico;

import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.ContratoAditivo;
import com.jaasielsilva.portalceo.model.ContratoAlerta;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ContratoLegalService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.repository.ContratoAditivoRepository;
import com.jaasielsilva.portalceo.repository.ContratoAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import java.util.HashMap;

@Controller
@RequestMapping("/juridico")
public class LegalController {

    @Autowired
    private ContratoLegalService contratoLegalService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ContratoAditivoRepository contratoAditivoRepository;

    @Autowired
    private ContratoAlertaRepository contratoAlertaRepository;

    // ==================== CONTRATOS ====================

    @GetMapping("/contratos")
    public String listarContratos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dataCriacao") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) ContratoLegal.StatusContrato status,
            @RequestParam(required = false) ContratoLegal.TipoContrato tipo,
            Model model) {

        Sort.Direction sortDirection = direction.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<ContratoLegal> contratos = contratoLegalService.buscarContratosComFiltros(numero, status, tipo, pageable);
        
        model.addAttribute("contratos", contratos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contratos.getTotalPages());
        model.addAttribute("totalElements", contratos.getTotalElements());
        model.addAttribute("statusOptions", ContratoLegal.StatusContrato.values());
        model.addAttribute("tipoOptions", ContratoLegal.TipoContrato.values());
        model.addAttribute("numero", numero);
        model.addAttribute("status", status);
        model.addAttribute("tipo", tipo);
        
        return "juridico/contratos/lista";
    }

    @GetMapping("/contratos/novo")
    public String novoContrato(Model model) {
        model.addAttribute("contrato", new ContratoLegal());
        model.addAttribute("tipoOptions", ContratoLegal.TipoContrato.values());
        model.addAttribute("prioridadeOptions", ContratoLegal.PrioridadeContrato.values());
        return "juridico/contratos/form";
    }

    @PostMapping("/contratos/salvar")
    public String salvarContrato(@Valid @ModelAttribute ContratoLegal contrato, 
                                BindingResult result, 
                                RedirectAttributes redirectAttributes, 
                                Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("tipoOptions", ContratoLegal.TipoContrato.values());
            model.addAttribute("prioridadeOptions", ContratoLegal.PrioridadeContrato.values());
            return "juridico/contratos/form";
        }

        try {
            ContratoLegal contratoSalvo = contratoLegalService.salvarContrato(contrato);
            redirectAttributes.addFlashAttribute("sucesso", "Contrato salvo com sucesso!");
            return "redirect:/juridico/contratos/" + contratoSalvo.getId();
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar contrato: " + e.getMessage());
            model.addAttribute("tipoOptions", ContratoLegal.TipoContrato.values());
            model.addAttribute("prioridadeOptions", ContratoLegal.PrioridadeContrato.values());
            return "juridico/contratos/form";
        }
    }

    @GetMapping("/contratos/{id}")
    public String visualizarContrato(@PathVariable Long id, Model model) {
        ContratoLegal contrato = contratoLegalService.buscarPorId(id);
        List<ContratoAditivo> aditivos = contratoAditivoRepository.findByContratoId(id);
        List<ContratoAlerta> alertas = contratoAlertaRepository.findByContratoId(id);
        
        model.addAttribute("contrato", contrato);
        model.addAttribute("aditivos", aditivos);
        model.addAttribute("alertas", alertas);
        model.addAttribute("alertasAtivos", alertas.stream().filter(a -> !a.isResolvido()).count());
        
        return "juridico/contratos/visualizar";
    }

    @GetMapping("/contratos/{id}/editar")
    public String editarContrato(@PathVariable Long id, Model model) {
        ContratoLegal contrato = contratoLegalService.buscarPorId(id);
        model.addAttribute("contrato", contrato);
        model.addAttribute("tipoOptions", ContratoLegal.TipoContrato.values());
        model.addAttribute("prioridadeOptions", ContratoLegal.PrioridadeContrato.values());
        return "juridico/contratos/form";
    }

    @PostMapping("/contratos/{id}/aprovar")
    public String aprovarContrato(@PathVariable Long id, 
                                 @RequestParam(required = false) String observacoes,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            contratoLegalService.aprovarContrato(id, observacoes != null ? observacoes : "Aprovado", usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Contrato aprovado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao aprovar contrato: " + e.getMessage());
        }
        return "redirect:/juridico/contratos/" + id;
    }

    @PostMapping("/contratos/{id}/assinar")
    public String assinarContrato(@PathVariable Long id, 
                                 @RequestParam(required = false) String dataAssinatura,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByNome(authentication.getName());
            LocalDate data = dataAssinatura != null ? LocalDate.parse(dataAssinatura) : LocalDate.now();
            contratoLegalService.assinarContrato(id, data, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Contrato assinado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao assinar contrato: " + e.getMessage());
        }
        return "redirect:/juridico/contratos/" + id;
    }

    @PostMapping("/contratos/{id}/ativar")
    public String ativarContrato(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratoLegalService.ativarContrato(id);
            redirectAttributes.addFlashAttribute("sucesso", "Contrato ativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao ativar contrato: " + e.getMessage());
        }
        return "redirect:/juridico/contratos/" + id;
    }

    @PostMapping("/contratos/{id}/suspender")
    public String suspenderContrato(@PathVariable Long id, 
                                   @RequestParam String motivo,
                                   RedirectAttributes redirectAttributes) {
        try {
            contratoLegalService.suspenderContrato(id, motivo);
            redirectAttributes.addFlashAttribute("sucesso", "Contrato suspenso com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao suspender contrato: " + e.getMessage());
        }
        return "redirect:/juridico/contratos/" + id;
    }

    @PostMapping("/contratos/{id}/renovar")
    public String renovarContrato(@PathVariable Long id, 
                                 @RequestParam Integer novasDuracaoMeses,
                                 @RequestParam BigDecimal novoValor,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
            contratoLegalService.renovarContrato(id, novasDuracaoMeses, novoValor, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Contrato renovado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao renovar contrato: " + e.getMessage());
        }
        return "redirect:/juridico/contratos/" + id;
    }

    // ==================== ADITIVOS ====================

    @GetMapping("/aditivos")
    public String listarAditivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ContratoAditivo.StatusAditivo status,
            @RequestParam(required = false) ContratoAditivo.TipoAditivo tipo,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCriacao"));
        
        List<ContratoAditivo> aditivos;
        if (status != null && tipo != null) {
            aditivos = contratoAditivoRepository.findByTipoAndStatus(tipo, status);
        } else if (status != null) {
            aditivos = contratoAditivoRepository.findByStatusOrderByDataCriacaoDesc(status);
        } else if (tipo != null) {
            aditivos = contratoAditivoRepository.findByTipoOrderByDataCriacaoDesc(tipo);
        } else {
            aditivos = contratoAditivoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataCriacao"));
        }
        
        model.addAttribute("aditivos", aditivos);
        model.addAttribute("statusOptions", ContratoAditivo.StatusAditivo.values());
        model.addAttribute("tipoOptions", ContratoAditivo.TipoAditivo.values());
        model.addAttribute("status", status);
        model.addAttribute("tipo", tipo);
        
        return "juridico/aditivos/lista";
    }

    @GetMapping("/contratos/{contratoId}/aditivos/novo")
    public String novoAditivo(@PathVariable Long contratoId, Model model) {
        ContratoLegal contrato = contratoLegalService.buscarPorId(contratoId);
        ContratoAditivo aditivo = new ContratoAditivo();
        aditivo.setContrato(contrato);
        
        model.addAttribute("aditivo", aditivo);
        model.addAttribute("contrato", contrato);
        model.addAttribute("tipoOptions", ContratoAditivo.TipoAditivo.values());
        
        return "juridico/aditivos/form";
    }

    // ==================== ALERTAS ====================

    @GetMapping("/alertas")
    public String listarAlertas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ContratoAlerta.TipoAlerta tipo,
            @RequestParam(required = false) ContratoAlerta.PrioridadeAlerta prioridade,
            @RequestParam(required = false) Boolean resolvido,
            Model model) {

        List<ContratoAlerta> alertas;
        
        if (resolvido != null && !resolvido) {
            alertas = contratoAlertaRepository.findAlertasNaoResolvidos();
        } else if (tipo != null && prioridade != null) {
            alertas = contratoAlertaRepository.findByTipoAndPrioridadeAndNaoResolvido(tipo, prioridade);
        } else if (tipo != null) {
            alertas = contratoAlertaRepository.findByTipoOrderByDataAlertaDesc(tipo);
        } else if (prioridade != null) {
            alertas = contratoAlertaRepository.findByPrioridadeOrderByDataAlertaDesc(prioridade);
        } else {
            alertas = contratoAlertaRepository.findAll(Sort.by(Sort.Direction.DESC, "dataAlerta"));
        }
        
        model.addAttribute("alertas", alertas);
        model.addAttribute("tipoOptions", ContratoAlerta.TipoAlerta.values());
        model.addAttribute("prioridadeOptions", ContratoAlerta.PrioridadeAlerta.values());
        model.addAttribute("tipo", tipo);
        model.addAttribute("prioridade", prioridade);
        model.addAttribute("resolvido", resolvido);
        
        return "juridico/alertas/lista";
    }

    @PostMapping("/alertas/{id}/resolver")
    public String resolverAlerta(@PathVariable Long id,
                                @RequestParam String observacoes,
                                RedirectAttributes redirectAttributes) {
        try {
            ContratoAlerta alerta = contratoAlertaRepository.findById(id).orElseThrow();
            alerta.marcarComoResolvido(observacoes, null); // TODO: Get current user
            contratoAlertaRepository.save(alerta);
            redirectAttributes.addFlashAttribute("sucesso", "Alerta resolvido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao resolver alerta: " + e.getMessage());
        }
        return "redirect:/juridico/alertas";
    }

    // ==================== DASHBOARD E RELATÓRIOS ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        
        // Estatísticas de contratos
        Map<String, Object> estatisticasContratos = contratoLegalService.obterEstatisticasGerais(inicioMes, hoje);
        
        // Alertas críticos
        List<ContratoAlerta> alertasCriticos = contratoAlertaRepository.findAlertasCriticos();
        List<ContratoAlerta> alertasVencidos = contratoAlertaRepository.findAlertasVencidos(hoje);
        
        // Contratos vencendo
        List<ContratoLegal> contratosVencendo = contratoLegalService.buscarContratosVencendoEm(30);
        
        // Aditivos pendentes
        List<ContratoAditivo> aditivosPendentes = contratoAditivoRepository.findAditivosPendentesAnalise();
        
        model.addAttribute("estatisticasContratos", estatisticasContratos);
        model.addAttribute("alertasCriticos", alertasCriticos);
        model.addAttribute("alertasVencidos", alertasVencidos);
        model.addAttribute("contratosVencendo", contratosVencendo);
        model.addAttribute("aditivosPendentes", aditivosPendentes);
        
        return "juridico/dashboard";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        return "juridico/relatorios/index";
    }

    @GetMapping("/relatorios/contratos")
    public String relatorioContratos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) ContratoLegal.TipoContrato tipo,
            @RequestParam(required = false) ContratoLegal.StatusContrato status,
            Model model) {
        
        Map<String, Object> relatorio = contratoLegalService.gerarRelatorioContratos(dataInicio, dataFim, tipo, status);
        
        model.addAttribute("relatorio", relatorio);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("tipo", tipo);
        model.addAttribute("status", status);
        
        return "juridico/relatorios/contratos";
    }

    @GetMapping("/relatorios/vencimentos")
    public String relatorioVencimentos(
            @RequestParam(defaultValue = "30") int proximosDias,
            Model model) {
        
        Map<String, Object> relatorio = contratoLegalService.gerarRelatorioVencimentos(proximosDias);
        
        model.addAttribute("relatorio", relatorio);
        model.addAttribute("proximosDias", proximosDias);
        
        return "juridico/relatorios/vencimentos";
    }

    // ==================== API REST ====================

    @GetMapping("/api/contratos/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEstatisticasContratos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        Map<String, Object> estatisticas = contratoLegalService.obterEstatisticasGerais(dataInicio, dataFim);
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/api/alertas/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardAlertas() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        
        List<Object[]> dashboardData = contratoAlertaRepository.getDashboardAlertas(inicioMes, hoje);
        
        Map<String, Object> dashboard = new HashMap<>();
        if (!dashboardData.isEmpty()) {
            Object[] data = dashboardData.get(0);
            dashboard.put("totalAlertas", data[0]);
            dashboard.put("alertasAtivos", data[1]);
            dashboard.put("alertasResolvidos", data[2]);
            dashboard.put("alertasCriticos", data[3]);
            dashboard.put("alertasVencidos", data[4]);
        }
        
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/api/contratos/{id}/timeline")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTimelineContrato(@PathVariable Long id) {
        Map<String, Object> timeline = contratoLegalService.obterTimelineContrato(id);
        return ResponseEntity.ok(timeline);
    }
}