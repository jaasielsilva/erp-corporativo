package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ContratoLegalService;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.juridico.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/juridico")
@RequiredArgsConstructor
public class JuridicoController {

    private final ContratoLegalService contratoLegalService;
    private final UsuarioRepository usuarioRepository;
    private final com.jaasielsilva.portalceo.service.juridico.ProcessoJuridicoService processoJuridicoService;
    // Repositórios jurídicos (Processos, Compliance, Documentos)
    private final ProcessoJuridicoRepository processoJuridicoRepository;
    private final AudienciaRepository audienciaRepository;
    private final PrazoJuridicoRepository prazoJuridicoRepository;
    private final AndamentoProcessoRepository andamentoProcessoRepository;
    private final NormaRepository normaRepository;
    private final NaoConformidadeRepository naoConformidadeRepository;
    private final AuditoriaComplianceRepository auditoriaComplianceRepository;
    private final DocumentoJuridicoRepository documentoJuridicoRepository;

    // Página principal do Jurídico
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Jurídico - Dashboard");
        model.addAttribute("moduleCSS", "juridico");
        
        // Estatísticas do dashboard (dados reais quando disponíveis)
        Map<ContratoLegal.StatusContrato, Long> estatisticasStatus = contratoLegalService.getEstatisticasPorStatus();
        long ativos = estatisticasStatus.getOrDefault(ContratoLegal.StatusContrato.ATIVO, 0L);
        model.addAttribute("contratosAtivos", ativos);

        model.addAttribute("processosAndamento", processoJuridicoService.contarProcessosEmAndamento());
        
        // Prazos/vencimentos a partir de contratos com vencimento próximo (próximos 30 dias)
        List<ContratoLegal> proximosVencimentos = contratoLegalService.buscarContratosVencendoEm(30);
        model.addAttribute("prazosVencendo", proximosVencimentos != null ? proximosVencimentos.size() : 0);

        // Alertas de compliance permanecem como placeholder até integração
        model.addAttribute("alertasCompliance", getAlertasCompliance());

        // Contratos próximos ao vencimento (renderização simplificada)
        List<Map<String, Object>> contratosVencimentoVm = new ArrayList<>();
        if (proximosVencimentos != null) {
            for (ContratoLegal c : proximosVencimentos) {
                Map<String, Object> item = new HashMap<>();
                item.put("titulo", c.getTitulo());
                // usar nome da contraparte quando disponível
                item.put("contraparte", c.getNomeContraparte());
                item.put("dataVencimento", c.getDataVencimento());
                item.put("valor", c.getValorContrato() != null ? c.getValorContrato() : c.getValorMensal());
                item.put("status", c.getStatus());
                contratosVencimentoVm.add(item);
            }
        }
        model.addAttribute("contratosVencimento", contratosVencimentoVm);
        
        // Processos com prazos urgentes
        model.addAttribute("processosUrgentes", processoJuridicoService.obterProcessosUrgentes(7));
        
        // Últimas atividades
        model.addAttribute("ultimasAtividades", processoJuridicoService.obterUltimasAtividades(10));
        
        return "juridico/index";
    }

    // =============== CONTRATOS JURÍDICOS ===============
    
    @GetMapping("/contratos")
    public String contratos(
            @RequestParam(value = "status", required = false) ContratoLegal.StatusContrato status,
            @RequestParam(value = "tipo", required = false) ContratoLegal.TipoContrato tipo,
            @RequestParam(value = "numero", required = false) String numero,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "dataInicio") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {
        model.addAttribute("pageTitle", "Gestão de Contratos");
        model.addAttribute("moduleCSS", "juridico");

        try {
            Set<String> camposPermitidos = Set.of("dataInicio", "dataFim", "dataVencimento", "valorMensal", "valorContrato");
            if (!camposPermitidos.contains(sortBy)) {
                sortBy = "dataInicio";
            }
            Sort.Direction direction;
            try {
                direction = Sort.Direction.fromString(sortDir);
            } catch (IllegalArgumentException e) {
                direction = Sort.Direction.ASC;
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<ContratoLegal> contratosPage = contratoLegalService.buscarContratosComFiltros(numero, status, tipo, pageable);

            model.addAttribute("listaContratos", contratosPage.getContent());
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("totalElements", contratosPage.getTotalElements());
            model.addAttribute("totalPages", contratosPage.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", direction.isAscending() ? "asc" : "desc");

            model.addAttribute("statusContrato", ContratoLegal.StatusContrato.values());
            model.addAttribute("tiposContrato", ContratoLegal.TipoContrato.values());

            // Estatísticas reais de contratos
            model.addAttribute("estatisticasStatus", contratoLegalService.getEstatisticasPorStatus());
            model.addAttribute("valorTotalAtivos", contratoLegalService.calcularValorTotalAtivos());
            model.addAttribute("receitaMensal", contratoLegalService.calcularReceitaMensalRecorrente());
        } catch (Exception e) {
            // Fallback seguro para evitar quebras de renderização
            model.addAttribute("listaContratos", java.util.Collections.emptyList());
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("totalElements", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("statusContrato", ContratoLegal.StatusContrato.values());
            model.addAttribute("tiposContrato", ContratoLegal.TipoContrato.values());
            model.addAttribute("estatisticasStatus", java.util.Collections.emptyMap());
            model.addAttribute("valorTotalAtivos", java.math.BigDecimal.ZERO);
            model.addAttribute("receitaMensal", java.math.BigDecimal.ZERO);
            model.addAttribute("erroCarregamento", e.getMessage());
        }

        return "juridico/contratos";
    }

    @GetMapping("/contratos/{id}")
    public String contratoDetalhe(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Detalhes do Contrato");
        model.addAttribute("moduleCSS", "juridico");
        model.addAttribute("contratoId", id);
        return "juridico/contrato-detalhe";
    }
    
    @PostMapping("/contratos")
    @ResponseBody
    public ResponseEntity<?> criarContrato(@RequestParam String titulo,
                                           @RequestParam ContratoLegal.TipoContrato tipo,
                                           @RequestParam(required = false) String descricao,
                                           @RequestParam String dataInicio,
                                           @RequestParam(required = false, defaultValue = "12") Integer duracaoMeses,
                                           @RequestParam(required = false) String valorMensal,
                                           @RequestParam(required = false) String valorContrato,
                                           @RequestParam(required = false, defaultValue = "false") Boolean renovacaoAutomatica,
                                           @RequestParam(required = false, defaultValue = "30") Integer prazoNotificacao,
                                           @RequestParam(required = false) String numeroContrato,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Obter usuário logado
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);

            // Mapear dados do contrato
            ContratoLegal contrato = new ContratoLegal();
            contrato.setTitulo(titulo);
            contrato.setTipo(tipo);
            contrato.setDescricao(descricao);
            contrato.setDataInicio(LocalDate.parse(dataInicio));
            contrato.setDuracaoMeses(duracaoMeses);
            contrato.setRenovacaoAutomatica(Boolean.TRUE.equals(renovacaoAutomatica));
            contrato.setPrazoNotificacao(prazoNotificacao);

            if (numeroContrato != null && !numeroContrato.isBlank()) {
                contrato.setNumeroContrato(numeroContrato.trim());
            }

            if (valorMensal != null && !valorMensal.isBlank()) {
                String vm = valorMensal.replace(".", "").replace(",", ".");
                contrato.setValorMensal(new BigDecimal(vm));
            }
            if (valorContrato != null && !valorContrato.isBlank()) {
                String vc = valorContrato.replace(".", "").replace(",", ".");
                contrato.setValorContrato(new BigDecimal(vc));
            }

            // Associar usuário
            contrato.setUsuarioCriacao(usuario);
            contrato.setUsuarioResponsavel(usuario);

            // Persistir usando service (gera número se vazio e cria alertas)
            ContratoLegal salvo = contratoLegalService.salvarContrato(contrato);

            Map<String, Object> payload = new HashMap<>();
            payload.put("id", salvo.getId());
            payload.put("numeroContrato", salvo.getNumeroContrato());
            payload.put("status", salvo.getStatus());
            payload.put("titulo", salvo.getTitulo());
            payload.put("tipo", salvo.getTipo());
            payload.put("dataInicio", salvo.getDataInicio());
            payload.put("valorMensal", salvo.getValorMensal());
            payload.put("valorContrato", salvo.getValorContrato());

            return ResponseEntity.ok(payload);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao criar contrato", "detalhes", e.getMessage()));
        }
    }

    // =============== PROCESSOS JURÍDICOS ===============
    
    @GetMapping("/processos")
    public String processos(Model model) {
        model.addAttribute("pageTitle", "Processos Jurídicos");
        model.addAttribute("moduleCSS", "juridico");
        
        model.addAttribute("processosAndamento", processoJuridicoService.listarPorStatus(com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.EM_ANDAMENTO));
        model.addAttribute("processosSuspensos", processoJuridicoService.listarPorStatus(com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.SUSPENSO));
        model.addAttribute("processosEncerrados", processoJuridicoService.listarPorStatus(com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.ENCERRADO));
        
        // Próximas audiências
        model.addAttribute("proximasAudiencias", processoJuridicoService.obterProximasAudiencias(30));
        
        // Prazos críticos
        model.addAttribute("prazosCriticos", processoJuridicoService.obterPrazosCriticos(7));
        
        return "juridico/processos";
    }
    
    @PostMapping("/processos")
    @ResponseBody
    public ResponseEntity<?> criarProcesso(@RequestParam String numero,
                                          @RequestParam String tipo,
                                          @RequestParam String tribunal,
                                          @RequestParam String parte,
                                          @RequestParam String assunto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        // Simular criação de processo
        Map<String, Object> processo = new HashMap<>();
        processo.put("id", System.currentTimeMillis());
        processo.put("numero", numero);
        processo.put("tipo", tipo);
        processo.put("tribunal", tribunal);
        processo.put("parte", parte);
        processo.put("assunto", assunto);
        processo.put("status", "EM_ANDAMENTO");
        processo.put("responsavel", userDetails.getUsername());
        processo.put("dataAbertura", LocalDateTime.now());
        
        return ResponseEntity.ok(processo);
    }

    // =============== COMPLIANCE ===============
    
    @GetMapping("/compliance")
    public String compliance(Model model) {
        model.addAttribute("pageTitle", "Compliance e Conformidade");
        model.addAttribute("moduleCSS", "juridico");
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        long totalNc = naoConformidadeRepository.count();
        long totalAud = auditoriaComplianceRepository.count();
        java.time.LocalDate ultimaAuditoria = auditoriaComplianceRepository.findAll().stream()
                .map(a -> a.getDataInicio())
                .filter(java.util.Objects::nonNull)
                .max(java.util.Comparator.naturalOrder())
                .orElse(null);
        long resolvidas = naoConformidadeRepository.findAll().stream()
                .filter(nc -> nc.isResolvida())
                .count();
        double conformidade = totalNc == 0 ? 100.0 : Math.round(100.0 * resolvidas / totalNc);
        status.put("conformidade", conformidade);
        status.put("naoConformidades", totalNc);
        status.put("auditoriasPendentes", totalAud);
        status.put("ultimaAuditoria", ultimaAuditoria);
        model.addAttribute("statusCompliance", status);
        return "juridico/compliance";
    }
    
    @PostMapping("/compliance/auditoria")
    @ResponseBody
    public ResponseEntity<?> criarAuditoria(@RequestParam String tipo,
                                           @RequestParam String escopo,
                                           @RequestParam String dataInicio,
                                           @RequestParam String auditor,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // Simular criação de auditoria
        Map<String, Object> auditoria = new HashMap<>();
        auditoria.put("id", System.currentTimeMillis());
        auditoria.put("tipo", tipo);
        auditoria.put("escopo", escopo);
        auditoria.put("auditor", auditor);
        auditoria.put("status", "PLANEJADA");
        auditoria.put("responsavel", userDetails.getUsername());
        auditoria.put("dataCriacao", LocalDateTime.now());
        
        return ResponseEntity.ok(auditoria);
    }

    // =============== DOCUMENTOS JURÍDICOS ===============
    
    @GetMapping("/documentos")
    public String documentos(Model model) {
        model.addAttribute("pageTitle", "Biblioteca de Documentos");
        model.addAttribute("moduleCSS", "juridico");
        java.util.List<com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico> todos = documentoJuridicoRepository.findAll();
        java.util.List<java.util.Map<String, Object>> categorias = todos.stream()
                .collect(java.util.stream.Collectors.groupingBy(d -> {
                    String c = d.getCategoria();
                    return c != null && !c.isBlank() ? c : "—";
                }, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("nome", e.getKey());
                    m.put("quantidade", e.getValue());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("categoriasDocumentos", categorias);

        org.springframework.data.domain.Pageable pr = org.springframework.data.domain.PageRequest.of(
                0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "criadoEm"));
        java.util.List<java.util.Map<String, Object>> recentes = documentoJuridicoRepository.findAll(pr).getContent().stream()
                .map(d -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("titulo", d.getTitulo());
                    m.put("categoria", d.getCategoria());
                    m.put("dataUpload", d.getCriadoEm());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("documentosRecentes", recentes);

        model.addAttribute("modelosDocumentos", getModelosDocumentos());
        model.addAttribute("documentosPendentes", getDocumentosPendentesAssinatura());
        return "juridico/documentos";
    }
    
    @PostMapping("/documentos/upload")
    @ResponseBody
    public ResponseEntity<?> uploadDocumento(@RequestParam String titulo,
                                            @RequestParam String categoria,
                                            @RequestParam String descricao,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        // Simular upload de documento
        Map<String, Object> documento = new HashMap<>();
        documento.put("id", System.currentTimeMillis());
        documento.put("titulo", titulo);
        documento.put("categoria", categoria);
        documento.put("descricao", descricao);
        documento.put("status", "ATIVO");
        documento.put("autor", userDetails.getUsername());
        documento.put("dataUpload", LocalDateTime.now());
        
        return ResponseEntity.ok(documento);
    }

    // =============== APIs ===============
    
    @GetMapping("/api/dashboard/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEstatisticasDashboard() {
        Map<String, Object> estatisticas = new HashMap<>();
        Map<ContratoLegal.StatusContrato, Long> estatisticasStatus = contratoLegalService.getEstatisticasPorStatus();
        long ativos = estatisticasStatus.getOrDefault(ContratoLegal.StatusContrato.ATIVO, 0L);
        estatisticas.put("contratosAtivos", ativos);
        estatisticas.put("processosAndamento", processoJuridicoService.contarProcessosEmAndamento());
        List<ContratoLegal> proximos = contratoLegalService.buscarContratosVencendoEm(30);
        estatisticas.put("prazosVencendo", proximos != null ? proximos.size() : 0);
        estatisticas.put("alertasCompliance", getAlertasCompliance());
        estatisticas.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/api/contratos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarContratosApi(
            @RequestParam(value = "status", required = false) ContratoLegal.StatusContrato status,
            @RequestParam(value = "tipo", required = false) ContratoLegal.TipoContrato tipo,
            @RequestParam(value = "numero", required = false) String numero,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "dataInicio") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        // Campos permitidos para ordenação
        Set<String> camposPermitidos = Set.of("dataInicio", "dataFim", "dataVencimento", "valorMensal", "valorContrato");
        if (!camposPermitidos.contains(sortBy)) {
            sortBy = "dataInicio";
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ContratoLegal> contratosPage = contratoLegalService.buscarContratosComFiltros(numero, status, tipo, pageable);
        List<com.jaasielsilva.portalceo.dto.ContratoLegalDTO> content = com.jaasielsilva.portalceo.mapper.ContratoLegalMapper.toDtoList(contratosPage.getContent());

        Map<String, Object> payload = new HashMap<>();
        payload.put("content", content);
        payload.put("totalElements", contratosPage.getTotalElements());
        payload.put("page", contratosPage.getNumber());
        payload.put("size", contratosPage.getSize());
        payload.put("totalPages", contratosPage.getTotalPages());
        payload.put("sortBy", sortBy);
        payload.put("sortDir", direction.isAscending() ? "asc" : "desc");

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/contratos/{id}")
    @ResponseBody
    public ResponseEntity<?> obterContratoPorId(@PathVariable Long id) {
        try {
            ContratoLegal contrato = contratoLegalService.buscarPorId(id);
            return ResponseEntity.ok(
                    com.jaasielsilva.portalceo.mapper.ContratoLegalMapper.toDto(contrato)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("erro", "Contrato não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao obter contrato", "detalhes", e.getMessage()));
        }
    }

    @PostMapping("/api/contratos")
    @ResponseBody
    public ResponseEntity<?> criarContratoApi(@RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);

            ContratoLegal contrato = new ContratoLegal();
            contrato.setTitulo(String.valueOf(body.getOrDefault("titulo", "")));
            Object tipoObj = body.get("tipo");
            if (tipoObj != null) {
                contrato.setTipo(ContratoLegal.TipoContrato.valueOf(String.valueOf(tipoObj)));
            }
            contrato.setDescricao(String.valueOf(body.getOrDefault("descricao", "")));
            Object dataInicioObj = body.get("dataInicio");
            if (dataInicioObj != null) {
                contrato.setDataInicio(LocalDate.parse(String.valueOf(dataInicioObj)));
            }
            Object duracaoObj = body.get("duracaoMeses");
            if (duracaoObj != null) {
                contrato.setDuracaoMeses(Integer.valueOf(String.valueOf(duracaoObj)));
            }
            Object renovacaoObj = body.get("renovacaoAutomatica");
            if (renovacaoObj != null) {
                contrato.setRenovacaoAutomatica(Boolean.parseBoolean(String.valueOf(renovacaoObj)));
            }
            Object prazoNotifObj = body.get("prazoNotificacao");
            if (prazoNotifObj != null) {
                contrato.setPrazoNotificacao(Integer.valueOf(String.valueOf(prazoNotifObj)));
            }
            Object numeroContratoObj = body.get("numeroContrato");
            if (numeroContratoObj != null) {
                String num = String.valueOf(numeroContratoObj);
                if (!num.isBlank()) contrato.setNumeroContrato(num.trim());
            }
            Object valorMensalObj = body.get("valorMensal");
            if (valorMensalObj != null) {
                String vm = String.valueOf(valorMensalObj).replace(".", "").replace(",", ".");
                contrato.setValorMensal(new BigDecimal(vm));
            }
            Object valorContratoObj = body.get("valorContrato");
            if (valorContratoObj != null) {
                String vc = String.valueOf(valorContratoObj).replace(".", "").replace(",", ".");
                contrato.setValorContrato(new BigDecimal(vc));
            }

            contrato.setUsuarioCriacao(usuario);
            contrato.setUsuarioResponsavel(usuario);

            ContratoLegal salvo = contratoLegalService.salvarContrato(contrato);
            return ResponseEntity.ok(Map.of(
                    "id", salvo.getId(),
                    "numeroContrato", salvo.getNumeroContrato(),
                    "status", salvo.getStatus(),
                    "titulo", salvo.getTitulo(),
                    "tipo", salvo.getTipo()
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao criar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/api/contratos/{id}")
    @ResponseBody
    public ResponseEntity<?> atualizarContratoApi(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> body) {
        try {
            ContratoLegal contrato;
            try {
                contrato = contratoLegalService.buscarPorId(id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(404).body(Map.of("erro", "Contrato não encontrado"));
            }

            if (body.containsKey("titulo")) contrato.setTitulo(String.valueOf(body.get("titulo")));
            if (body.containsKey("descricao")) contrato.setDescricao(String.valueOf(body.get("descricao")));
            if (body.containsKey("tipo") && body.get("tipo") != null)
                contrato.setTipo(ContratoLegal.TipoContrato.valueOf(String.valueOf(body.get("tipo"))));
            if (body.containsKey("dataInicio") && body.get("dataInicio") != null)
                contrato.setDataInicio(LocalDate.parse(String.valueOf(body.get("dataInicio"))));
            if (body.containsKey("duracaoMeses") && body.get("duracaoMeses") != null)
                contrato.setDuracaoMeses(Integer.valueOf(String.valueOf(body.get("duracaoMeses"))));
            if (body.containsKey("renovacaoAutomatica"))
                contrato.setRenovacaoAutomatica(Boolean.parseBoolean(String.valueOf(body.get("renovacaoAutomatica"))));
            if (body.containsKey("prazoNotificacao") && body.get("prazoNotificacao") != null)
                contrato.setPrazoNotificacao(Integer.valueOf(String.valueOf(body.get("prazoNotificacao"))));
            if (body.containsKey("numeroContrato") && body.get("numeroContrato") != null) {
                String num = String.valueOf(body.get("numeroContrato"));
                contrato.setNumeroContrato(num != null ? num.trim() : null);
            }
            if (body.containsKey("valorMensal") && body.get("valorMensal") != null) {
                String vm = String.valueOf(body.get("valorMensal")).replace(".", "").replace(",", ".");
                contrato.setValorMensal(new BigDecimal(vm));
            }
            if (body.containsKey("valorContrato") && body.get("valorContrato") != null) {
                String vc = String.valueOf(body.get("valorContrato")).replace(".", "").replace(",", ".");
                contrato.setValorContrato(new BigDecimal(vc));
            }

            ContratoLegal atualizado = contratoLegalService.salvarContrato(contrato);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus()
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao atualizar contrato", "detalhes", e.getMessage()));
        }
    }

    @DeleteMapping("/api/contratos/{id}")
    @ResponseBody
    public ResponseEntity<?> excluirContratoApi(@PathVariable Long id) {
        try {
            contratoLegalService.deleteById(id);
            return ResponseEntity.ok(Map.of("mensagem", "Contrato excluído com sucesso"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao excluir contrato", "detalhes", e.getMessage()));
        }
    }

    @GetMapping("/api/contratos/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> estatisticasContratosApi() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("porStatus", contratoLegalService.getEstatisticasPorStatus());
        payload.put("porTipo", contratoLegalService.getEstatisticasPorTipo());
        payload.put("valorTotalAtivos", contratoLegalService.calcularValorTotalAtivos());
        payload.put("receitaMensalRecorrente", contratoLegalService.calcularReceitaMensalRecorrente());
        List<ContratoLegal> proximos = contratoLegalService.buscarContratosVencendoEm(30);
        payload.put("proximosVencimentos", proximos);
        payload.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(payload);
    }

    // =============== APIs de Processos ===============
    @GetMapping("/api/processos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarProcessosApi(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico> processos = processoJuridicoRepository.findAll(pageable);
        List<Map<String, Object>> content = new ArrayList<>();
        for (var p : processos.getContent()) {
            if (status != null && !status.isBlank() && p.getStatus() != null && !p.getStatus().name().equalsIgnoreCase(status)) {
                continue;
            }
            if (search != null && !search.isBlank()) {
                String texto = ((p.getNumero() == null ? "" : p.getNumero()) + " " +
                        (p.getParte() == null ? "" : p.getParte()) + " " +
                        (p.getAssunto() == null ? "" : p.getAssunto())).toLowerCase();
                if (!texto.contains(search.toLowerCase())) continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("numero", p.getNumero());
            item.put("tipo", p.getTipo());
            item.put("tribunal", p.getTribunal());
            item.put("parte", p.getParte());
            item.put("assunto", p.getAssunto());
            item.put("status", p.getStatus());
            item.put("dataAbertura", p.getDataAbertura());
            content.add(item);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", content);
        payload.put("totalElements", processos.getTotalElements());
        payload.put("page", processos.getNumber());
        payload.put("size", processos.getSize());
        payload.put("totalPages", processos.getTotalPages());
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/processos/{id}")
    @ResponseBody
    public ResponseEntity<?> obterProcessoPorId(@PathVariable Long id) {
        return processoJuridicoRepository.findById(id)
                .map(p -> ResponseEntity.ok(Map.of(
                        "id", p.getId(),
                        "numero", p.getNumero(),
                        "tipo", p.getTipo(),
                        "tribunal", p.getTribunal(),
                        "parte", p.getParte(),
                        "assunto", p.getAssunto(),
                        "status", p.getStatus(),
                        "dataAbertura", p.getDataAbertura()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Processo não encontrado")));
    }

    @PostMapping("/api/processos")
    @ResponseBody
    public ResponseEntity<?> criarProcessoApi(@RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico p = new com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico();
        p.setNumero(String.valueOf(body.getOrDefault("numero", "")));
        p.setTipo(String.valueOf(body.getOrDefault("tipo", "JUDICIAL")));
        p.setTribunal(String.valueOf(body.getOrDefault("tribunal", "")));
        p.setParte(String.valueOf(body.getOrDefault("parte", "")));
        p.setAssunto(String.valueOf(body.getOrDefault("assunto", "")));
        Object statusObj = body.get("status");
        p.setStatus(statusObj != null ? com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.valueOf(String.valueOf(statusObj)) : com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.EM_ANDAMENTO);
        p.setDataAbertura(java.time.LocalDate.now());
        processoJuridicoRepository.save(p);
        return ResponseEntity.ok(Map.of("id", p.getId()));
    }

    @PostMapping("/api/processos/{id}/audiencias")
    @ResponseBody
    public ResponseEntity<?> criarAudienciaApi(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        com.jaasielsilva.portalceo.model.juridico.Audiencia a = new com.jaasielsilva.portalceo.model.juridico.Audiencia();
        a.setProcessoId(id);
        Object dh = body.get("dataHora");
        if (dh != null && !String.valueOf(dh).isBlank()) {
            a.setDataHora(java.time.LocalDateTime.parse(String.valueOf(dh)));
        } else {
            a.setDataHora(java.time.LocalDateTime.now().plusDays(7));
        }
        a.setTipo(String.valueOf(body.getOrDefault("tipo", "INSTRUCAO")));
        a.setObservacoes(String.valueOf(body.getOrDefault("observacoes", "")));
        audienciaRepository.save(a);
        return ResponseEntity.ok(Map.of("id", a.getId()));
    }

    @GetMapping("/api/processos/{id}/audiencias")
    @ResponseBody
    public ResponseEntity<List<com.jaasielsilva.portalceo.model.juridico.Audiencia>> listarAudiencias(@PathVariable Long id) {
        return ResponseEntity.ok(processoJuridicoService.listarAudienciasDoProcesso(id));
    }

    @PostMapping("/api/processos/{id}/prazos")
    @ResponseBody
    public ResponseEntity<?> criarPrazoApi(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body) {
        com.jaasielsilva.portalceo.model.juridico.PrazoJuridico pz = new com.jaasielsilva.portalceo.model.juridico.PrazoJuridico();
        pz.setProcessoId(id);
        Object dl = body.get("dataLimite");
        if (dl != null && !String.valueOf(dl).isBlank()) {
            pz.setDataLimite(java.time.LocalDate.parse(String.valueOf(dl)));
        } else {
            pz.setDataLimite(java.time.LocalDate.now().plusDays(15));
        }
        pz.setDescricao(String.valueOf(body.getOrDefault("descricao", "Apresentar contestação")));
        pz.setResponsabilidade(String.valueOf(body.getOrDefault("responsavel", "juridico")));
        pz.setCumprido(false);
        prazoJuridicoRepository.save(pz);
        return ResponseEntity.ok(Map.of("id", pz.getId()));
    }

    @GetMapping("/api/processos/{id}/prazos")
    @ResponseBody
    public ResponseEntity<List<com.jaasielsilva.portalceo.model.juridico.PrazoJuridico>> listarPrazos(@PathVariable Long id) {
        return ResponseEntity.ok(processoJuridicoService.listarPrazosDoProcesso(id));
    }

    @PutMapping("/api/prazos/{id}/concluir")
    @ResponseBody
    public ResponseEntity<?> concluirPrazo(@PathVariable Long id) {
        try {
            com.jaasielsilva.portalceo.model.juridico.PrazoJuridico p = processoJuridicoService.concluirPrazo(id);
            return ResponseEntity.ok(Map.of("id", p.getId(), "cumprido", p.isCumprido()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/api/processos/{id}/status")
    @ResponseBody
    public ResponseEntity<?> atualizarStatusProcesso(@PathVariable Long id,
                                                     @RequestParam("status") String status) {
        try {
            com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso st = com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.valueOf(status);
            com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico p = processoJuridicoService.atualizarStatus(id, st);
            return ResponseEntity.ok(Map.of("id", p.getId(), "status", p.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Status inválido"));
        }
    }

    // =============== APIs de Compliance ===============
    @GetMapping("/api/compliance/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> statusComplianceApi() {
        Map<String, Object> status = new HashMap<>();
        status.put("conformidade", 100);
        status.put("naoConformidades", naoConformidadeRepository.count());
        status.put("auditoriasPendentes", auditoriaComplianceRepository.count());
        status.put("ultimaAuditoria", java.time.LocalDate.now());
        return ResponseEntity.ok(status);
    }

    // Removido endpoint duplicado de normas; ver versão consolidada abaixo

    @GetMapping("/api/compliance/nao-conformidades")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> naoConformidadesApi() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (var nc : naoConformidadeRepository.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", nc.getId());
            m.put("codigo", nc.getCodigo());
            m.put("titulo", nc.getTitulo());
            m.put("descricao", nc.getDescricao());
            m.put("severidade", nc.getSeveridade());
            m.put("dataDeteccao", nc.getDataDeteccao());
            m.put("resolvida", nc.isResolvida());
            items.add(m);
        }
        return ResponseEntity.ok(items);
    }

    @PutMapping("/api/compliance/nao-conformidades/{id}/status")
    @ResponseBody
    public ResponseEntity<?> atualizarStatusNaoConformidade(@PathVariable Long id,
                                                            @RequestParam(value = "resolvida", required = false) Boolean resolvida) {
        return naoConformidadeRepository.findById(id)
                .map(nc -> {
                    if (resolvida == null) {
                        nc.setResolvida(!nc.isResolvida());
                    } else {
                        nc.setResolvida(resolvida);
                    }
                    naoConformidadeRepository.save(nc);
                    return ResponseEntity.ok(Map.of("id", nc.getId(), "resolvida", nc.isResolvida()));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Não conformidade não encontrada")));
    }

    @GetMapping("/api/compliance/normas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> normasApi() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (var n : normaRepository.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("codigo", n.getCodigo());
            m.put("nome", n.getTitulo());
            // Deriva status com base em 'vigente'
            m.put("status", n.isVigente() ? "VIGENTE" : "OBSOLETA");
            // Campo não existente em Norma; manter nulo para compatibilidade de template
            m.put("ultimaRevisao", null);
            items.add(m);
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping("/api/compliance/normas/{id}")
    @ResponseBody
    public ResponseEntity<?> obterNormaPorId(@PathVariable Long id) {
        return normaRepository.findById(id)
                .map(n -> ResponseEntity.ok(Map.of(
                        "id", n.getId(),
                        "codigo", n.getCodigo(),
                        "nome", n.getTitulo(),
                        "descricao", n.getDescricao(),
                        // Deriva status com base em 'vigente'
                        "status", n.isVigente() ? "VIGENTE" : "OBSOLETA",
                        // Campo não existente em Norma; manter nulo para compatibilidade de template
                        "ultimaRevisao", null
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Norma não encontrada")));
    }

    @GetMapping("/api/compliance/auditorias")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> auditoriasApi() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (var a : auditoriaComplianceRepository.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("tipo", a.getTipo());
            m.put("escopo", a.getEscopo());
            m.put("dataInicio", a.getDataInicio());
            m.put("auditor", a.getAuditor());
            m.put("resultado", a.getResultado());
            items.add(m);
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping("/api/compliance/auditorias/{id}")
    @ResponseBody
    public ResponseEntity<?> obterAuditoriaPorId(@PathVariable Long id) {
        return auditoriaComplianceRepository.findById(id)
                .map(a -> ResponseEntity.ok(Map.of(
                        "id", a.getId(),
                        "tipo", a.getTipo(),
                        "escopo", a.getEscopo(),
                        "dataInicio", a.getDataInicio(),
                        "auditor", a.getAuditor(),
                        "resultado", a.getResultado()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Auditoria não encontrada")));
    }

    @PostMapping("/api/compliance/auditorias")
    @ResponseBody
    public ResponseEntity<?> criarAuditoriaApi(@RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        com.jaasielsilva.portalceo.model.juridico.AuditoriaCompliance ac = new com.jaasielsilva.portalceo.model.juridico.AuditoriaCompliance();
        ac.setTipo(String.valueOf(body.getOrDefault("tipo", "INTERNA")));
        ac.setEscopo(String.valueOf(body.getOrDefault("escopo", "LGPD")));
        Object di = body.get("dataInicio");
        if (di != null && !String.valueOf(di).isBlank()) {
            ac.setDataInicio(java.time.LocalDate.parse(String.valueOf(di)));
        } else {
            ac.setDataInicio(java.time.LocalDate.now());
        }
        ac.setAuditor(String.valueOf(body.getOrDefault("auditor", userDetails != null ? userDetails.getUsername() : "sistema")));
        auditoriaComplianceRepository.save(ac);
        return ResponseEntity.ok(Map.of("id", ac.getId()));
    }

    // =============== APIs de Documentos ===============
    @GetMapping("/api/documentos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarDocumentosApi(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico> docs = documentoJuridicoRepository.findAll(pageable);
        List<Map<String, Object>> content = new ArrayList<>();
        for (var d : docs.getContent()) {
            if (categoria != null && !categoria.isBlank()) {
                String cat = categoria.toLowerCase();
                if (d.getCategoria() == null || !d.getCategoria().toLowerCase().contains(cat)) continue;
            }
            if (search != null && !search.isBlank()) {
                String termo = search.toLowerCase();
                String texto = ((d.getTitulo() == null ? "" : d.getTitulo()) + " " + (d.getDescricao() == null ? "" : d.getDescricao())).toLowerCase();
                if (!texto.contains(termo)) continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("titulo", d.getTitulo());
            m.put("categoria", d.getCategoria());
            m.put("descricao", d.getDescricao());
            m.put("caminhoArquivo", d.getCaminhoArquivo());
            m.put("criadoEm", d.getCriadoEm());
            content.add(m);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", content);
        payload.put("totalElements", docs.getTotalElements());
        payload.put("page", docs.getNumber());
        payload.put("size", docs.getSize());
        payload.put("totalPages", docs.getTotalPages());
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/api/documentos/upload")
    @ResponseBody
    public ResponseEntity<?> uploadDocumentoApi(@RequestBody Map<String, Object> body,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico d = new com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico();
        d.setTitulo(String.valueOf(body.getOrDefault("titulo", "Documento")));
        d.setCategoria(String.valueOf(body.getOrDefault("categoria", "Contrato")));
        d.setDescricao(String.valueOf(body.getOrDefault("descricao", "")));
        d.setCaminhoArquivo(String.valueOf(body.getOrDefault("caminhoArquivo", "")));
        d.setCriadoEm(java.time.LocalDateTime.now());
        documentoJuridicoRepository.save(d);
        return ResponseEntity.ok(Map.of("id", d.getId()));
    }

    @PostMapping("/api/documentos/upload-multipart")
    @ResponseBody
    public ResponseEntity<?> uploadDocumentoMultipart(@RequestParam("file") MultipartFile file,
                                                      @RequestParam String titulo,
                                                      @RequestParam String categoria,
                                                      @RequestParam(value = "descricao", required = false) String descricao,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String baseDir = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "juridico" + java.io.File.separator + "documentos";
            java.nio.file.Path dir = java.nio.file.Paths.get(baseDir);
            java.nio.file.Files.createDirectories(dir);
            String sanitized = java.util.UUID.randomUUID() + "_" + (
                    file.getOriginalFilename() == null
                            ? "arquivo"
                            : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9_\\.\\-]", "_")
            );
            java.nio.file.Path target = dir.resolve(sanitized);
            file.transferTo(target.toFile());

            com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico d = new com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico();
            d.setTitulo(titulo);
            d.setCategoria(categoria);
            d.setDescricao(descricao != null ? descricao : "");
            d.setCaminhoArquivo(target.toString());
            d.setCriadoEm(java.time.LocalDateTime.now());
            documentoJuridicoRepository.save(d);
            return ResponseEntity.ok(Map.of("id", d.getId(), "caminhoArquivo", d.getCaminhoArquivo()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha no upload do arquivo", "detalhes", e.getMessage()));
        }
    }

    @GetMapping("/api/documentos/{id}")
    @ResponseBody
    public ResponseEntity<?> obterDocumentoPorId(@PathVariable Long id) {
        return documentoJuridicoRepository.findById(id)
                .map(d -> ResponseEntity.ok(Map.of(
                        "id", d.getId(),
                        "titulo", d.getTitulo(),
                        "categoria", d.getCategoria(),
                        "descricao", d.getDescricao(),
                        "caminhoArquivo", d.getCaminhoArquivo(),
                        "criadoEm", d.getCriadoEm()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Documento não encontrado")));
    }

    @DeleteMapping("/api/documentos/{id}")
    @ResponseBody
    public ResponseEntity<?> excluirDocumentoApi(@PathVariable Long id) {
        if (!documentoJuridicoRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("erro", "Documento não encontrado"));
        }
        documentoJuridicoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("id", id, "excluido", true));
    }

    // =============== Endpoints de Ações de Contrato ===============

    @PutMapping("/contratos/{id}/enviar-analise")
    @ResponseBody
    public ResponseEntity<?> enviarParaAnalise(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.enviarParaAnalise(id, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato enviado para análise"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao enviar para análise", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/aprovar")
    @ResponseBody
    public ResponseEntity<?> aprovarContrato(@PathVariable Long id,
                                             @RequestBody(required = false) Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String observacoes = body != null ? String.valueOf(body.getOrDefault("observacoes", "")) : "";
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.aprovarContrato(id, observacoes, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato aprovado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao aprovar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/assinar")
    @ResponseBody
    public ResponseEntity<?> assinarContrato(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String dataStr = String.valueOf(body.getOrDefault("dataAssinatura", ""));
            LocalDate dataAssinatura = LocalDate.parse(dataStr);
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.assinarContrato(id, dataAssinatura, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato assinado"
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao assinar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/ativar")
    @ResponseBody
    public ResponseEntity<?> ativarContrato(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.ativarContrato(id, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato ativado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao ativar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/suspender")
    @ResponseBody
    public ResponseEntity<?> suspenderContrato(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String motivo = String.valueOf(body.getOrDefault("motivo", ""));
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.suspenderContrato(id, motivo, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato suspenso"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao suspender contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/reativar")
    @ResponseBody
    public ResponseEntity<?> reativarContrato(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.reativarContrato(id, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato reativado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao reativar contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/rescindir")
    @ResponseBody
    public ResponseEntity<?> rescindirContrato(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String motivo = String.valueOf(body.getOrDefault("motivo", ""));
            String dataStr = String.valueOf(body.getOrDefault("dataRescisao", ""));
            LocalDate dataRescisao = LocalDate.parse(dataStr);
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.rescindirContrato(id, motivo, dataRescisao, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato rescindido"
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Data inválida: use o formato YYYY-MM-DD"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao rescindir contrato", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/contratos/{id}/renovar")
    @ResponseBody
    public ResponseEntity<?> renovarContrato(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer novasDuracaoMeses = body.get("novasDuracaoMeses") != null ? Integer.parseInt(String.valueOf(body.get("novasDuracaoMeses"))) : null;
            String valorStr = String.valueOf(body.getOrDefault("novoValor", "0"));
            // aceitar tanto "1500,00" quanto "1500.00"
            valorStr = valorStr.replace(".", "").replace(",", ".");
            BigDecimal novoValor = new BigDecimal(valorStr);

            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .or(() -> usuarioRepository.findByMatricula(userDetails.getUsername()))
                    .orElse(null);
            ContratoLegal atualizado = contratoLegalService.renovarContrato(id, novasDuracaoMeses, novoValor, usuario);
            return ResponseEntity.ok(Map.of(
                    "id", atualizado.getId(),
                    "numeroContrato", atualizado.getNumeroContrato(),
                    "status", atualizado.getStatus(),
                    "mensagem", "Contrato renovado"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha ao renovar contrato", "detalhes", e.getMessage()));
        }
    }

    // =============== MÉTODOS AUXILIARES ===============
    
    private int getContratosAtivos() {
        return 45;
    }
    
    private int getProcessosAndamento() {
        try {
            return (int) processoJuridicoRepository.count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int getPrazosVencendo() {
        return 8;
    }
    
    private int getAlertasCompliance() {
        try {
            return (int) naoConformidadeRepository.count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private List<Map<String, Object>> getContratosProximosVencimento() {
        List<Map<String, Object>> contratos = new ArrayList<>();
        
        Map<String, Object> contrato1 = new HashMap<>();
        contrato1.put("titulo", "Contrato de Prestação de Serviços - TechCorp");
        contrato1.put("contraparte", "TechCorp Ltda");
        contrato1.put("dataVencimento", LocalDate.now().plusDays(15));
        contrato1.put("valor", "R$ 50.000,00");
        contrato1.put("status", "ATIVO");
        contratos.add(contrato1);
        
        Map<String, Object> contrato2 = new HashMap<>();
        contrato2.put("titulo", "Contrato de Locação - Escritório Central");
        contrato2.put("contraparte", "Imobiliária ABC");
        contrato2.put("dataVencimento", LocalDate.now().plusDays(30));
        contrato2.put("valor", "R$ 15.000,00");
        contrato2.put("status", "ATIVO");
        contratos.add(contrato2);
        
        return contratos;
    }
    
    private List<Map<String, Object>> getProcessosUrgentes() {
        List<Map<String, Object>> processos = new ArrayList<>();
        
        Map<String, Object> processo1 = new HashMap<>();
        processo1.put("numero", "1234567-89.2023.8.26.0100");
        processo1.put("tipo", "Trabalhista");
        processo1.put("parte", "João Silva vs. Empresa");
        processo1.put("proximoPrazo", LocalDate.now().plusDays(3));
        processo1.put("acao", "Contestação");
        processos.add(processo1);
        
        return processos;
    }
    
    private List<Map<String, Object>> getUltimasAtividades() {
        List<Map<String, Object>> atividades = new ArrayList<>();
        
        Map<String, Object> atividade1 = new HashMap<>();
        atividade1.put("tipo", "Contrato");
        atividade1.put("descricao", "Contrato de prestação de serviços aprovado");
        atividade1.put("data", LocalDateTime.now().minusHours(2));
        atividade1.put("usuario", "Dr. Carlos Oliveira");
        atividades.add(atividade1);
        
        Map<String, Object> atividade2 = new HashMap<>();
        atividade2.put("tipo", "Processo");
        atividade2.put("descricao", "Audiência agendada para processo trabalhista");
        atividade2.put("data", LocalDateTime.now().minusHours(4));
        atividade2.put("usuario", "Dra. Ana Santos");
        atividades.add(atividade2);
        
        return atividades;
    }
    
    private List<Map<String, Object>> getListaContratos(String status) {
        // Simular lista de contratos por status
        return new ArrayList<>();
    }
    
    private Map<String, Object> getEstatisticasContratos() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAtivos", 45);
        stats.put("totalAnalise", 8);
        stats.put("totalVencidos", 3);
        stats.put("valorTotal", "R$ 2.450.000,00");
        return stats;
    }
    
    private List<Map<String, Object>> getListaProcessos(String status) {
        // Simular lista de processos por status
        return new ArrayList<>();
    }
    
    private List<Map<String, Object>> getProximasAudiencias() {
        List<Map<String, Object>> audiencias = new ArrayList<>();
        
        Map<String, Object> audiencia1 = new HashMap<>();
        audiencia1.put("processo", "1234567-89.2023.8.26.0100");
        audiencia1.put("tipo", "Instrução");
        audiencia1.put("data", LocalDateTime.now().plusDays(7));
        audiencia1.put("local", "Fórum Central");
        audiencias.add(audiencia1);
        
        return audiencias;
    }
    
    private List<Map<String, Object>> getPrazosCriticos() {
        List<Map<String, Object>> prazos = new ArrayList<>();
        
        Map<String, Object> prazo1 = new HashMap<>();
        prazo1.put("processo", "1234567-89.2023.8.26.0100");
        prazo1.put("acao", "Contestação");
        prazo1.put("dataLimite", LocalDate.now().plusDays(3));
        prazo1.put("responsavel", "Dr. Carlos Oliveira");
        prazos.add(prazo1);
        
        return prazos;
    }
    
    private Map<String, Object> getStatusCompliance() {
        Map<String, Object> status = new HashMap<>();
        status.put("conformidade", 92.5);
        status.put("naoConformidades", 3);
        status.put("auditoriasPendentes", 2);
        status.put("ultimaAuditoria", LocalDate.now().minusMonths(3));
        return status;
    }
    
    private List<Map<String, Object>> getNormasVigentes() {
        List<Map<String, Object>> normas = new ArrayList<>();
        
        Map<String, Object> norma1 = new HashMap<>();
        norma1.put("codigo", "LGPD");
        norma1.put("nome", "Lei Geral de Proteção de Dados");
        norma1.put("status", "VIGENTE");
        norma1.put("ultimaRevisao", LocalDate.now().minusMonths(6));
        normas.add(norma1);
        
        return normas;
    }
    
    private List<Map<String, Object>> getNaoConformidades() {
        List<Map<String, Object>> naoConformidades = new ArrayList<>();
        
        Map<String, Object> nc1 = new HashMap<>();
        nc1.put("codigo", "NC-001");
        nc1.put("descricao", "Falta de treinamento em LGPD");
        nc1.put("severidade", "MEDIA");
        nc1.put("status", "ABERTA");
        nc1.put("prazoCorrecao", LocalDate.now().plusDays(30));
        naoConformidades.add(nc1);
        
        return naoConformidades;
    }
    
    private List<Map<String, Object>> getAuditorias() {
        List<Map<String, Object>> auditorias = new ArrayList<>();
        
        Map<String, Object> auditoria1 = new HashMap<>();
        auditoria1.put("tipo", "Compliance LGPD");
        auditoria1.put("status", "CONCLUIDA");
        auditoria1.put("data", LocalDate.now().minusMonths(3));
        auditoria1.put("resultado", "APROVADO");
        auditorias.add(auditoria1);
        
        return auditorias;
    }
    
    private List<Map<String, Object>> getCategoriasDocumentos() {
        List<Map<String, Object>> categorias = new ArrayList<>();
        
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("nome", "Contratos");
        cat1.put("quantidade", 45);
        categorias.add(cat1);
        
        Map<String, Object> cat2 = new HashMap<>();
        cat2.put("nome", "Processos");
        cat2.put("quantidade", 23);
        categorias.add(cat2);
        
        return categorias;
    }
    
    private List<Map<String, Object>> getDocumentosRecentes() {
        List<Map<String, Object>> documentos = new ArrayList<>();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("titulo", "Contrato de Prestação de Serviços");
        doc1.put("categoria", "Contratos");
        doc1.put("dataUpload", LocalDateTime.now().minusHours(2));
        doc1.put("autor", "Dr. Carlos Oliveira");
        documentos.add(doc1);
        
        return documentos;
    }
    
    private List<Map<String, Object>> getModelosDocumentos() {
        List<Map<String, Object>> modelos = new ArrayList<>();
        
        Map<String, Object> modelo1 = new HashMap<>();
        modelo1.put("nome", "Modelo de Contrato de Prestação de Serviços");
        modelo1.put("categoria", "Contratos");
        modelo1.put("versao", "2.1");
        modelos.add(modelo1);
        
        return modelos;
    }
    
    private List<Map<String, Object>> getDocumentosPendentesAssinatura() {
        List<Map<String, Object>> pendentes = new ArrayList<>();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("titulo", "Contrato de Locação - Filial Norte");
        doc1.put("signatarios", "2 de 3");
        doc1.put("prazoAssinatura", LocalDate.now().plusDays(5));
        pendentes.add(doc1);
        
        return pendentes;
    }
}