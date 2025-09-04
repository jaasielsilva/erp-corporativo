package com.jaasielsilva.portalceo.controller.rh.colaborador;

import com.jaasielsilva.portalceo.service.BeneficioAdesaoService;
import com.jaasielsilva.portalceo.service.AdesaoColaboradorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para APIs de benefícios no processo de adesão de colaboradores.
 * Responsável por fornecer benefícios disponíveis, calcular custos e gerenciar seleções.
 */
@RestController
@RequestMapping("/api/rh/colaboradores/adesao/beneficios")
public class BeneficioAdesaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(BeneficioAdesaoController.class);
    
    @Autowired
    private BeneficioAdesaoService beneficioAdesaoService;
    
    @Autowired
    private AdesaoColaboradorService adesaoService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Lista benefícios disponíveis para seleção
     */
    @GetMapping("/disponiveis")
    public ResponseEntity<Map<String, Object>> listarBeneficiosDisponiveis(
            @RequestParam(required = false) String sessionId) {
        try {
            logger.info("Listando benefícios disponíveis para sessionId: {}", sessionId);
            
            List<BeneficioAdesaoService.BeneficioInfo> beneficios = 
                beneficioAdesaoService.obterBeneficiosDisponiveis();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("beneficios", beneficios);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao listar benefícios disponíveis: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao carregar benefícios disponíveis");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Recupera benefícios já selecionados na sessão atual
     */
    @GetMapping("/sessao")
    public ResponseEntity<Map<String, Object>> obterBeneficiosSessao(
            @RequestParam String sessionId) {
        try {
            logger.info("Recuperando benefícios da sessão: {}", sessionId);
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "SessionId é obrigatório");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Verificar se a sessão existe
            if (!adesaoService.existeSessao(sessionId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Sessão não encontrada");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> beneficiosSelecionados = adesaoService.obterBeneficiosSessao(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("beneficiosSelecionados", beneficiosSelecionados));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao recuperar benefícios da sessão {}: {}", sessionId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao recuperar benefícios da sessão");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Endpoint de teste para verificar se o controller está funcionando
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "BeneficioAdesaoController está funcionando!");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de debug para verificar planos de saúde no banco
     */
    @GetMapping("/debug/planos")
    public ResponseEntity<Map<String, Object>> debugPlanos() {
        try {
            List<BeneficioAdesaoService.BeneficioInfo> beneficios = 
                beneficioAdesaoService.obterBeneficiosDisponiveis();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("beneficios", beneficios);
            response.put("message", "Debug: Benefícios disponíveis carregados com sucesso");
            
            // Log detalhado dos planos encontrados
            beneficios.forEach(beneficio -> {
                logger.info("Benefício: {} - Tipo: {} - Opções: {}", 
                    beneficio.getNome(), beneficio.getTipo(), beneficio.getOpcoes().size());
                if ("PLANO_SAUDE".equals(beneficio.getTipo())) {
                    beneficio.getOpcoes().forEach(opcao -> {
                        logger.info("  -> Plano: {} - ID: {} - Valor: R$ {}", 
                            opcao.getNome(), opcao.getId(), opcao.getValor());
                    });
                }
            });
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro no debug de planos: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao carregar planos: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Calcula o valor total dos benefícios selecionados
     */
    @PostMapping("/calcular")
    public ResponseEntity<Map<String, Object>> calcularCustoBeneficios(
            @RequestBody Map<String, Object> request) {
        try {
            logger.info("=== INÍCIO PROCESSAMENTO CÁLCULO BENEFÍCIOS ===");
            logger.info("Request recebido: {}", request);
            
            String sessionId = (String) request.get("sessionId");
            Object beneficiosObj = request.get("beneficios");
            
            logger.info("SessionId: {}", sessionId);
            logger.info("Benefícios recebidos: {}", beneficiosObj);
            logger.info("Tipo do objeto benefícios: {}", beneficiosObj != null ? beneficiosObj.getClass().getName() : "null");
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("SessionId não informado ou vazio");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "SessionId é obrigatório");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (beneficiosObj == null) {
                logger.warn("Benefícios não informados");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Benefícios são obrigatórios");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Verificar se a sessão existe ANTES de converter benefícios
            if (!adesaoService.existeSessao(sessionId)) {
                logger.warn("Sessão não encontrada: {}", sessionId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Sessão não encontrada");
                return ResponseEntity.notFound().build();
            }
            
            // Converter benefícios para o formato esperado
            Map<String, Object> beneficios = new HashMap<>();
            if (beneficiosObj instanceof String) {
                // Se vier como string JSON, fazer parse
                logger.info("Convertendo string JSON para Map");
                beneficios = objectMapper.readValue((String) beneficiosObj, 
                    new TypeReference<Map<String, Object>>() {});
            } else if (beneficiosObj instanceof List) {
                // Se vier como lista (formato do frontend), converter para Map
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> beneficiosList = (List<Map<String, Object>>) beneficiosObj;
                logger.info("Convertendo lista para Map. Lista contém {} itens", beneficiosList.size());
                for (Map<String, Object> item : beneficiosList) {
                    logger.info("Item da lista: {}", item);
                }
                beneficios.put("beneficios", beneficiosList);
                logger.info("Map convertido: {}", beneficios);
            } else if (beneficiosObj instanceof Map) {
                // Se já for um Map
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) beneficiosObj;
                beneficios = temp;
                logger.info("Benefícios já em formato Map: {}", beneficios);
            } else {
                logger.error("Tipo de benefícios não suportado: {}", beneficiosObj.getClass().getName());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Formato de benefícios inválido. Tipo recebido: " + beneficiosObj.getClass().getSimpleName());
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.info("Benefícios processados para validação: {}", beneficios);
            
            // Validar benefícios selecionados
            logger.info("=== INICIANDO VALIDAÇÃO ===");
            List<String> errosValidacao = beneficioAdesaoService.validarSelecaoBeneficios(beneficios);
            logger.info("Erros de validação encontrados: {}", errosValidacao);
            
            if (!errosValidacao.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Seleção de benefícios inválida");
                errorResponse.put("erros", errosValidacao);
                logger.warn("Retornando erro de validação: {}", errorResponse);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Calcular custos
            logger.info("=== INICIANDO CÁLCULO ===");
            BeneficioAdesaoService.CalculoBeneficio calculo = 
                beneficioAdesaoService.calcularCustoBeneficios(beneficios);
            
            logger.info("Cálculo realizado: {} itens, custo total: R$ {}", 
                calculo.getItens().size(), calculo.getCustoTotal());
            
            // Montar resposta no formato esperado
            Map<String, Object> resumo = new HashMap<>();
            resumo.put("itens", calculo.getItens());
            resumo.put("totalMensal", calculo.getCustoTotal());
            resumo.put("totalAnual", calculo.getCustoTotal().multiply(java.math.BigDecimal.valueOf(12)));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("resumo", resumo));
            
            logger.info("=== SUCESSO - RETORNANDO RESPOSTA ===");
            logger.info("Resposta: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== ERRO NO PROCESSAMENTO ===");
            logger.error("Erro ao calcular custos de benefícios para request: {}", request, e);
            logger.error("Tipo da exceção: {}", e.getClass().getSimpleName());
            logger.error("Mensagem: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro interno do servidor: " + e.getMessage());
            errorResponse.put("details", "Verifique os logs do servidor para mais detalhes");
            
            // Em ambiente de desenvolvimento, incluir stack trace
            if (logger.isDebugEnabled()) {
                errorResponse.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
            }
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtém resumo dos benefícios selecionados para a tela de revisão
     */
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> obterResumoBeneficios(
            @RequestParam String sessionId) {
        try {
            logger.info("=== INICIO RESUMO BENEFICIOS ===");
            logger.info("Obtendo resumo de benefícios para sessionId: {}", sessionId);
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("SessionId é null ou vazio");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "SessionId é obrigatório");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Verificar se a sessão existe
            logger.info("Verificando se sessão existe: {}", sessionId);
            boolean sessionExists = adesaoService.existeSessao(sessionId);
            logger.info("Sessão existe: {}", sessionExists);
            
            if (!sessionExists) {
                logger.warn("Sessão não encontrada: {}", sessionId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Sessão não encontrada");
                return ResponseEntity.notFound().build();
            }
            
            // Obter benefícios da sessão
            logger.info("Obtendo benefícios da sessão: {}", sessionId);
            Map<String, Object> beneficiosSelecionados = adesaoService.obterBeneficiosSessao(sessionId);
            logger.info("Benefícios obtidos: {} itens", beneficiosSelecionados != null ? beneficiosSelecionados.size() : 0);
            
            if (beneficiosSelecionados == null || beneficiosSelecionados.isEmpty()) {
                logger.info("Nenhum benefício selecionado para sessionId: {}", sessionId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("temBeneficios", false);
                response.put("resumo", Map.of(
                    "beneficios", List.of(),
                    "totalMensal", 0.0
                ));
                return ResponseEntity.ok(response);
            }
            
            // Calcular resumo
            logger.info("Calculando resumo de benefícios para {} itens", beneficiosSelecionados.size());
            BeneficioAdesaoService.CalculoBeneficio calculo = 
                beneficioAdesaoService.calcularCustoBeneficios(beneficiosSelecionados);
            logger.info("Cálculo realizado com sucesso: {} itens, total: R$ {}", 
                       calculo.getQuantidadeItens(), calculo.getCustoTotal());
            
            // Montar resumo no formato esperado pelo frontend
            Map<String, Object> resumo = new HashMap<>();
            resumo.put("beneficios", calculo.getItens());
            resumo.put("totalMensal", calculo.getCustoTotal());
            resumo.put("totalAnual", calculo.getCustoTotal().multiply(java.math.BigDecimal.valueOf(12)));
            resumo.put("quantidadeItens", calculo.getQuantidadeItens());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("temBeneficios", true);
            response.put("resumo", resumo);
            
            logger.info("=== RESUMO BENEFICIOS SUCESSO ===");
            logger.info("Resumo de benefícios gerado: {} itens, total mensal: R$ {}", 
                       calculo.getQuantidadeItens(), calculo.getCustoTotal());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== ERRO RESUMO BENEFICIOS ===");
            logger.error("Erro ao obter resumo de benefícios para sessionId {}: {}", sessionId, e.getMessage(), e);
            logger.error("Tipo da exceção: {}", e.getClass().getSimpleName());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro interno do servidor: " + e.getMessage());
            errorResponse.put("details", "Verifique os logs do servidor para mais detalhes");
            
            // Em ambiente de desenvolvimento, incluir stack trace
            if (logger.isDebugEnabled()) {
                errorResponse.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
            }
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Valida seleção de benefícios
     */
    @PostMapping("/validar")
    public ResponseEntity<Map<String, Object>> validarSelecaoBeneficios(
            @RequestBody Map<String, Object> request) {
        try {
            Object beneficiosObj = request.get("beneficios");
            
            if (beneficiosObj == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Benefícios são obrigatórios");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Converter benefícios para Map
            Map<String, Object> beneficios;
            if (beneficiosObj instanceof String) {
                beneficios = objectMapper.readValue((String) beneficiosObj, 
                    new TypeReference<Map<String, Object>>() {});
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) beneficiosObj;
                beneficios = temp;
            }
            
            List<String> errosValidacao = beneficioAdesaoService.validarSelecaoBeneficios(beneficios);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valido", errosValidacao.isEmpty());
            if (!errosValidacao.isEmpty()) {
                response.put("erros", errosValidacao);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao validar seleção de benefícios: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao validar seleção de benefícios");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}