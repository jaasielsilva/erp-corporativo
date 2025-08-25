package com.jaasielsilva.portalceo.controller.api;

import com.jaasielsilva.portalceo.service.BeneficioAdesaoService;
import com.jaasielsilva.portalceo.service.BeneficioAdesaoService.BeneficioInfo;
import com.jaasielsilva.portalceo.service.BeneficioAdesaoService.CalculoBeneficio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestão de benefícios no processo de adesão de colaboradores.
 * Fornece endpoints para listar benefícios, calcular custos e validar seleções.
 */
@RestController
@RequestMapping("/api/rh/colaboradores/adesao/beneficios")
public class BeneficioAdesaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(BeneficioAdesaoController.class);
    
    @Autowired
    private BeneficioAdesaoService beneficioAdesaoService;
    
    /**
     * Listar todos os benefícios disponíveis para adesão
     */
    @GetMapping("/disponiveis")
    public ResponseEntity<Map<String, Object>> listarBeneficiosDisponiveis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<BeneficioInfo> beneficios = beneficioAdesaoService.obterBeneficiosDisponiveis();
            
            response.put("success", true);
            response.put("beneficios", beneficios);
            response.put("total", beneficios.size());
            
            logger.info("Listados {} benefícios disponíveis", beneficios.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao listar benefícios disponíveis: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Calcular custo dos benefícios selecionados
     */
    @PostMapping("/calcular")
    public ResponseEntity<Map<String, Object>> calcularCustoBeneficios(
            @RequestBody Map<String, Object> beneficiosSelecionados,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar seleção
            List<String> erros = beneficioAdesaoService.validarSelecaoBeneficios(beneficiosSelecionados);
            
            if (!erros.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dados inválidos");
                response.put("errors", erros);
                
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calcular custos
            CalculoBeneficio calculo = beneficioAdesaoService.calcularCustoBeneficios(beneficiosSelecionados);
            
            // Salvar na sessão
            session.setAttribute("beneficios_calculo", calculo);
            session.setAttribute("beneficios_selecionados", beneficiosSelecionados);
            
            response.put("success", true);
            response.put("calculo", calculo);
            response.put("message", "Cálculo realizado com sucesso");
            
            logger.info("Cálculo de benefícios realizado para sessão: {}", session.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao calcular custo dos benefícios: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Validar seleção de benefícios
     */
    @PostMapping("/validar")
    public ResponseEntity<Map<String, Object>> validarSelecaoBeneficios(
            @RequestBody Map<String, Object> beneficiosSelecionados) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> erros = beneficioAdesaoService.validarSelecaoBeneficios(beneficiosSelecionados);
            
            if (erros.isEmpty()) {
                response.put("success", true);
                response.put("message", "Seleção válida");
                response.put("valid", true);
            } else {
                response.put("success", false);
                response.put("message", "Dados inválidos");
                response.put("valid", false);
                response.put("errors", erros);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao validar seleção de benefícios: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("valid", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Obter benefícios salvos na sessão
     */
    @GetMapping("/sessao")
    public ResponseEntity<Map<String, Object>> obterBeneficiosSessao(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> beneficiosSelecionados = 
                (Map<String, Object>) session.getAttribute("beneficios_selecionados");
            
            CalculoBeneficio calculo = 
                (CalculoBeneficio) session.getAttribute("beneficios_calculo");
            
            response.put("success", true);
            response.put("beneficios", beneficiosSelecionados);
            response.put("calculo", calculo);
            response.put("temDados", beneficiosSelecionados != null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter benefícios da sessão: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Limpar benefícios da sessão
     */
    @DeleteMapping("/sessao")
    public ResponseEntity<Map<String, Object>> limparBeneficiosSessao(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            session.removeAttribute("beneficios_selecionados");
            session.removeAttribute("beneficios_calculo");
            
            response.put("success", true);
            response.put("message", "Benefícios removidos da sessão");
            
            logger.info("Benefícios removidos da sessão: {}", session.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao limpar benefícios da sessão: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Obter resumo dos benefícios para revisão
     */
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> obterResumoBeneficios(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> beneficiosSelecionados = 
                (Map<String, Object>) session.getAttribute("beneficios_selecionados");
            
            CalculoBeneficio calculo = 
                (CalculoBeneficio) session.getAttribute("beneficios_calculo");
            
            if (beneficiosSelecionados == null || calculo == null) {
                response.put("success", false);
                response.put("message", "Nenhum benefício selecionado");
                response.put("temBeneficios", false);
                
                return ResponseEntity.ok(response);
            }
            
            // Criar resumo detalhado
            Map<String, Object> resumo = new HashMap<>();
            resumo.put("beneficiosSelecionados", beneficiosSelecionados);
            resumo.put("calculo", calculo);
            resumo.put("custoTotalFormatado", "R$ " + calculo.getCustoTotal().toString());
            resumo.put("quantidadeItens", calculo.getQuantidadeItens());
            
            response.put("success", true);
            response.put("resumo", resumo);
            response.put("temBeneficios", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter resumo dos benefícios: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Verificar se benefícios obrigatórios foram selecionados
     */
    @GetMapping("/obrigatorios/status")
    public ResponseEntity<Map<String, Object>> verificarBeneficiosObrigatorios(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> beneficiosSelecionados = 
                (Map<String, Object>) session.getAttribute("beneficios_selecionados");
            
            // Por enquanto, não há benefícios obrigatórios
            // Esta funcionalidade pode ser expandida no futuro
            boolean obrigatoriosCompletos = true;
            List<String> obrigatoriosFaltantes = List.of();
            
            response.put("success", true);
            response.put("obrigatoriosCompletos", obrigatoriosCompletos);
            response.put("obrigatoriosFaltantes", obrigatoriosFaltantes);
            response.put("temBeneficios", beneficiosSelecionados != null && !beneficiosSelecionados.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao verificar benefícios obrigatórios: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}