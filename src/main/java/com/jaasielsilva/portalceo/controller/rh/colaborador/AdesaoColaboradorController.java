package com.jaasielsilva.portalceo.controller.rh.colaborador;

import com.jaasielsilva.portalceo.dto.AdesaoColaboradorDTO;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.AdesaoColaboradorService;
import com.jaasielsilva.portalceo.service.AdesaoSecurityService;
import com.jaasielsilva.portalceo.service.AuditService;
import com.jaasielsilva.portalceo.service.CargoService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.BeneficioService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService;
import com.jaasielsilva.portalceo.service.BeneficioAdesaoService;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;

import jakarta.validation.Valid;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rh/colaboradores/adesao")
public class AdesaoColaboradorController {

    private static final Logger logger = LoggerFactory.getLogger(AdesaoColaboradorController.class);

    @Autowired
    private AdesaoColaboradorService adesaoService;

    @Autowired
    private AdesaoSecurityService securityService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private BeneficioService beneficioService;

    @Autowired
    private DocumentoAdesaoService documentoService;

    @Autowired
    private BeneficioAdesaoService beneficioAdesaoService;

    @Autowired
    private WorkflowAdesaoService workflowService;

    /**
     * Página inicial do processo de adesão
     */
    @GetMapping
    public String iniciarAdesao(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            Model model) {
        
        // Se sessionId foi fornecido, tentar continuar processo existente
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            try {
                AdesaoColaboradorDTO dadosExistentes = adesaoService.obterDadosCompletos(sessionId);
                if (dadosExistentes != null) {
                    logger.info("Continuando processo existente para sessão: {}", sessionId);
                    model.addAttribute("colaborador", dadosExistentes);
                    model.addAttribute("sessionId", sessionId);
                    model.addAttribute("cargos", cargoService.listarAtivos());
                    model.addAttribute("departamentos", departamentoService.listarTodos());
                    model.addAttribute("beneficios", beneficioService.listarTodos());
                    return "rh/colaboradores/adesao/inicio";
                }
            } catch (Exception e) {
                logger.warn("Erro ao recuperar sessão {}: {}", sessionId, e.getMessage());
                // Continua para iniciar novo processo
            }
        }
        
        // Iniciar novo processo
        logger.info("Iniciando novo processo de adesão de colaborador");
        logger.info("Cargos encontrados: {}", cargoService.listarAtivos().size());
        model.addAttribute("colaborador", new Colaborador());
        model.addAttribute("cargos", cargoService.listarAtivos());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("beneficios", beneficioService.listarTodos());

        return "rh/colaboradores/adesao/inicio";
    }

    /**
     * Endpoint REST para carregar todos os cargos ativos
     */
    @GetMapping("/api/cargos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> carregarCargos() {
        try {
            List<Map<String, Object>> cargosResponse = cargoService.listarAtivos()
                    .stream()
                    .map(cargo -> {
                        Map<String, Object> cargoMap = new HashMap<>();
                        cargoMap.put("id", cargo.getId());
                        cargoMap.put("nome", cargo.getNome());
                        return cargoMap;
                    })
                    .toList();

            logger.info("Carregando {} cargos ativos via API", cargosResponse.size());
            return ResponseEntity.ok(cargosResponse);
        } catch (Exception e) {
            logger.error("Erro ao carregar cargos via API: {}", e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * Processar dados pessoais (Etapa 1)
     */
    @PostMapping("/dados-pessoais")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processarDadosPessoais(
            @Valid @RequestBody AdesaoColaboradorDTO dadosAdesao,
            BindingResult result,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIp(request);
        String sessionId = request.getSession().getId();

        try {
            // Log dos dados recebidos para debug
            logger.info("Dados recebidos - Nome: {}, CPF: {}, Email: {}",
                    dadosAdesao.getNome(), dadosAdesao.getCpf(), dadosAdesao.getEmail());

            // Verificar rate limiting
            if (!securityService.checkRateLimit(clientIp)) {
                auditService.logRateLimitExcedido(clientIp, "/dados-pessoais", request.getHeader("User-Agent"));
                response.put("success", false);
                response.put("message", "Muitas tentativas. Tente novamente em alguns minutos.");
                return ResponseEntity.status(429).body(response);
            }

            if (result.hasErrors()) {
                Map<String, List<String>> errors = new HashMap<>();
                result.getFieldErrors().forEach(error -> {
                    String fieldName = error.getField();
                    String errorMessage = error.getDefaultMessage();

                    // Melhorar mensagens específicas
                    if ("telefone".equals(fieldName)) {
                        errorMessage = "Telefone deve estar no formato (XX) XXXXX-XXXX ou (XX) XXXX-XXXX";
                    } else if ("cpf".equals(fieldName)) {
                        errorMessage = "CPF deve estar no formato XXX.XXX.XXX-XX e ser válido";
                    } else if ("email".equals(fieldName)) {
                        errorMessage = "Email deve ter um formato válido (exemplo@dominio.com)";
                    }

                    errors.computeIfAbsent(fieldName, k -> new ArrayList<>())
                            .add(errorMessage);
                });

                response.put("success", false);
                response.put("message", "Dados inválidos. Verifique os campos destacados.");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }

            // Validar dados de entrada
            AdesaoSecurityService.ValidationResult validation = securityService.validateDadosPessoais(dadosAdesao);
            if (!validation.isValid()) {
                // Log de erros de validação
                validation.getErrors().forEach((campo, erros) -> {
                    erros.forEach(erro -> auditService.logErroValidacao(sessionId, campo, erro, clientIp));
                });

                response.put("success", false);
                response.put("message", "Dados inválidos");
                response.put("errors", validation.getErrors());
                return ResponseEntity.badRequest().body(response);
            }

            // Validar dados únicos (CPF, email)
            if (adesaoService.existeCpf(dadosAdesao.getCpf())) {
                response.put("success", false);
                response.put("message", "CPF já cadastrado no sistema");
                return ResponseEntity.badRequest().body(response);
            }

            if (adesaoService.existeEmail(dadosAdesao.getEmail())) {
                response.put("success", false);
                response.put("message", "Email já cadastrado no sistema");
                return ResponseEntity.badRequest().body(response);
            }

            // Salvar dados temporários da etapa 1
            String tempSessionId = adesaoService.salvarDadosTemporarios(dadosAdesao);

            // Salvar no workflow de aprovação
            Map<String, Object> dadosPessoaisMap = new HashMap<>();
            dadosPessoaisMap.put("nome", dadosAdesao.getNome());
            dadosPessoaisMap.put("email", dadosAdesao.getEmail());
            dadosPessoaisMap.put("cpf", dadosAdesao.getCpf());
            dadosPessoaisMap.put("cargo", dadosAdesao.getCargoId());
            dadosPessoaisMap.put("dataAdmissao",
                    dadosAdesao.getDataAdmissao() != null ? dadosAdesao.getDataAdmissao().toString() : null);
            dadosPessoaisMap.put("telefone", dadosAdesao.getTelefone());
            dadosPessoaisMap.put("sexo", dadosAdesao.getSexo());
            dadosPessoaisMap.put("estadoCivil", dadosAdesao.getEstadoCivil());

            workflowService.salvarProcesso(tempSessionId, dadosPessoaisMap);
            workflowService.atualizarEtapa(tempSessionId, "dados-pessoais");

            // Log de auditoria
            auditService.logDadosPessoaisSalvos(tempSessionId, dadosAdesao.getCpf(), dadosAdesao.getNome(), clientIp);

            response.put("success", true);
            response.put("sessionId", tempSessionId);
            response.put("proximaEtapa", "documentos");

            logger.info("Dados pessoais processados com sucesso para CPF: {} - IP: {}", dadosAdesao.getCpf(), clientIp);

        } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
            logger.error("Erro ao ler mensagem HTTP: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Erro na codificação dos dados. Verifique caracteres especiais.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Erro ao processar dados pessoais - IP: {}", clientIp, e);

            response.put("success", false);
            response.put("message", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Obter IP do cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Página de status do processo
     */
    @GetMapping("/status/{sessionId}")
    public String paginaStatus(@PathVariable String sessionId, Model model) {
        try {
            // Buscar processo no workflow
            // Por enquanto, vamos usar dados da sessão temporária
            AdesaoColaboradorDTO dadosCompletos = adesaoService.obterDadosCompletos(sessionId);
            model.addAttribute("dadosAdesao", dadosCompletos);
            model.addAttribute("sessionId", sessionId);

            return "rh/colaboradores/adesao/status";

        } catch (Exception e) {
            logger.error("Erro ao carregar status do processo: ", e);
            return "redirect:/rh/colaboradores/adesao?erro=sessao-invalida";
        }
    }

    /**
     * API: Obter status do processo
     */
    @GetMapping("/api/status/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obterStatusProcesso(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Buscar dados do processo no workflow
            // Por enquanto, retornar status básico
            response.put("success", true);
            response.put("status", "AGUARDANDO_APROVACAO");
            response.put("etapaAtual", "revisao");
            response.put("mensagem", "Processo enviado para aprovação");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao obter status do processo: ", e);
            response.put("success", false);
            response.put("message", "Erro ao obter status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Página de seleção de benefícios (Etapa 3)
     */
    @GetMapping("/beneficios")
    public String paginaBeneficios(
            @RequestParam("sessionId") String sessionId,
            Model model,
            RedirectAttributes redirectAttrs) {

        try {
            // Buscar dados do processo
            AdesaoColaboradorDTO dadosAdesao = adesaoService.obterDadosCompletos(sessionId);

            if (dadosAdesao == null) {
                logger.warn("Processo não encontrado para sessionId: {}", sessionId);
                redirectAttrs.addFlashAttribute("erro", "Processo não encontrado. Reinicie a adesão.");
                return "redirect:/rh/colaboradores/adesao";
            }

            // Verificar se a etapa anterior foi concluída
            if (!"documentos".equalsIgnoreCase(dadosAdesao.getEtapaAtual())) {
                logger.warn("Acesso inválido à etapa benefícios. Etapa atual: {}", dadosAdesao.getEtapaAtual());
                return "redirect:/rh/colaboradores/adesao/" + dadosAdesao.getEtapaAtual() + "?sessionId=" + sessionId;
            }

            model.addAttribute("dadosAdesao", dadosAdesao);
            model.addAttribute("beneficios", beneficioService.listarTodos());
            model.addAttribute("sessionId", sessionId);

            return "rh/colaboradores/adesao/beneficios"; // nome do template Thymeleaf

        } catch (Exception e) {
            logger.error("Erro ao carregar página de benefícios", e);
            redirectAttrs.addFlashAttribute("erro", "Erro ao carregar página de benefícios.");
            return "redirect:/rh/colaboradores/adesao";
        }
    }

    /**
     * Página de documentos (Etapa 2)
     */
    @GetMapping("/documentos")
    public String paginaDocumentos(@RequestParam("sessionId") String sessionId, Model model) {
        try {
            // Validar se a sessão existe
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("SessionId inválido para página de documentos");
                return "redirect:/rh/colaboradores/adesao?erro=sessao-invalida";
            }

            // Verificar se existem dados da etapa anterior
            AdesaoColaboradorDTO dadosAdesao = adesaoService.obterDadosCompletos(sessionId);
            if (dadosAdesao == null) {
                logger.warn("Dados de adesão não encontrados para sessionId: {}", sessionId);
                return "redirect:/rh/colaboradores/adesao?erro=sessao-invalida";
            }

            model.addAttribute("sessionId", sessionId);
            model.addAttribute("dadosAdesao", dadosAdesao);

            logger.info("Carregando página de documentos para sessão: {}", sessionId);
            return "rh/colaboradores/adesao/documentos";

        } catch (Exception e) {
            logger.error("Erro ao carregar página de documentos: ", e);
            return "redirect:/rh/colaboradores/adesao?erro=erro-interno";
        }
    }

    /**
     * Processar documentos (Etapa 2)
     */
    @PostMapping("/documentos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processarDocumentos(
            @RequestParam("sessionId") String sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar se todos os documentos obrigatórios foram enviados
            boolean documentosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);

            if (!documentosCompletos) {
                response.put("success", false);
                response.put("message", "Todos os documentos obrigatórios devem ser enviados");
                return ResponseEntity.badRequest().body(response);
            }

            // Atualizar workflow
            workflowService.atualizarEtapa(sessionId, "documentos");

            response.put("success", true);
            response.put("proximaEtapa", "beneficios");
            response.put("message", "Documentos processados com sucesso!");

            logger.info("Documentos processados com sucesso para sessão: {}", sessionId);

        } catch (Exception e) {
            logger.error("Erro ao processar documentos: ", e);
            response.put("success", false);
            response.put("message", "Erro ao processar documentos: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Seleção de benefícios (Etapa 3)
     */
    @PostMapping("/beneficios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processarBeneficios(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Extrair sessionId e benefícios do request
            String sessionId = (String) request.get("sessionId");
            @SuppressWarnings("unchecked")
            Map<String, Object> beneficiosSelecionados = (Map<String, Object>) request.get("beneficios");
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "SessionId é obrigatório");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (beneficiosSelecionados == null) {
                response.put("success", false);
                response.put("message", "Benefícios são obrigatórios");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar benefícios selecionados usando o serviço especializado
            List<String> erros = beneficioAdesaoService.validarSelecaoBeneficios(beneficiosSelecionados);

            if (!erros.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dados inválidos");
                response.put("errors", erros);
                return ResponseEntity.badRequest().body(response);
            }

            // Calcular custos dos benefícios
            BeneficioAdesaoService.CalculoBeneficio calculo = beneficioAdesaoService
                    .calcularCustoBeneficios(beneficiosSelecionados);

            // Atualizar workflow
            workflowService.salvarBeneficios(sessionId, beneficiosSelecionados, calculo.getCustoTotal().doubleValue());
            workflowService.atualizarEtapa(sessionId, "beneficios");

            // Processar seleção de benefícios
            adesaoService.processarBeneficios(sessionId, beneficiosSelecionados);
            
            // Marcar etapa como concluída para avançar para revisão
            adesaoService.marcarEtapaConcluida(sessionId, "beneficios");

            response.put("success", true);
            response.put("proximaEtapa", "revisao");
            response.put("calculo", calculo);

            logger.info("Benefícios processados com sucesso para sessão: {} - Custo total: R$ {}",
                    sessionId, calculo.getCustoTotal());

        } catch (Exception e) {
            logger.error("Erro ao processar benefícios: ", e);
            response.put("success", false);
            response.put("message", "Erro ao processar benefícios: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Revisão e finalização (Etapa 4)
     */
    @GetMapping("/revisao")
    public String paginaRevisao(
            @RequestParam("sessionId") String sessionId,
            Model model) {
        try {
            AdesaoColaboradorDTO dadosCompletos = adesaoService.obterDadosCompletos(sessionId);
            model.addAttribute("dadosAdesao", dadosCompletos);
            model.addAttribute("sessionId", sessionId);
            return "rh/colaboradores/adesao/revisao";
        } catch (Exception e) {
            logger.error("Erro ao carregar página de revisão: ", e);
            return "redirect:/rh/colaboradores/adesao?erro=sessao-invalida";
        }
    }

    /**
     * Finalizar processo de adesão
     */
    @PostMapping("/finalizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> finalizarAdesao(
            @RequestParam("sessionId") String sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Finalizar processo no workflow
            workflowService.finalizarProcesso(sessionId, "Processo de adesão finalizado pelo colaborador");

            // Criar colaborador definitivamente
            Colaborador colaboradorCriado = adesaoService.finalizarAdesao(sessionId);

            response.put("success", true);
            response.put("colaboradorId", colaboradorCriado.getId());
            response.put("message", "Adesão finalizada com sucesso! Aguardando aprovação.");
            response.put("redirectUrl", "/rh/colaboradores/adesao/status/" + sessionId);

            logger.info("Adesão finalizada com sucesso. Colaborador criado: ID {}, Nome: {}",
                    colaboradorCriado.getId(), colaboradorCriado.getNome());

        } catch (Exception e) {
            logger.error("Erro ao finalizar adesão: ", e);
            response.put("success", false);
            response.put("message", "Erro ao finalizar adesão: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar processo de adesão
     */
    @PostMapping("/cancelar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelarAdesao(
            @RequestParam("sessionId") String sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Cancelar no workflow
            workflowService.cancelarProcesso(sessionId, "Processo cancelado pelo usuário");

            // Limpar documentos da sessão
            documentoService.limparSessao(sessionId);

            // Cancelar adesão
            adesaoService.cancelarAdesao(sessionId);

            response.put("success", true);
            response.put("message", "Processo de adesão cancelado");

            logger.info("Processo de adesão cancelado para sessão: {}", sessionId);

        } catch (Exception e) {
            logger.error("Erro ao cancelar adesão: ", e);
            response.put("success", false);
            response.put("message", "Erro ao cancelar processo");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Listar processos de adesão em andamento
     */
    @GetMapping("/em-andamento")
    public String listarAdesoesEmAndamento(Model model) {
        model.addAttribute("adesoes", adesaoService.listarAdesoesEmAndamento());
        return "rh/colaboradores/adesao/em-andamento";
    }
}