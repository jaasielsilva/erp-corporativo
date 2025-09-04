package com.jaasielsilva.portalceo.controller.rh.beneficios;

import com.jaasielsilva.portalceo.dto.ConfiguracaoValeTransporteDTO;
import com.jaasielsilva.portalceo.dto.ResumoValeTransporteDTO;
import com.jaasielsilva.portalceo.dto.ValeTransporteListDTO;
import com.jaasielsilva.portalceo.model.ValeTransporte;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.ValeTransporteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rh/beneficios/vale-transporte")
public class ValeTransporteController {

    private static final Logger logger = LoggerFactory.getLogger(ValeTransporteController.class);

    private final ValeTransporteService service;
    
    @Autowired
    private ColaboradorService colaboradorService;

    public ValeTransporteController(ValeTransporteService service) {
        this.service = service;
    }

    // LISTAR - COM DADOS REAIS
    @GetMapping("/listar")
    public String listar(Model model,
                        @RequestParam(value = "mes", required = false) Integer mes,
                        @RequestParam(value = "ano", required = false) Integer ano,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "departamento", required = false) String departamento) {
        
        logger.debug("Listando vale transporte - Filtros: mes={}, ano={}, status={}, departamento={}", 
                    mes, ano, status, departamento);
        
        try {
            // Definir mês/ano padrão se não informado
            LocalDate hoje = LocalDate.now();
            if (mes == null) mes = hoje.getMonthValue();
            if (ano == null) ano = hoje.getYear();
            
            // Buscar vales transporte filtrados
            List<ValeTransporteListDTO> vales;
            if (status != null && !status.isEmpty()) {
                ValeTransporte.StatusValeTransporte statusEnum = 
                    ValeTransporte.StatusValeTransporte.valueOf(status.toUpperCase());
                vales = service.listarPorStatus(statusEnum);
            } else {
                vales = service.listarPorMesAno(mes, ano);
            }
            
            // Gerar resumo mensal
            ResumoValeTransporteDTO resumo = service.gerarResumoMensal(mes, ano);
            
            // Adicionar dados ao modelo
            model.addAttribute("vales", vales);
            model.addAttribute("resumo", resumo);
            model.addAttribute("mesAtual", mes);
            model.addAttribute("anoAtual", ano);
            model.addAttribute("statusFiltro", status);
            model.addAttribute("departamentoFiltro", departamento);
            
            // Configuracao para formularios
            ConfiguracaoValeTransporteDTO config = new ConfiguracaoValeTransporteDTO();
            model.addAttribute("configuracao", config);
            
            return "rh/beneficios/vale-transporte/listar";
            
        } catch (Exception e) {
            logger.error("Erro ao listar vale transporte: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar dados: " + e.getMessage());
            return "rh/beneficios/vale-transporte/listar";
        }
    }

    // FORM NOVO - COM COLABORADORES
    @GetMapping("/novo")
    public String novo(Model model) {
        try {
            model.addAttribute("vale", new ValeTransporte());
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            
            // Valores padrão
            LocalDate hoje = LocalDate.now();
            model.addAttribute("mesAtual", hoje.getMonthValue());
            model.addAttribute("anoAtual", hoje.getYear());
            
            return "rh/beneficios/vale-transporte/form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário novo: {}", e.getMessage(), e);
            return "redirect:/rh/beneficios/vale-transporte/listar";
        }
    }

    // SALVAR - COM VALIDAÇÕES
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("vale") ValeTransporte vale, 
                        BindingResult result, 
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        logger.debug("Salvando vale transporte para colaborador: {}", 
                    vale.getColaborador() != null ? vale.getColaborador().getId() : "N/A");
        
        if (result.hasErrors()) {
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
        
        try {
            service.salvar(vale);
            redirectAttributes.addFlashAttribute("mensagem", "Vale transporte salvo com sucesso!");
            return "redirect:/rh/beneficios/vale-transporte/listar";
        } catch (Exception e) {
            logger.error("Erro ao salvar vale transporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
    }

    // FORM EDITAR
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        try {
            ValeTransporte vale = service.buscarPorId(id);
            model.addAttribute("vale", vale);
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de edição: {}", e.getMessage(), e);
            return "redirect:/rh/beneficios/vale-transporte/listar";
        }
    }

    // ATUALIZAR
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("vale") ValeTransporte vale,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
        
        try {
            vale.setId(id);
            service.salvar(vale);
            redirectAttributes.addFlashAttribute("mensagem", "Vale transporte atualizado com sucesso!");
            return "redirect:/rh/beneficios/vale-transporte/listar";
        } catch (Exception e) {
            logger.error("Erro ao atualizar vale transporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
            model.addAttribute("colaboradores", colaboradorService.listarAtivos());
            return "rh/beneficios/vale-transporte/form";
        }
    }

    // DELETAR
    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Vale transporte removido com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao deletar vale transporte: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/rh/beneficios/vale-transporte/listar";
    }

    // === ENDPOINTS AJAX/API ===

    /**
     * API para buscar estatísticas em tempo real
     */
    @GetMapping("/api/estatisticas")
    @ResponseBody
    public ResponseEntity<ResumoValeTransporteDTO> getEstatisticas(
            @RequestParam(defaultValue = "0") Integer mes,
            @RequestParam(defaultValue = "0") Integer ano) {
        
        try {
            LocalDate hoje = LocalDate.now();
            if (mes == 0) mes = hoje.getMonthValue();
            if (ano == 0) ano = hoje.getYear();
            
            ResumoValeTransporteDTO resumo = service.gerarResumoMensal(mes, ano);
            return ResponseEntity.ok(resumo);
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Processar cálculo mensal em lote
     */
    @PostMapping("/api/calcular-mes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calcularMes(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            @RequestBody ConfiguracaoValeTransporteDTO config) {
        
        logger.info("Iniciando cálculo mensal para {}/{}", mes, ano);
        Map<String, Object> response = new HashMap<>();
        
        try {
            int processados = service.calcularValesMensais(mes, ano, config);
            
            response.put("sucesso", true);
            response.put("processados", processados);
            response.put("mensagem", String.format("Processados %d vales transporte para %d/%d", 
                        processados, mes, ano));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro no cálculo mensal: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Suspender vale transporte
     */
    @PostMapping("/api/suspender/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suspender(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            service.suspender(id, motivo);
            response.put("sucesso", true);
            response.put("mensagem", "Vale transporte suspenso com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao suspender vale: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Reativar vale transporte
     */
    @PostMapping("/api/reativar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reativar(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            service.reativar(id);
            response.put("sucesso", true);
            response.put("mensagem", "Vale transporte reativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao reativar vale: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cancelar vale transporte
     */
    @PostMapping("/api/cancelar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelar(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            service.cancelar(id, motivo);
            response.put("sucesso", true);
            response.put("mensagem", "Vale transporte cancelado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao cancelar vale: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Exportar relatório de Vale Transporte
     */
    @GetMapping("/api/relatorio")
    public ResponseEntity<Map<String, Object>> exportarRelatorio(
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "formato", defaultValue = "json") String formato) {
        
        try {
            LocalDate hoje = LocalDate.now();
            if (mes == null) mes = hoje.getMonthValue();
            if (ano == null) ano = hoje.getYear();
            
            // Buscar dados para o relatório
            List<ValeTransporteListDTO> vales = mes != null && ano != null ? 
                service.listarPorMesAno(mes, ano) : service.listarComColaboradores();
            
            ResumoValeTransporteDTO resumo = service.gerarResumoMensal(mes, ano);
            
            Map<String, Object> relatorio = new HashMap<>();
            relatorio.put("titulo", "Relatório de Vale Transporte");
            relatorio.put("periodo", mes + "/" + ano);
            relatorio.put("dataGeracao", LocalDate.now().toString());
            relatorio.put("resumo", resumo);
            relatorio.put("vales", vales);
            relatorio.put("totalRegistros", vales.size());
            
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
