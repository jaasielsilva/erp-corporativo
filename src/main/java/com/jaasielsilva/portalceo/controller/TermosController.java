package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.EstatisticasTermosDTO;
import com.jaasielsilva.portalceo.dto.TermoDTO;
import com.jaasielsilva.portalceo.dto.PoliticaSegurancaCriacaoDTO;
import com.jaasielsilva.portalceo.dto.TermoAceiteDTO;
import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.TermoAceite;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.TermoService;
import com.jaasielsilva.portalceo.service.TermoAuditoriaService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/termos")
public class TermosController {

    @Autowired
    private TermoService termoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TermoAuditoriaService termoAuditoriaService;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    // ===============================
    // PÁGINAS PRINCIPAIS
    // ===============================

    @GetMapping
    public String index(
            Model model,
            @RequestParam(name = "tipo", required = false) String tipoParam,
            @RequestParam(name = "sort", required = false, defaultValue = "vigenciaFim") String sort,
            @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            org.springframework.security.core.Authentication authentication) {
        // Configurações da página
        model.addAttribute("pageTitle", "Termos de Uso");
        model.addAttribute("pageSubtitle", "Gerenciamento de termos e políticas");
        model.addAttribute("moduleIcon", "fas fa-file-contract");
        model.addAttribute("moduleCSS", "termos");

        // Filtro por tipo (opcional)
        Termo.TipoTermo tipoSelecionado = null;
        if (tipoParam != null && !tipoParam.isEmpty()) {
            try {
                tipoSelecionado = Termo.TipoTermo.valueOf(tipoParam);
            } catch (IllegalArgumentException e) {
                // Ignora valores inválidos e segue com estatísticas gerais
                tipoSelecionado = null;
            }
        }

        // Buscar estatísticas gerais (o serviço não possui versão filtrada por tipo)
        EstatisticasTermosDTO estatisticas = termoService.buscarEstatisticas();

        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("tipoSelecionado", tipoSelecionado != null ? tipoSelecionado.name() : null);

        // Construir ordenação compatível com os campos da entidade
        String sortProperty;
        switch (sort) {
            case "titulo": sortProperty = "titulo"; break;
            case "versao": sortProperty = "versao"; break;
            case "tipo": sortProperty = "tipo"; break;
            case "status": sortProperty = "status"; break;
            case "vigenciaInicio": sortProperty = "dataVigenciaInicio"; break;
            case "vigenciaFim": sortProperty = "dataVigenciaFim"; break;
            default: sortProperty = "dataCriacao"; break;
        }

        org.springframework.data.domain.Sort.Direction direction =
                "desc".equalsIgnoreCase(order) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page - 1), Math.max(1, size), org.springframework.data.domain.Sort.by(direction, sortProperty)
        );

        org.springframework.data.domain.Page<Termo> pageResult = termoService.buscarTermosAtivosPage(tipoSelecionado, pageable);

        model.addAttribute("termosAtivos", termoService.converterParaDTO(pageResult.getContent()));
        model.addAttribute("page", pageResult.getNumber() + 1);
        model.addAttribute("size", pageResult.getSize());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("totalPages", Math.max(1, pageResult.getTotalPages()));
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        // Últimos aceites para atividades recentes (limite 10)
        model.addAttribute("ultimosAceites", termoService.buscarUltimosAceitesDTO(10));

        // Termo mais recente para card de informações
        java.util.Optional<TermoDTO> termoMaisRecente = termoService.buscarTermoMaisRecenteDTO();
        model.addAttribute("termoMaisRecente", termoMaisRecente.orElse(null));

        // Próxima revisão (termo ativo mais próximo do fim da vigência)
        java.util.Optional<TermoDTO> proximaRevisao = termoService.buscarProximoTermoExpiracaoDTO();
        model.addAttribute("proximaRevisao", proximaRevisao.orElse(null));

        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();

        java.util.Map<String, String> uso = new java.util.HashMap<>();
        uso.put("type", "link");
        uso.put("url", "/termos/uso");
        uso.put("label", "Termos de Uso");
        uso.put("icon", "fas fa-file-contract");
        pageActions.add(uso);

        java.util.Map<String, String> privacidade = new java.util.HashMap<>();
        privacidade.put("type", "link");
        privacidade.put("url", "/termos/privacidade");
        privacidade.put("label", "Privacidade");
        privacidade.put("icon", "fas fa-shield-alt");
        pageActions.add(privacidade);

        java.util.Map<String, String> historico = new java.util.HashMap<>();
        historico.put("type", "link");
        historico.put("url", "/termos/historico");
        historico.put("label", "Histórico");
        historico.put("icon", "fas fa-history");
        pageActions.add(historico);

        // Link Gerenciar (somente para usuários com permissão)
        boolean canManage = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> {
            String auth = a.getAuthority();
            return "ROLE_ADMIN".equals(auth) || "ROLE_MASTER".equals(auth) || "ROLE_TI_ADMIN".equals(auth) || "ROLE_COMPLIANCE".equals(auth);
        });
        model.addAttribute("canManage", canManage);
        if (canManage) {
            java.util.Map<String, String> gerenciar = new java.util.HashMap<>();
            gerenciar.put("type", "link");
            gerenciar.put("url", "/termos/gerenciar");
            gerenciar.put("label", "Gerenciar");
            gerenciar.put("icon", "fas fa-cogs");
            pageActions.add(gerenciar);
        }

        java.util.Map<String, String> aceites = new java.util.HashMap<>();
        aceites.put("type", "link");
        aceites.put("url", "/termos/aceites");
        aceites.put("label", "Aceites de Usuários");
        aceites.put("icon", "fas fa-users-check");
        pageActions.add(aceites);

        model.addAttribute("pageActions", pageActions);

        return "termos/index";
    }

    @GetMapping("/gerenciar")
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public String gerenciar(Model model) {
        model.addAttribute("pageTitle", "Gerenciar Políticas de Segurança");
        model.addAttribute("pageSubtitle", "Crie, envie para aprovação, publique e arquive políticas");
        model.addAttribute("moduleIcon", "fas fa-shield-alt");
        model.addAttribute("moduleCSS", "termos");
        return "termos/gerenciar";
    }

    @GetMapping("/uso")
    public String termosUso(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Termos de Uso");
        model.addAttribute("pageSubtitle", "Leia e aceite os termos de uso");
        model.addAttribute("moduleIcon", "fas fa-file-contract");
        model.addAttribute("moduleCSS", "termos");

        // Buscar termo de uso mais recente
        Optional<Termo> termoUso = termoService.buscarTermoMaisRecentePorTipo(Termo.TipoTermo.TERMOS_USO);
        if (termoUso.isPresent()) {
            model.addAttribute("termo", termoService.converterParaDTO(termoUso.get()));

            // Verificar se usuário já aceitou
            if (authentication != null) {
                Optional<Usuario> usuario = usuarioService.buscarPorEmail(authentication.getName());
                if (usuario.isPresent()) {
                    boolean jaAceitou = termoService.usuarioAceitouTermo(termoUso.get().getId(), usuario.get());
                    model.addAttribute("jaAceitou", jaAceitou);
                }
            }
        }

        return "termos/uso";
    }

    @GetMapping("/privacidade")
    public String politicaPrivacidade(Model model) {
        model.addAttribute("pageTitle", "Política de Privacidade");
        model.addAttribute("pageSubtitle", "Nossa política de privacidade e proteção de dados");
        model.addAttribute("moduleIcon", "fas fa-shield-alt");
        model.addAttribute("moduleCSS", "termos");

        // Buscar política de privacidade mais recente
        Optional<Termo> politica = termoService.buscarTermoMaisRecentePorTipo(Termo.TipoTermo.POLITICA_PRIVACIDADE);
        if (politica.isPresent()) {
            model.addAttribute("termo", termoService.converterParaDTO(politica.get()));
        }

        return "termos/privacidade";
    }

    @GetMapping("/historico")
    public String historico(Model model) {
        model.addAttribute("pageTitle", "Histórico de Versões");
        model.addAttribute("pageSubtitle", "Histórico de alterações dos termos");
        model.addAttribute("moduleIcon", "fas fa-history");
        model.addAttribute("moduleCSS", "termos");

        // Buscar todos os termos ordenados por data
        List<Termo> todosTermos = termoService.buscarTodos();
        model.addAttribute("termos", termoService.converterParaDTO(todosTermos));

        return "termos/historico";
    }

    @GetMapping("/aceites")
    public String aceites(Model model) {
        model.addAttribute("pageTitle", "Aceites de Usuários");
        model.addAttribute("pageSubtitle", "Controle de aceites dos termos pelos usuários");
        model.addAttribute("moduleIcon", "fas fa-users-check");
        model.addAttribute("moduleCSS", "termos");

        // Buscar estatísticas
        EstatisticasTermosDTO estatisticas = termoService.buscarEstatisticas();
        model.addAttribute("estatisticas", estatisticas);

        return "termos/aceites";
    }

    @GetMapping("/aceites/export")
    public ResponseEntity<byte[]> exportAceites(
            @RequestParam(name = "format", defaultValue = "csv") String format,
            @RequestParam(name = "status", required = false) String statusParam,
            @RequestParam(name = "termoId", required = false) Long termoId,
            @RequestParam(name = "inicio", required = false) java.time.LocalDate inicio,
            @RequestParam(name = "fim", required = false) java.time.LocalDate fim,
            @RequestParam(name = "usuario", required = false) String usuarioLike
    ) throws Exception {
        TermoAceite.StatusAceite status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try {
                status = TermoAceite.StatusAceite.valueOf(statusParam);
            } catch (IllegalArgumentException ignored) {}
        }

        java.util.List<TermoAceiteDTO> dados = termoService.buscarAceitesFiltradosDTO(status, termoId, inicio, fim, usuarioLike);

        String filename = "aceites_" + java.time.LocalDateTime.now().toString().replace(":","-") + "." + ("pdf".equalsIgnoreCase(format) ? "pdf" : "csv");

        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdf = gerarPdfAceites(dados);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdf);
        } else {
            String csv = gerarCsvAceites(dados);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(org.springframework.http.MediaType.valueOf("text/csv"))
                    .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String gerarCsvAceites(java.util.List<TermoAceiteDTO> dados) {
        StringBuilder sb = new StringBuilder();
        sb.append("Data Aceite;Usuário;Email;Termo;Versão;Status\n");
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (var d : dados) {
            String data = d.getDataAceite() != null ? fmt.format(d.getDataAceite()) : "";
            sb.append(escapeCsv(data)).append(';')
              .append(escapeCsv(nvl(d.getUsuarioNome()))).append(';')
              .append(escapeCsv(nvl(d.getUsuarioEmail()))).append(';')
              .append(escapeCsv(nvl(d.getTermoTitulo()))).append(';')
              .append(escapeCsv(nvl(d.getTermoVersao()))).append(';')
              .append(escapeCsv(nvl(d.getStatusDescricao()))).append('\n');
        }
        return sb.toString();
    }

    private String nvl(String s) { return s != null ? s : ""; }
    private String escapeCsv(String s) {
        String v = s != null ? s : "";
        if (v.contains(";") || v.contains("\"")) {
            v = '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    private byte[] gerarPdfAceites(java.util.List<TermoAceiteDTO> dados) throws Exception {
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
        document.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
        document.add(new com.lowagie.text.Paragraph("Relatório de Aceites", titleFont));
        document.add(new com.lowagie.text.Paragraph("Gerado em: " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(java.time.LocalDateTime.now())));
        document.add(new com.lowagie.text.Paragraph(" "));

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell("Data Aceite");
        table.addCell("Usuário");
        table.addCell("Email");
        table.addCell("Termo");
        table.addCell("Versão");
        table.addCell("Status");

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (var d : dados) {
            table.addCell(d.getDataAceite() != null ? fmt.format(d.getDataAceite()) : "");
            table.addCell(nvl(d.getUsuarioNome()));
            table.addCell(nvl(d.getUsuarioEmail()));
            table.addCell(nvl(d.getTermoTitulo()));
            table.addCell(nvl(d.getTermoVersao()));
            table.addCell(nvl(d.getStatusDescricao()));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    // ===============================
    // AÇÕES DE ACEITE
    // ===============================

    @PostMapping("/aceitar/{id}")
    @ResponseBody
    public ResponseEntity<?> aceitarTermo(@PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            Optional<Usuario> usuario = usuarioService.buscarPorEmail(authentication.getName());
            if (!usuario.isPresent()) {
                return ResponseEntity.badRequest().body("Usuário não encontrado");
            }

            String ipAceite = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            TermoAceite aceite = termoService.aceitarTermo(id, usuario.get(), ipAceite, userAgent);
            // Auditoria persistente de ação de aceite
            termoAuditoriaService.registrarAcao(aceite.getTermo(), authentication.getName(), null, ipAceite, "aceite");

            return ResponseEntity.ok().body("Termo aceito com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===============================
    // CRUD RÁPIDO – POLÍTICAS DE SEGURANÇA (API)
    // ===============================

    @GetMapping("/api/politicas")
    @ResponseBody
    public ResponseEntity<?> listarPoliticasSeguranca() {
        try {
            List<Termo> politicas = termoService.buscarPorTipo(Termo.TipoTermo.POLITICA_SEGURANCA);
            return ResponseEntity.ok(termoService.converterParaDTO(politicas));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/politica-seguranca")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public ResponseEntity<?> criarPoliticaSeguranca(@Valid @RequestBody PoliticaSegurancaCriacaoDTO req,
            Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body("Não autenticado");
            }

            var usuarioOpt = usuarioService.buscarPorEmail(authentication.getName());
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Usuário não encontrado");
            }

            // Monta TermoDTO completo, forçando tipo=POLITICA_SEGURANCA
            TermoDTO dto = new TermoDTO();
            dto.setTitulo(req.getTitulo());
            dto.setConteudo(req.getConteudo());
            dto.setVersao(req.getVersao());
            dto.setObservacoes(req.getObservacoes());
            dto.setObrigatorioAceite(req.isObrigatorioAceite());
            dto.setNotificarUsuarios(req.isNotificarUsuarios());
            dto.setDataVigenciaInicio(req.getDataVigenciaInicio());
            dto.setDataVigenciaFim(req.getDataVigenciaFim());
            dto.setTipo(com.jaasielsilva.portalceo.model.Termo.TipoTermo.POLITICA_SEGURANCA);

            var termoCriado = termoService.criarTermo(dto, usuarioOpt.get());
            termoAuditoriaService.registrarCriacao(termoCriado, authentication.getName());
            var dtoResposta = termoService.converterParaDTO(termoCriado);
            return ResponseEntity.ok(dtoResposta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/{id}/enviar-aprovacao")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public ResponseEntity<?> enviarAprovacao(@PathVariable Long id, Authentication authentication,
            HttpServletRequest request) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body("Não autenticado");
            }
            var termoOpt = termoService.buscarPorId(id);
            if (!termoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Termo não encontrado");
            }
            var termo = termoOpt.get();
            var statusAnterior = termo.getStatus();
            termoService.enviarParaAprovacao(id);
            termoAuditoriaService.registrarMudancaStatus(termo, statusAnterior, Termo.StatusTermo.PENDENTE_APROVACAO,
                    authentication.getName(), "envio para aprovação", getClientIpAddress(request), "envio_aprovacao");
            return ResponseEntity.ok("Política enviada para aprovação");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/{id}/aprovar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public ResponseEntity<?> aprovarPolitica(@PathVariable Long id, Authentication authentication,
            HttpServletRequest request) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body("Não autenticado");
            }

            var usuarioOpt = usuarioService.buscarPorEmail(authentication.getName());
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Usuário não encontrado");
            }

            var termoOpt = termoService.buscarPorId(id);
            if (!termoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Termo não encontrado");
            }
            var termo = termoOpt.get();
            var statusAnterior = termo.getStatus();
            termoService.aprovarTermo(id, usuarioOpt.get());
            termoAuditoriaService.registrarMudancaStatus(termo, statusAnterior, Termo.StatusTermo.APROVADO,
                    authentication.getName(), "aprovação", getClientIpAddress(request), "aprovar");
            return ResponseEntity.ok("Política aprovada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/{id}/publicar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public ResponseEntity<?> publicarPolitica(@PathVariable Long id, Authentication authentication,
            HttpServletRequest request) {
        try {
            var termoOpt = termoService.buscarPorId(id);
            if (!termoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Termo não encontrado");
            }
            var termo = termoOpt.get();
            var statusAnterior = termo.getStatus();
            termoService.publicarTermo(id);
            termoAuditoriaService.registrarMudancaStatus(termo, statusAnterior, Termo.StatusTermo.PUBLICADO,
                    authentication != null ? authentication.getName() : null, "publicação", getClientIpAddress(request),
                    "publicar");
            // Notificação WebSocket
            var termoAtualizadoOpt = termoService.buscarPorId(id);
            if (termoAtualizadoOpt.isPresent()) {
                var dto = termoService.converterParaDTO(termoAtualizadoOpt.get());
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("type", "termo_publicado");
                payload.put("termo", dto);
                messagingTemplate.convertAndSend("/topic/termos", payload);
            }
            return ResponseEntity.ok("Política publicada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/{id}/arquivar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public ResponseEntity<?> arquivarPolitica(@PathVariable Long id, Authentication authentication,
            HttpServletRequest request) {
        try {
            var termoOpt = termoService.buscarPorId(id);
            if (!termoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Termo não encontrado");
            }
            var termo = termoOpt.get();
            var statusAnterior = termo.getStatus();
            termoService.arquivarTermo(id);
            termoAuditoriaService.registrarMudancaStatus(termo, statusAnterior, Termo.StatusTermo.ARQUIVADO,
                    authentication != null ? authentication.getName() : null, "arquivamento",
                    getClientIpAddress(request), "arquivar");
            return ResponseEntity.ok("Política arquivada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    @PostMapping("/api/{id}/cancelar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN','MASTER','TI_ADMIN','COMPLIANCE')")
    public ResponseEntity<?> cancelarPolitica(@PathVariable Long id,
            @RequestParam(required = false) String motivo,
            Authentication authentication,
            HttpServletRequest request) {
        try {
            var termoOpt = termoService.buscarPorId(id);
            if (!termoOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Termo não encontrado");
            }
            var termo = termoOpt.get();
            var statusAnterior = termo.getStatus();
            termoService.cancelarTermo(id);
            String usuario = authentication != null ? authentication.getName() : null;
            termoAuditoriaService.registrarMudancaStatus(termo, statusAnterior, Termo.StatusTermo.CANCELADO, usuario,
                    motivo, getClientIpAddress(request), "cancelar");

            var termoAtualizadoOpt = termoService.buscarPorId(id);
            if (termoAtualizadoOpt.isPresent()) {
                var dto = termoService.converterParaDTO(termoAtualizadoOpt.get());
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("type", "termo_cancelado");
                payload.put("termo", dto);
                messagingTemplate.convertAndSend("/topic/termos", payload);
            }
            return ResponseEntity.ok("Política cancelada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}