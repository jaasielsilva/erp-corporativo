package com.jaasielsilva.portalceo.controller.rh.colaborador;

import com.jaasielsilva.portalceo.dto.AdesaoColaboradorDTO;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.service.AdesaoColaboradorService;
import com.jaasielsilva.portalceo.service.AdesaoSecurityService;
import com.jaasielsilva.portalceo.service.AuditService;
import com.jaasielsilva.portalceo.service.CargoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.BeneficioService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService;
import com.jaasielsilva.portalceo.service.DocumentoAdesaoService.DocumentoInfo;
import com.jaasielsilva.portalceo.service.BeneficioAdesaoService;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;

import jakarta.validation.Valid;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
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
    
    @Autowired
    private ColaboradorService colaboradorService;

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
                    model.addAttribute("supervisores", colaboradorService.buscarSupervisoresPotenciais());
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
        model.addAttribute("supervisores", colaboradorService.buscarSupervisoresPotenciais());

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
     * Endpoint REST para carregar todos os supervisores ativos
     */
    @GetMapping("/api/supervisores")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> carregarSupervisores() {
        try {
            List<Map<String, Object>> supervisoresResponse = colaboradorService.buscarSupervisoresPotenciais()
                    .stream()
                    .map(supervisor -> {
                        Map<String, Object> supervisorMap = new HashMap<>();
                        supervisorMap.put("id", supervisor.getId());
                        supervisorMap.put("nome", supervisor.getNome());
                        supervisorMap.put("cargo", supervisor.getCargo() != null ? supervisor.getCargo().getNome() : "");
                        return supervisorMap;
                    })
                    .toList();

            logger.info("Carregando {} supervisores ativos via API", supervisoresResponse.size());
            return ResponseEntity.ok(supervisoresResponse);
        } catch (Exception e) {
            logger.error("Erro ao carregar supervisores via API: {}", e.getMessage());
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
            logger.info("Dados recebidos - Nome: {}, CPF: {}, Email: {}, SupervisorId: {}",
                    dadosAdesao.getNome(), dadosAdesao.getCpf(), dadosAdesao.getEmail(), dadosAdesao.getSupervisorId());

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
            dadosPessoaisMap.put("supervisorId", dadosAdesao.getSupervisorId());

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
            logger.info("Iniciando processamento de documentos para sessionId: {}", sessionId);
            
            // Validar se todos os documentos obrigatórios foram enviados
            boolean documentosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);

            if (!documentosCompletos) {
                logger.warn("Documentos obrigatórios não foram enviados para sessionId: {}", sessionId);
                response.put("success", false);
                response.put("message", "Todos os documentos obrigatórios devem ser enviados");
                return ResponseEntity.badRequest().body(response);
            }

            // ✅ CORREÇÃO: Salvar documentos no workflow antes de atualizar etapa
            List<DocumentoInfo> documentosEnviados = documentoService.listarDocumentos(sessionId);
            Map<String, Object> dadosDocumentos = new HashMap<>();
            
            // Converter lista de documentos para Map
            for (DocumentoInfo doc : documentosEnviados) {
                Map<String, Object> infoDoc = new HashMap<>();
                infoDoc.put("tipo", doc.getTipo());
                infoDoc.put("nomeArquivo", doc.getNomeArquivo());
                infoDoc.put("tamanho", doc.getTamanho());
                infoDoc.put("contentType", doc.getContentType());
                infoDoc.put("caminhoArquivo", doc.getCaminhoArquivo());
                infoDoc.put("obrigatorio", doc.isObrigatorio());
                infoDoc.put("dataUpload", doc.getDataUpload());
                
                dadosDocumentos.put(doc.getTipoInterno(), infoDoc);
            }
            
            // Adicionar informações de validação
            dadosDocumentos.put("documentosCompletos", documentosCompletos);
            dadosDocumentos.put("totalDocumentos", documentosEnviados.size());
            dadosDocumentos.put("dataProcessamento", java.time.LocalDateTime.now());
            
            logger.info("Salvando {} documentos no workflow para sessionId: {}", 
                       documentosEnviados.size(), sessionId);
            
            // Salvar documentos no workflow
            workflowService.salvarDocumentos(sessionId, dadosDocumentos);

            // Atualizar workflow para próxima etapa
            workflowService.atualizarEtapa(sessionId, "documentos");

            response.put("success", true);
            response.put("proximaEtapa", "beneficios");
            response.put("message", "Documentos processados com sucesso!");
            response.put("totalDocumentos", documentosEnviados.size());

            logger.info("Documentos processados com sucesso para sessão: {} - Total: {}", 
                       sessionId, documentosEnviados.size());

        } catch (Exception e) {
            logger.error("Erro ao processar documentos para sessionId: {}", sessionId, e);
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
            
            // Atualizar processo para permitir finalização na próxima etapa
            try {
                // Atualizar para etapa de revisão no workflow
                workflowService.atualizarEtapa(sessionId, "revisao");
                logger.info("Processo atualizado para etapa de revisão - sessionId: {}", sessionId);
            } catch (Exception e) {
                logger.warn("Erro ao atualizar etapa para revisão: {}", e.getMessage());
            }

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
     * Revisão e finalização (Etapa 4) - Query Parameter
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
     * API: Obter dados para revisão - Path Parameter (para compatibilidade com frontend)
     */
    @GetMapping("/revisao/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obterDadosRevisao(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Obtendo dados para revisão - sessionId: {}", sessionId);
            
            // Verificar se a sessão existe
            if (!adesaoService.existeSessao(sessionId)) {
                response.put("success", false);
                response.put("message", "Sessão não encontrada");
                return ((BodyBuilder) ResponseEntity.notFound()).body(response);
            }
            
            // Obter dados completos
            AdesaoColaboradorDTO dadosCompletos;
            try {
                dadosCompletos = adesaoService.obterDadosCompletos(sessionId);
                logger.info("Dados completos obtidos com sucesso para sessionId: {}", sessionId);
            } catch (Exception e) {
                logger.error("Erro ao obter dados completos para sessionId {}: {}", sessionId, e.getMessage(), e);
                response.put("success", false);
                response.put("message", "Erro ao carregar dados da sessão: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Obter benefícios selecionados
            Map<String, Object> beneficiosSelecionados;
            try {
                beneficiosSelecionados = adesaoService.obterBeneficiosSessao(sessionId);
                logger.info("Benefícios da sessão obtidos: {} itens", beneficiosSelecionados.size());
            } catch (Exception e) {
                logger.warn("Erro ao obter benefícios da sessão {}: {}", sessionId, e.getMessage());
                beneficiosSelecionados = new HashMap<>();
            }
            
            // Verificar se há cálculo de benefícios
            BeneficioAdesaoService.CalculoBeneficio calculo = null;
            if (beneficiosSelecionados != null && !beneficiosSelecionados.isEmpty()) {
                try {
                    calculo = beneficioAdesaoService.calcularCustoBeneficios(beneficiosSelecionados);
                    logger.info("Cálculo de benefícios realizado: {} itens, total: R$ {}", 
                               calculo.getQuantidadeItens(), calculo.getCustoTotal());
                } catch (Exception e) {
                    logger.warn("Erro ao calcular benefícios para revisão sessionId {}: {}", sessionId, e.getMessage());
                    // Continuar sem o cálculo - não é crítico para a exibição
                }
            }
            
            // Montar dados de resposta
            Map<String, Object> dadosRevisao = new HashMap<>();
            
            // Dados pessoais - usando null-safe accessors
            Map<String, Object> dadosPessoaisMap = new HashMap<>();
            dadosPessoaisMap.put("nome", dadosCompletos.getNome() != null ? dadosCompletos.getNome() : "");
            dadosPessoaisMap.put("cpf", dadosCompletos.getCpf() != null ? dadosCompletos.getCpf() : "");
            dadosPessoaisMap.put("email", dadosCompletos.getEmail() != null ? dadosCompletos.getEmail() : "");
            dadosPessoaisMap.put("telefone", dadosCompletos.getTelefone() != null ? dadosCompletos.getTelefone() : "");
            dadosPessoaisMap.put("dataNascimento", dadosCompletos.getDataNascimento() != null ? dadosCompletos.getDataNascimento().toString() : "");
            dadosPessoaisMap.put("cargo", dadosCompletos.getCargoId() != null ? dadosCompletos.getCargoId().toString() : "");
            dadosPessoaisMap.put("departamento", dadosCompletos.getDepartamentoId() != null ? dadosCompletos.getDepartamentoId().toString() : "");
            dadosPessoaisMap.put("dataAdmissao", dadosCompletos.getDataAdmissao() != null ? dadosCompletos.getDataAdmissao().toString() : "");
            dadosPessoaisMap.put("salario", dadosCompletos.getSalario() != null ? dadosCompletos.getSalario().doubleValue() : 0.0);
            
            // Construir endereço completo de forma segura
            StringBuilder endereco = new StringBuilder();
            if (dadosCompletos.getLogradouro() != null) endereco.append(dadosCompletos.getLogradouro());
            if (dadosCompletos.getNumero() != null) endereco.append(", ").append(dadosCompletos.getNumero());
            if (dadosCompletos.getBairro() != null) endereco.append(" - ").append(dadosCompletos.getBairro());
            if (dadosCompletos.getCidade() != null) endereco.append(", ").append(dadosCompletos.getCidade());
            if (dadosCompletos.getEstado() != null) endereco.append("/").append(dadosCompletos.getEstado());
            if (dadosCompletos.getCep() != null) endereco.append(" - CEP: ").append(dadosCompletos.getCep());
            
            dadosPessoaisMap.put("endereco", endereco.toString());
            dadosRevisao.put("dadosPessoais", dadosPessoaisMap);
            
            // Documentos (mock - pode ser implementado depois)
            dadosRevisao.put("documentos", List.of());
            
            // Benefícios e cálculo
            dadosRevisao.put("beneficios", beneficiosSelecionados != null ? beneficiosSelecionados : Map.of());
            if (calculo != null) {
                dadosRevisao.put("beneficios_calculo", Map.of(
                    "itens", calculo.getItens(),
                    "custoTotal", calculo.getCustoTotal(),
                    "quantidadeItens", calculo.getQuantidadeItens()
                ));
            }
            
            response.put("success", true);
            response.put("data", dadosRevisao);
            
            logger.info("Dados de revisão carregados com sucesso para sessionId: {}", sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter dados para revisão - sessionId: {}", sessionId, e);
            response.put("success", false);
            response.put("message", "Erro ao carregar dados para revisão: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Listar processos de adesão em andamento
     */
    @GetMapping("/em-andamento")
    public String listarAdesoesEmAndamento(Model model) {
        model.addAttribute("adesoes", adesaoService.listarAdesoesEmAndamento());
        return "rh/colaboradores/adesao/em-andamento";
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
            logger.info("Iniciando finalização de adesão para sessionId: {}", sessionId);
            
            // Verificar se a sessão existe
            if (!adesaoService.existeSessao(sessionId)) {
                response.put("success", false);
                response.put("message", "Sessão não encontrada");
                return ((BodyBuilder) ResponseEntity.notFound()).body(response);
            }
            
            // ✅ CORREÇÃO: Validar documentos usando o serviço correto ao invés do DTO
            boolean documentosCompletos = documentoService.verificarDocumentosObrigatorios(sessionId);
            if (!documentosCompletos) {
                logger.warn("Tentativa de finalização sem documentos completos - sessionId: {}", sessionId);
                response.put("success", false);
                response.put("message", "Documentos obrigatórios não foram enviados. Volte à etapa de documentos e envie todos os arquivos necessários.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // ✅ CORREÇÃO: Atualizar o DTO com o estado real dos documentos antes de finalizar
            AdesaoColaboradorDTO dadosAdesao = adesaoService.obterDadosCompletos(sessionId);
            sincronizarDocumentosNoDTO(dadosAdesao, sessionId);
            
            // ✅ CORREÇÃO: Salvar os dados atualizados de volta no cache temporário
            adesaoService.atualizarDadosTemporarios(sessionId, dadosAdesao);
            
            // Tentar garantir que o processo esteja no estado correto antes de finalizar
            try {
                // Verificar se já está na etapa de revisão, se não, atualizar
                ProcessoAdesao processo = workflowService.buscarProcessoPorSessionId(sessionId);
                
                if (!"revisao".equals(processo.getEtapaAtual())) {
                    logger.info("Processo não está na etapa de revisão (atual: {}), atualizando...", 
                               processo.getEtapaAtual());
                    workflowService.atualizarEtapa(sessionId, "revisao");
                }
                
                // Se o status não for EM_ANDAMENTO, atualizar para permitir finalização
                if (processo.getStatus() != ProcessoAdesao.StatusProcesso.EM_ANDAMENTO) {
                    logger.info("Processo não está EM_ANDAMENTO (status atual: {}), corrigindo...", 
                               processo.getStatus());
                    processo.setStatus(ProcessoAdesao.StatusProcesso.EM_ANDAMENTO);
                    // Salvar através do repository diretamente se necessário
                }
                
            } catch (Exception e) {
                logger.warn("Erro ao verificar/corrigir estado do processo: {}", e.getMessage());
            }
            
            // Finalizar processo no workflow
            workflowService.finalizarProcesso(sessionId, "Processo de adesão finalizado pelo colaborador");

            // Criar colaborador definitivamente
            Colaborador colaboradorCriado = adesaoService.finalizarAdesao(sessionId);

            response.put("success", true);
            response.put("colaboradorId", colaboradorCriado.getId());
            response.put("message", "Adesão finalizada com sucesso! Aguardando aprovação.");
            response.put("protocolo", sessionId); // Usar sessionId como protocolo temporário
            response.put("redirectUrl", "/rh/colaboradores/adesao/status/" + sessionId);

            logger.info("Adesão finalizada com sucesso. Colaborador criado: ID {}, Nome: {}",
                    colaboradorCriado.getId(), colaboradorCriado.getNome());

        } catch (WorkflowAdesaoService.WorkflowException e) {
            logger.error("Erro de workflow ao finalizar adesão: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erro ao finalizar adesão: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Erro ao finalizar adesão para sessionId: {}", sessionId, e);
            response.put("success", false);
            response.put("message", "Erro ao finalizar adesão: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }
    
    /**
     * Sincroniza o estado dos documentos no DTO com o estado real do sistema
     */
    private void sincronizarDocumentosNoDTO(AdesaoColaboradorDTO dadosAdesao, String sessionId) {
        try {
            List<DocumentoInfo> documentosEnviados = documentoService.listarDocumentos(sessionId);
            
            logger.info("=== SINCRONIZAÇÃO DE DOCUMENTOS ====");
            logger.info("SessionId: {}", sessionId);
            logger.info("Documentos encontrados no sistema: {}", documentosEnviados.size());
            
            // Inicializar mapas se não existirem
            if (dadosAdesao.getDocumentosUpload() == null) {
                dadosAdesao.setDocumentosUpload(new java.util.HashMap<>());
            }
            if (dadosAdesao.getStatusDocumentos() == null) {
                dadosAdesao.setStatusDocumentos(new java.util.HashMap<>());
            }
            
            // Limpar estado anterior
            dadosAdesao.getDocumentosUpload().clear();
            dadosAdesao.getStatusDocumentos().clear();
            
            // Mapear documentos do sistema atual para o formato esperado pelo DTO
            for (DocumentoInfo doc : documentosEnviados) {
                String tipoDto = mapearTipoDocumentoParaDTO(doc.getTipo());
                dadosAdesao.getDocumentosUpload().put(tipoDto, doc.getCaminhoArquivo());
                dadosAdesao.getStatusDocumentos().put(tipoDto, true);
                
                logger.info("Documento mapeado: {} -> {} (caminho: {})", 
                           doc.getTipo(), tipoDto, doc.getCaminhoArquivo());
            }
            
            // Certificar que todos os documentos obrigatórios estão marcados como falso se não foram enviados
            for (String docObrigatorio : dadosAdesao.getDocumentosObrigatorios()) {
                if (!dadosAdesao.getStatusDocumentos().containsKey(docObrigatorio)) {
                    dadosAdesao.getStatusDocumentos().put(docObrigatorio, false);
                    logger.info("Documento obrigatório não encontrado, marcado como false: {}", docObrigatorio);
                }
            }
            
            logger.info("DTO sincronizado com {} documentos para sessionId: {}", 
                       documentosEnviados.size(), sessionId);
            logger.info("Documentos obrigatórios no DTO: {}", dadosAdesao.getDocumentosObrigatorios());
            logger.info("Status final dos documentos: {}", dadosAdesao.getStatusDocumentos());
            logger.info("DocumentosUpload final: {}", dadosAdesao.getDocumentosUpload());
            logger.info("Pode finalizar? {}", dadosAdesao.isDocumentosObrigatoriosCompletos());
            logger.info("====================================");
                       
        } catch (Exception e) {
            logger.error("Erro ao sincronizar documentos no DTO para sessionId: {}", sessionId, e);
        }
    }
    
    /**
     * Mapeia os tipos de documento do sistema atual para o formato esperado pelo DTO
     */
    private String mapearTipoDocumentoParaDTO(String tipoSistema) {
        return switch (tipoSistema.toLowerCase()) {
            case "rg" -> "RG";
            case "cpf" -> "CPF";
            case "comprovante de endereço" -> "Comprovante de Endereço";
            case "carteira de trabalho" -> "Carteira de Trabalho";
            case "título de eleitor" -> "Título de Eleitor";
            case "certificado de reservista" -> "Certificado de Reservista";
            case "comprovante de escolaridade" -> "Comprovante de Escolaridade";
            case "certidão de nascimento/casamento" -> "Certidão de Nascimento/Casamento";
            default -> tipoSistema; // Retorna o tipo original se não houver mapeamento
        };
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
     * Debug endpoint to check session status
     */
    @GetMapping("/debug/session/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugSession(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("=== DEBUG SESSION {} ===", sessionId);
            
            // Check 1: Session exists
            boolean sessionExists = adesaoService.existeSessao(sessionId);
            response.put("sessionExists", sessionExists);
            logger.info("Session exists: {}", sessionExists);
            
            if (!sessionExists) {
                response.put("success", true);
                response.put("message", "Session does not exist");
                return ResponseEntity.ok(response);
            }
            
            // Check 2: Get complete data
            try {
                AdesaoColaboradorDTO dados = adesaoService.obterDadosCompletos(sessionId);
                response.put("hasCompleteData", true);
                response.put("currentStage", dados.getEtapaAtual());
                response.put("processStatus", dados.getStatusProcesso());
                response.put("hasName", dados.getNome() != null);
                response.put("hasEmail", dados.getEmail() != null);
                response.put("hasCpf", dados.getCpf() != null);
                logger.info("Complete data - Stage: {}, Status: {}, Name: {}", 
                           dados.getEtapaAtual(), dados.getStatusProcesso(), dados.getNome());
            } catch (Exception e) {
                response.put("hasCompleteData", false);
                response.put("completeDataError", e.getMessage());
                logger.error("Error getting complete data: {}", e.getMessage(), e);
            }
            
            // Check 3: Benefits data
            try {
                Map<String, Object> benefits = adesaoService.obterBeneficiosSessao(sessionId);
                response.put("hasBenefits", benefits != null && !benefits.isEmpty());
                response.put("benefitsCount", benefits != null ? benefits.size() : 0);
                logger.info("Benefits data - Count: {}", benefits != null ? benefits.size() : 0);
            } catch (Exception e) {
                response.put("hasBenefits", false);
                response.put("benefitsError", e.getMessage());
                logger.error("Error getting benefits: {}", e.getMessage());
            }
            
            // Check 4: Workflow status
            try {
                ProcessoAdesao processo = workflowService.buscarProcessoPorSessionId(sessionId);
                response.put("hasWorkflowProcess", true);
                response.put("workflowStage", processo.getEtapaAtual());
                response.put("workflowStatus", processo.getStatus().toString());
                response.put("isFinalizeable", processo.isFinalizavel());
                logger.info("Workflow - Stage: {}, Status: {}, Finalizeable: {}", 
                           processo.getEtapaAtual(), processo.getStatus(), processo.isFinalizavel());
            } catch (Exception e) {
                response.put("hasWorkflowProcess", false);
                response.put("workflowError", e.getMessage());
                logger.error("Error getting workflow: {}", e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "Debug completed");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in debug endpoint for sessionId {}: {}", sessionId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Debug failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }}