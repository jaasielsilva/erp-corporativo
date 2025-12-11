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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final com.jaasielsilva.portalceo.repository.juridico.DocumentoModeloRepository documentoModeloRepository;
    private final com.jaasielsilva.portalceo.service.DocumentTemplateService documentTemplateService;

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
                item.put("contraparte", c.getNomeContraparte());
                item.put("dataVencimento", c.getDataVencimento());
                item.put("diasRestantes", c.getDiasParaVencimento());
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
        long totalDocumentos = documentoJuridicoRepository.count();
        model.addAttribute("totalDocumentos", totalDocumentos);
        java.util.List<java.util.Map<String, Object>> categorias = new java.util.ArrayList<>();
        for (Object[] row : documentoJuridicoRepository.contagemPorCategoria()) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("nome", String.valueOf(row[0]));
            m.put("quantidade", ((Number) row[1]).longValue());
            categorias.add(m);
        }
        model.addAttribute("categoriasDocumentos", categorias);

        org.springframework.data.domain.Pageable pr = org.springframework.data.domain.PageRequest.of(
                0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "criadoEm"));
        java.util.List<java.util.Map<String, Object>> recentes = documentoJuridicoRepository.findAll(pr).getContent().stream()
                .map(d -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", d.getId());
                    m.put("titulo", d.getTitulo());
                    m.put("categoria", d.getCategoria());
                    m.put("dataUpload", d.getCriadoEm());
                    m.put("autor", d.getAutor());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("documentosRecentes", recentes);

        if (documentoModeloRepository.count() == 0) {
            try {
                String baseDirModelos = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "juridico" + java.io.File.separator + "modelos";
                java.nio.file.Path dirM = java.nio.file.Paths.get(baseDirModelos);
                java.nio.file.Files.createDirectories(dirM);
                java.nio.file.Path target = dirM.resolve("modelo_contrato_prestacao_servicos_v2_1.docx");
                if (!java.nio.file.Files.exists(target)) {
                    java.nio.file.Files.write(target, new byte[0]);
                }
                com.jaasielsilva.portalceo.model.juridico.DocumentoModelo novo = new com.jaasielsilva.portalceo.model.juridico.DocumentoModelo();
                novo.setNome("Modelo de Contrato de Prestação de Serviços");
                novo.setCategoria("CONTRATOS");
                novo.setVersao("2.1");
                novo.setStatus(com.jaasielsilva.portalceo.model.juridico.ModeloStatus.PUBLICADO);
                novo.setArquivoModelo(target.toString());
                novo.setDataCriacao(java.time.LocalDateTime.now());
                novo.setDataPublicacao(java.time.LocalDateTime.now());
                documentoModeloRepository.save(novo);
            } catch (Exception ignored) {}
        }
        java.util.List<java.util.Map<String, Object>> modelosVm = documentoModeloRepository.findAll().stream()
                .map(m -> {
                    java.util.Map<String, Object> vm = new java.util.HashMap<>();
                    vm.put("id", m.getId());
                    vm.put("nome", m.getNome());
                    vm.put("categoria", m.getCategoria());
                    vm.put("versao", m.getVersao());
                    vm.put("status", m.getStatus() != null ? m.getStatus().name() : null);
                    return vm;
                }).collect(java.util.stream.Collectors.toList());
        model.addAttribute("modelosDocumentos", modelosVm);
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

    @GetMapping("/api/processos-urgentes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarProcessosUrgentesApi(
            @RequestParam(value = "dias", defaultValue = "7") int dias,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        List<Map<String, Object>> todos = processoJuridicoService.obterProcessosUrgentes(dias);
        int total = todos != null ? todos.size() : 0;
        int from = Math.max(0, Math.min(page * size, total));
        int to = Math.max(from, Math.min(from + size, total));
        List<Map<String, Object>> content = total > 0 ? todos.subList(from, to) : java.util.Collections.emptyList();
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("content", content);
        payload.put("totalElements", total);
        payload.put("page", page);
        payload.put("size", size);
        payload.put("totalPages", size > 0 ? (int) Math.ceil(total / (double) size) : 0);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/ultimas-atividades")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarUltimasAtividadesApi(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "dataHora"));
        org.springframework.data.domain.Page<com.jaasielsilva.portalceo.model.juridico.AndamentoProcesso> pagina = andamentoProcessoRepository.findAll(pageable);
        java.util.List<java.util.Map<String, Object>> content = pagina.getContent().stream()
                .map(a -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("tipo", "Processo");
                    m.put("descricao", a.getDescricao());
                    m.put("data", a.getDataHora());
                    m.put("usuario", a.getUsuario());
                    return m;
                }).toList();
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("content", content);
        payload.put("totalElements", pagina.getTotalElements());
        payload.put("page", pagina.getNumber());
        payload.put("size", pagina.getSize());
        payload.put("totalPages", pagina.getTotalPages());
        return ResponseEntity.ok(payload);
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
        com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico.StatusProcesso.valueOf(status);
            } catch (IllegalArgumentException ignored) {}
        }

        Page<com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico> processos;
        boolean hasSearch = search != null && !search.isBlank();
        if (statusEnum != null && hasSearch) {
            processos = processoJuridicoRepository.searchByStatusAndText(statusEnum, search, pageable);
        } else if (statusEnum != null) {
            processos = processoJuridicoRepository.findByStatus(statusEnum, pageable);
        } else if (hasSearch) {
            processos = processoJuridicoRepository.findByNumeroContainingIgnoreCaseOrParteContainingIgnoreCaseOrAssuntoContainingIgnoreCase(
                    search, search, search, pageable);
        } else {
            processos = processoJuridicoRepository.findAll(pageable);
        }

        List<Map<String, Object>> content = new ArrayList<>();
        for (var p : processos.getContent()) {
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
                .map(p -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", p.getId());
                    m.put("numero", p.getNumero());
                    m.put("tipo", p.getTipo());
                    m.put("tribunal", p.getTribunal());
                    m.put("parte", p.getParte());
                    m.put("assunto", p.getAssunto());
                    m.put("status", p.getStatus());
                    m.put("dataAbertura", p.getDataAbertura());
                    return ResponseEntity.ok(m);
                })
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

    @PostMapping("/api/compliance/nao-conformidades")
    @ResponseBody
    public ResponseEntity<?> criarNaoConformidadeApi(@RequestBody Map<String, Object> body) {
        com.jaasielsilva.portalceo.model.juridico.NaoConformidade nc = new com.jaasielsilva.portalceo.model.juridico.NaoConformidade();
        nc.setCodigo(String.valueOf(body.getOrDefault("codigo", "NC-" + System.currentTimeMillis())));
        nc.setTitulo(String.valueOf(body.getOrDefault("titulo", "Não conformidade")));
        nc.setDescricao(String.valueOf(body.getOrDefault("descricao", "")));
        nc.setSeveridade(String.valueOf(body.getOrDefault("severidade", "MEDIA")));
        Object dd = body.get("dataDeteccao");
        if (dd != null && !String.valueOf(dd).isBlank()) {
            nc.setDataDeteccao(java.time.LocalDate.parse(String.valueOf(dd)));
        } else {
            nc.setDataDeteccao(java.time.LocalDate.now());
        }
        Object res = body.get("resolvida");
        nc.setResolvida(res != null && Boolean.parseBoolean(String.valueOf(res)));
        naoConformidadeRepository.save(nc);
        return ResponseEntity.ok(Map.of("id", nc.getId()));
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

    @PostMapping("/api/compliance/normas")
    @ResponseBody
    public ResponseEntity<?> criarNormaApi(@RequestBody Map<String, Object> body) {
        com.jaasielsilva.portalceo.model.juridico.Norma n = new com.jaasielsilva.portalceo.model.juridico.Norma();
        String codigo = String.valueOf(body.getOrDefault("codigo", "NR-" + System.currentTimeMillis()));
        String titulo = String.valueOf(body.getOrDefault("titulo", "Norma"));
        String orgao = String.valueOf(body.getOrDefault("orgao", "Órgão Regulador"));
        String descricao = String.valueOf(body.getOrDefault("descricao", ""));
        codigo = codigo.length() > 255 ? codigo.substring(0, 255) : codigo;
        titulo = titulo.length() > 255 ? titulo.substring(0, 255) : titulo;
        orgao = orgao.length() > 255 ? orgao.substring(0, 255) : orgao;
        descricao = descricao.length() > 2048 ? descricao.substring(0, 2048) : descricao;
        n.setCodigo(codigo);
        n.setTitulo(titulo);
        n.setOrgao(orgao);
        n.setDescricao(descricao);
        Object vig = body.get("vigente");
        n.setVigente(vig != null && Boolean.parseBoolean(String.valueOf(vig)));
        normaRepository.save(n);
        return ResponseEntity.ok(Map.of("id", n.getId()));
    }

    @GetMapping("/api/compliance/normas/{id}")
    @ResponseBody
    public ResponseEntity<?> obterNormaPorId(@PathVariable Long id) {
        return normaRepository.findById(id)
                .map(n -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", n.getId());
                    m.put("codigo", n.getCodigo());
                    m.put("nome", n.getTitulo());
                    m.put("descricao", n.getDescricao());
                    m.put("status", n.isVigente() ? "VIGENTE" : "OBSOLETA");
                    m.put("ultimaRevisao", null);
                    return ResponseEntity.ok(m);
                })
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
            m.put("status", a.getResultado() == null ? "PLANEJADA" : "CONCLUÍDA");
            items.add(m);
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping("/api/compliance/auditorias/{id}")
    @ResponseBody
    public ResponseEntity<?> obterAuditoriaPorId(@PathVariable Long id) {
        return auditoriaComplianceRepository.findById(id)
                .map(a -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", a.getId());
                    m.put("tipo", a.getTipo());
                    m.put("escopo", a.getEscopo());
                    m.put("dataInicio", a.getDataInicio());
                    m.put("auditor", a.getAuditor());
                    m.put("resultado", a.getResultado());
                    m.put("status", a.getResultado() == null ? "PLANEJADA" : "CONCLUÍDA");
                    return ResponseEntity.ok(m);
                })
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

    @PutMapping("/api/compliance/auditorias/{id}/resultado")
    @ResponseBody
    public ResponseEntity<?> atualizarResultadoAuditoria(@PathVariable Long id,
                                                         @RequestBody Map<String, Object> body) {
        return auditoriaComplianceRepository.findById(id)
                .map(a -> {
                    String resultado = String.valueOf(body.getOrDefault("resultado", "")).trim();
                    a.setResultado(resultado.isEmpty() ? null : resultado);
                    auditoriaComplianceRepository.save(a);
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("resultado", a.getResultado());
                    m.put("status", a.getResultado() == null ? "PLANEJADA" : "CONCLUÍDA");
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("erro", "Auditoria não encontrada")));
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
            m.put("contentType", d.getContentType());
            m.put("originalFilename", d.getOriginalFilename());
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

    @GetMapping("/api/documentos/metrics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> metricsDocumentos() {
        long total = documentoJuridicoRepository.count();
        java.util.List<java.util.Map<String, Object>> categorias = new java.util.ArrayList<>();
        for (Object[] row : documentoJuridicoRepository.contagemPorCategoria()) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("nome", String.valueOf(row[0]));
            m.put("quantidade", ((Number) row[1]).longValue());
            categorias.add(m);
        }
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("total", total);
        payload.put("categorias", categorias);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/modelos")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> listarModelos(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "status", required = false) com.jaasielsilva.portalceo.model.juridico.ModeloStatus status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<com.jaasielsilva.portalceo.model.juridico.DocumentoModelo> modelos;
        if (categoria != null && !categoria.isBlank() && status != null) {
            modelos = documentoModeloRepository.findByCategoriaContainingIgnoreCaseAndStatus(categoria, status, pageable);
        } else if (categoria != null && !categoria.isBlank()) {
            modelos = documentoModeloRepository.findByCategoriaContainingIgnoreCase(categoria, pageable);
        } else if (status != null) {
            modelos = documentoModeloRepository.findByStatus(status, pageable);
        } else {
            modelos = documentoModeloRepository.findAll(pageable);
        }
        java.util.List<java.util.Map<String, Object>> content = new java.util.ArrayList<>();
        for (var m : modelos.getContent()) {
            if (search != null && !search.isBlank()) {
                String s = search.toLowerCase();
                String txt = ((m.getNome() == null ? "" : m.getNome()) + " " + (m.getCategoria() == null ? "" : m.getCategoria())).toLowerCase();
                if (!txt.contains(s)) continue;
            }
            java.util.Map<String, Object> vm = new java.util.HashMap<>();
            vm.put("id", m.getId());
            vm.put("nome", m.getNome());
            vm.put("categoria", m.getCategoria());
            vm.put("versao", m.getVersao());
            vm.put("status", m.getStatus() != null ? m.getStatus().name() : null);
            content.add(vm);
        }
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("content", content);
        payload.put("totalElements", modelos.getTotalElements());
        payload.put("page", modelos.getNumber());
        payload.put("size", modelos.getSize());
        payload.put("totalPages", modelos.getTotalPages());
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/api/modelos/upload-multipart")
    @ResponseBody
    public ResponseEntity<?> criarModeloMultipart(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                                  @RequestParam String nome,
                                                  @RequestParam String categoria,
                                                  @RequestParam(required = false) String versao,
                                                  @RequestParam(required = false) String changelog,
                                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            String baseDir = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "juridico" + java.io.File.separator + "modelos";
            java.nio.file.Path dir = java.nio.file.Paths.get(baseDir);
            java.nio.file.Files.createDirectories(dir);
            String sanitized = java.util.UUID.randomUUID() + "_" + (
                    file.getOriginalFilename() == null
                            ? "modelo"
                            : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9_\\.\\-]", "_")
            );
            java.nio.file.Path target = dir.resolve(sanitized);
            file.transferTo(target.toFile());

            com.jaasielsilva.portalceo.model.juridico.DocumentoModelo m = new com.jaasielsilva.portalceo.model.juridico.DocumentoModelo();
            m.setNome(nome);
            m.setCategoria(categoria);
            m.setVersao(versao != null && !versao.isBlank() ? versao : "1.0.0");
            m.setStatus(com.jaasielsilva.portalceo.model.juridico.ModeloStatus.RASCUNHO);
            m.setArquivoModelo(target.toString());
            m.setChangelog(changelog);
            m.setCriadoPor(userDetails != null ? userDetails.getUsername() : null);
            m.setDataCriacao(java.time.LocalDateTime.now());
            documentoModeloRepository.save(m);
            return ResponseEntity.ok(java.util.Map.of("id", m.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("erro", "Falha ao criar modelo", "detalhes", e.getMessage()));
        }
    }

    @PostMapping("/api/modelos/upload-batch")
    @ResponseBody
    public ResponseEntity<?> criarModelosBatch(@RequestParam("files") java.util.List<org.springframework.web.multipart.MultipartFile> files,
                                               @RequestParam(required = false) String categoria,
                                               @RequestParam(required = false) String versao,
                                               @RequestParam(required = false) String changelog,
                                               @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("erro", "Selecione ao menos um arquivo"));
            }
            long maxSize = 10L * 1024 * 1024;
            java.util.Set<String> allowed = java.util.Set.of(".doc", ".docx", ".pdf");
            java.util.List<java.util.Map<String, Object>> criados = new java.util.ArrayList<>();
            String baseDir = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "juridico" + java.io.File.separator + "modelos";
            java.nio.file.Path dir = java.nio.file.Paths.get(baseDir);
            java.nio.file.Files.createDirectories(dir);
            for (var file : files) {
                if (file == null || file.isEmpty()) continue;
                if (file.getSize() > maxSize) {
                    return ResponseEntity.badRequest().body(java.util.Map.of("erro", "Arquivo excede 10MB"));
                }
                String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "modelo";
                String lower = originalName.toLowerCase();
                boolean ok = allowed.stream().anyMatch(lower::endsWith);
                if (!ok) {
                    return ResponseEntity.badRequest().body(java.util.Map.of("erro", "Tipos permitidos: PDF, DOC, DOCX"));
                }
                String sanitized = java.util.UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
                java.nio.file.Path target = dir.resolve(sanitized);
                file.transferTo(target.toFile());

                com.jaasielsilva.portalceo.model.juridico.DocumentoModelo m = new com.jaasielsilva.portalceo.model.juridico.DocumentoModelo();
                String nomeDerivado = originalName.replaceFirst("\\.[^.]*$", "").replaceAll("[_-]", " ").trim();
                m.setNome(nomeDerivado.isBlank() ? "Modelo" : nomeDerivado);
                m.setCategoria(categoria != null && !categoria.isBlank() ? categoria : "CONTRATOS");
                m.setVersao(versao != null && !versao.isBlank() ? versao : "1.0.0");
                m.setStatus(com.jaasielsilva.portalceo.model.juridico.ModeloStatus.RASCUNHO);
                m.setArquivoModelo(target.toString());
                m.setChangelog(changelog);
                m.setCriadoPor(userDetails != null ? userDetails.getUsername() : null);
                m.setDataCriacao(java.time.LocalDateTime.now());
                documentoModeloRepository.save(m);
                criados.add(java.util.Map.of("id", m.getId(), "nome", m.getNome(), "categoria", m.getCategoria(), "versao", m.getVersao()));
            }
            return ResponseEntity.ok(java.util.Map.of("criados", criados));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("erro", "Falha ao criar modelos", "detalhes", e.getMessage()));
        }
    }

    @PutMapping("/api/modelos/{id}/status")
    @ResponseBody
    public ResponseEntity<?> atualizarStatusModelo(@PathVariable Long id,
                                                   @RequestParam com.jaasielsilva.portalceo.model.juridico.ModeloStatus status,
                                                   @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return documentoModeloRepository.findById(id)
                .map(m -> {
                    m.setStatus(status);
                    if (status == com.jaasielsilva.portalceo.model.juridico.ModeloStatus.APROVADO) {
                        m.setAprovadoPor(userDetails != null ? userDetails.getUsername() : null);
                    }
                    if (status == com.jaasielsilva.portalceo.model.juridico.ModeloStatus.PUBLICADO) {
                        m.setDataPublicacao(java.time.LocalDateTime.now());
                    }
                    if (status == com.jaasielsilva.portalceo.model.juridico.ModeloStatus.DEPRECADO) {
                        m.setDeprecadoEm(java.time.LocalDateTime.now());
                    }
                    documentoModeloRepository.save(m);
                    return ResponseEntity.ok(java.util.Map.of("id", m.getId(), "status", m.getStatus().name()));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(java.util.Map.of("erro", "Modelo não encontrado")));
    }

    @GetMapping("/modelos/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadModelo(@PathVariable Long id) {
        return documentoModeloRepository.findById(id)
                .map(m -> {
                    try {
                        java.nio.file.Path path = java.nio.file.Paths.get(m.getArquivoModelo());
                        if (!java.nio.file.Files.exists(path)) {
                            return ResponseEntity.status(404).body((org.springframework.core.io.Resource) null);
                        }
                        String contentType = java.nio.file.Files.probeContentType(path);
                        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(path);
                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.setContentType(contentType != null ? org.springframework.http.MediaType.parseMediaType(contentType) : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", path.getFileName().toString());
                        return ResponseEntity.ok().headers(headers).body(resource);
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body((org.springframework.core.io.Resource) null);
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body((org.springframework.core.io.Resource) null));
    }

    @PostMapping("/api/documentos/gerar")
    @ResponseBody
    public ResponseEntity<?> gerarDocumentoAPartirDeModelo(@RequestParam Long modeloId,
                                                           @RequestParam String titulo,
                                                           @RequestParam(required = false) String categoria,
                                                           @RequestParam(required = false) String descricao,
                                                           @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return documentoModeloRepository.findById(modeloId)
                .map(m -> {
                    try {
                        String baseDir = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "juridico" + java.io.File.separator + "documentos";
                        java.nio.file.Path dir = java.nio.file.Paths.get(baseDir);
                        java.nio.file.Files.createDirectories(dir);
                        String filename = java.util.UUID.randomUUID() + "_" + java.nio.file.Paths.get(m.getArquivoModelo()).getFileName().toString();
                        java.nio.file.Path target = dir.resolve(filename);
                        java.nio.file.Files.copy(java.nio.file.Paths.get(m.getArquivoModelo()), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                        com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico d = new com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico();
                        d.setTitulo(titulo);
                        d.setCategoria(categoria != null && !categoria.isBlank() ? categoria : m.getCategoria());
                        d.setDescricao((descricao != null && !descricao.isBlank() ? descricao + " | " : "") + "Gerado a partir do modelo: " + m.getNome());
                        d.setCaminhoArquivo(target.toString());
                        d.setCriadoEm(java.time.LocalDateTime.now());
                        d.setAutor(userDetails != null ? userDetails.getUsername() : null);
                        documentoJuridicoRepository.save(d);
                        return ResponseEntity.ok(java.util.Map.of("id", d.getId(), "caminhoArquivo", d.getCaminhoArquivo()));
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body(java.util.Map.of("erro", "Falha ao gerar documento", "detalhes", e.getMessage()));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body(java.util.Map.of("erro", "Modelo não encontrado")));
    }

    @PostMapping("/api/documentos/{id}/personalizar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_JURIDICO')")
    public ResponseEntity<?> personalizarDocumento(@PathVariable Long id,
                                                   @RequestBody java.util.Map<String, Object> body,
                                                   @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return documentoJuridicoRepository.findById(id)
                .map(d -> {
                    try {
                        Object phObj = body.get("placeholders");
                        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                        if (phObj instanceof java.util.Map<?, ?> map) {
                            for (java.lang.Object k : map.keySet()) {
                                placeholders.put(String.valueOf(k), String.valueOf(map.get(k)));
                            }
                        }
                        boolean persist = java.util.Optional.ofNullable(body.get("persist")).map(v -> Boolean.parseBoolean(String.valueOf(v))).orElse(true);
                        String nomeArquivo = java.util.Optional.ofNullable(body.get("nomeArquivo")).map(String::valueOf).orElse(null);

                        byte[] result;
                        if (d.getConteudo() != null && d.getConteudo().length > 0) {
                            result = documentTemplateService.personalizeDocx(d.getConteudo(), placeholders);
                        } else if (d.getCaminhoArquivo() != null) {
                            java.nio.file.Path path = java.nio.file.Paths.get(d.getCaminhoArquivo());
                            result = documentTemplateService.personalizeDocx(path, placeholders);
                        } else {
                            return ResponseEntity.status(400).body(java.util.Map.of("erro", "Documento sem conteúdo ou caminho para personalização"));
                        }

                        if (persist) {
                            d.setConteudo(result);
                            d.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                            String baseName = nomeArquivo != null && !nomeArquivo.isBlank() ? nomeArquivo : (d.getOriginalFilename() != null ? d.getOriginalFilename() : (d.getTitulo() != null ? d.getTitulo() : "documento"));
                            if (!baseName.toLowerCase().endsWith(".docx")) baseName = baseName + ".docx";
                            d.setOriginalFilename(baseName);
                            d.setTamanho((long) result.length);
                            d.setCaminhoArquivo(null);
                            d.setCriadoEm(d.getCriadoEm() != null ? d.getCriadoEm() : java.time.LocalDateTime.now());
                            d.setAutor(userDetails != null ? userDetails.getUsername() : d.getAutor());
                            documentoJuridicoRepository.save(d);
                            return ResponseEntity.ok(java.util.Map.of("id", d.getId(), "personalizado", true));
                        } else {
                            org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(result);
                            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                            headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
                            String fname = nomeArquivo != null ? nomeArquivo : (d.getTitulo() != null ? d.getTitulo() : "documento") + "_personalizado.docx";
                            headers.setContentDispositionFormData("attachment", fname);
                            headers.setContentLength(result.length);
                            return ResponseEntity.ok().headers(headers).body(resource);
                        }
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body(java.util.Map.of("erro", "Falha ao personalizar", "detalhes", e.getMessage()));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body(java.util.Map.of("erro", "Documento não encontrado")));
    }

    @PostMapping("/api/documentos/gerar-pdf")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_JURIDICO')")
    public ResponseEntity<?> gerarDocumentoPdf(@RequestParam String titulo,
                                               @RequestBody java.util.Map<String, Object> body,
                                               @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            String html = java.util.Optional.ofNullable(body.get("html")).map(String::valueOf).orElse("");
            Object phObj = body.get("placeholders");
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            if (phObj instanceof java.util.Map<?, ?> map) {
                for (java.lang.Object k : map.keySet()) {
                    placeholders.put(String.valueOf(k), String.valueOf(map.get(k)));
                }
            }
            byte[] pdf = documentTemplateService.generatePdfFromHtml(html, placeholders);
            com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico d = new com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico();
            d.setTitulo(titulo);
            d.setCategoria(String.valueOf(java.util.Optional.ofNullable(body.get("categoria")).orElse("Contrato")));
            d.setDescricao(String.valueOf(java.util.Optional.ofNullable(body.get("descricao")).orElse("Documento PDF personalizado a partir de HTML")));
            d.setConteudo(pdf);
            d.setContentType(org.springframework.http.MediaType.APPLICATION_PDF_VALUE);
            d.setOriginalFilename(titulo.endsWith(".pdf") ? titulo : titulo + ".pdf");
            d.setTamanho((long) pdf.length);
            d.setCaminhoArquivo(null);
            d.setCriadoEm(java.time.LocalDateTime.now());
            d.setAutor(userDetails != null ? userDetails.getUsername() : null);
            documentoJuridicoRepository.save(d);
            return ResponseEntity.ok(java.util.Map.of("id", d.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("erro", "Falha ao gerar PDF", "detalhes", e.getMessage()));
        }
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
        d.setAutor(userDetails != null ? userDetails.getUsername() : null);
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
            com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico d = new com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico();
            d.setTitulo(titulo);
            d.setCategoria(categoria);
            d.setDescricao(descricao != null ? descricao : "");
            d.setConteudo(file.getBytes());
            d.setContentType(file.getContentType());
            d.setOriginalFilename(file.getOriginalFilename());
            d.setTamanho(file.getSize());
            d.setCaminhoArquivo(null);
            d.setCriadoEm(java.time.LocalDateTime.now());
            d.setAutor(userDetails != null ? userDetails.getUsername() : null);
            documentoJuridicoRepository.save(d);
            return ResponseEntity.ok(Map.of("id", d.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Falha no upload do arquivo", "detalhes", e.getMessage()));
        }
    }

    @GetMapping("/api/documentos/{id}")
    @ResponseBody
    public ResponseEntity<?> obterDocumentoPorId(@PathVariable Long id) {
        return documentoJuridicoRepository.findById(id)
                .map(d -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", d.getId());
                    m.put("titulo", d.getTitulo());
                    m.put("categoria", d.getCategoria());
                    m.put("descricao", d.getDescricao());
                    m.put("caminhoArquivo", d.getCaminhoArquivo());
                    m.put("criadoEm", d.getCriadoEm());
                    m.put("contentType", d.getContentType());
                    m.put("originalFilename", d.getOriginalFilename());
                    return ResponseEntity.ok(m);
                })
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

    @GetMapping("/documentos/download/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_JURIDICO')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocumento(@PathVariable Long id) {
        return documentoJuridicoRepository.findById(id)
                .map(d -> {
                    try {
                        if (d.getConteudo() != null && d.getConteudo().length > 0) {
                            org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(d.getConteudo());
                            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                            headers.setContentType(d.getContentType() != null ? org.springframework.http.MediaType.parseMediaType(d.getContentType()) : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                            String fname = d.getOriginalFilename() != null ? d.getOriginalFilename() : "documento";
                            headers.setContentDispositionFormData("attachment", fname);
                            headers.setContentLength(d.getTamanho() != null ? d.getTamanho() : d.getConteudo().length);
                            return ResponseEntity.ok().headers(headers).body(resource);
                        }
                        java.nio.file.Path path = java.nio.file.Paths.get(d.getCaminhoArquivo());
                        if (!java.nio.file.Files.exists(path)) {
                            return ResponseEntity.status(404).body((org.springframework.core.io.Resource) null);
                        }
                        String contentType = java.nio.file.Files.probeContentType(path);
                        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(path);
                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.setContentType(contentType != null ? org.springframework.http.MediaType.parseMediaType(contentType) : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", path.getFileName().toString());
                        return ResponseEntity.ok().headers(headers).body(resource);
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body((org.springframework.core.io.Resource) null);
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body((org.springframework.core.io.Resource) null));
    }

    @GetMapping("/documentos/preview/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_JURIDICO')")
    public ResponseEntity<org.springframework.core.io.Resource> previewDocumento(@PathVariable Long id) {
        return documentoJuridicoRepository.findById(id)
                .map(d -> {
                    try {
                        if (d.getConteudo() != null && d.getConteudo().length > 0) {
                            org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(d.getConteudo());
                            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                            headers.setContentType(d.getContentType() != null ? org.springframework.http.MediaType.parseMediaType(d.getContentType()) : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                            String fname = d.getOriginalFilename() != null ? d.getOriginalFilename() : "documento";
                            headers.set("Content-Disposition", "inline; filename=\"" + fname + "\"");
                            headers.setCacheControl("no-cache, no-store, must-revalidate");
                            headers.setPragma("no-cache");
                            headers.setExpires(0);
                            headers.setContentLength(d.getTamanho() != null ? d.getTamanho() : d.getConteudo().length);
                            return ResponseEntity.ok().headers(headers).body(resource);
                        }
                        java.nio.file.Path path = java.nio.file.Paths.get(d.getCaminhoArquivo());
                        if (!java.nio.file.Files.exists(path)) {
                            return ResponseEntity.status(404).body((org.springframework.core.io.Resource) null);
                        }
                        String contentType = java.nio.file.Files.probeContentType(path);
                        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(path);
                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.setContentType(contentType != null ? org.springframework.http.MediaType.parseMediaType(contentType) : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                        headers.set("Content-Disposition", "inline; filename=\"" + path.getFileName().toString() + "\"");
                        headers.setCacheControl("no-cache, no-store, must-revalidate");
                        headers.setPragma("no-cache");
                        headers.setExpires(0);
                        return ResponseEntity.ok().headers(headers).body(resource);
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body((org.springframework.core.io.Resource) null);
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body((org.springframework.core.io.Resource) null));
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
