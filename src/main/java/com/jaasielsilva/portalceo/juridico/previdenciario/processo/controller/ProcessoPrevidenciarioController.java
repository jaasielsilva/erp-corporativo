package com.jaasielsilva.portalceo.juridico.previdenciario.processo.controller;

import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity.TipoDocumentoProcesso;
import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.service.DocumentoProcessoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.historico.service.HistoricoProcessoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.service.ProcessoPrevidenciarioService;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflow;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.service.WorkflowService;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciarioStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoDecisaoResultado;

@Controller
@RequestMapping("/juridico/previdenciario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL','ROLE_JURIDICO','ROLE_ESTAGIARIO_JURIDICO')")
public class ProcessoPrevidenciarioController {

    private final ProcessoPrevidenciarioService processoService;
    private final WorkflowService workflowService;
    private final DocumentoProcessoService documentoService;
    private final HistoricoProcessoService historicoService;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Jurídico > Previdenciário (INSS)");
        model.addAttribute("processos", processoService.listar());
        return "juridico/previdenciario/lista";
    }

    @GetMapping("/novo")
    @PreAuthorize("hasAnyAuthority('MENU_JURIDICO_PREVIDENCIARIO_NOVO', 'ROLE_ADMIN', 'ROLE_MASTER')")
    public String novo(Model model, Authentication authentication) {
        List<EtapaWorkflow> etapas = workflowService.listarEtapasOrdenadas();
        Integer ordemCadastro = etapas.stream()
                .filter(e -> e.getCodigo() == EtapaWorkflowCodigo.CADASTRO)
                .map(EtapaWorkflow::getOrdem)
                .findFirst()
                .orElse(null);

        model.addAttribute("pageTitle", "Novo Processo Previdenciário");
        model.addAttribute("modoCriacao", true);
        model.addAttribute("processo", new ProcessoPrevidenciario());
        model.addAttribute("dataAberturaFmt", "—");
        model.addAttribute("processoNumeroFmt", "Novo processo");
        model.addAttribute("statusAtualFmt", "—");
        model.addAttribute("etapaAtualFmt", "CADASTRO");
        model.addAttribute("etapas", etapas);
        model.addAttribute("ordemEtapaAtual", ordemCadastro);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("usuarioPadraoId", usuarioLogado(authentication).getId());
        return "juridico/previdenciario/detalhe";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Long clienteId,
            @RequestParam Long responsavelId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario executor = usuarioLogado(authentication);
            ProcessoPrevidenciario salvo = processoService.criar(clienteId, responsavelId, executor);
            redirectAttributes.addFlashAttribute("sucesso", "Processo criado com sucesso");
            return "redirect:/juridico/previdenciario/" + salvo.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/juridico/previdenciario/novo";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model, Authentication authentication) {
        ProcessoPrevidenciario processo = processoService.buscarPorId(id);
        List<EtapaWorkflow> etapas = workflowService.listarEtapasOrdenadas();
        Integer ordemEtapaAtual = etapas.stream()
                .filter(e -> e.getCodigo() == processo.getEtapaAtual())
                .map(EtapaWorkflow::getOrdem)
                .findFirst()
                .orElse(null);
        Usuario executor = usuarioLogado(authentication);
        List<EtapaWorkflow> destinosPermitidos = workflowService
                .listarDestinosPermitidos(processo.getEtapaAtual(), executor, authentication);
        if (ordemEtapaAtual != null && destinosPermitidos != null && !destinosPermitidos.isEmpty()) {
            List<EtapaWorkflow> paraFrente = destinosPermitidos.stream()
                    .filter(d -> d.getOrdem() != null && d.getOrdem() > ordemEtapaAtual)
                    .sorted(Comparator.comparingInt(EtapaWorkflow::getOrdem))
                    .toList();
            List<EtapaWorkflow> paraTras = destinosPermitidos.stream()
                    .filter(d -> d.getOrdem() != null && d.getOrdem() < ordemEtapaAtual)
                    .sorted(Comparator.comparingInt(EtapaWorkflow::getOrdem).reversed())
                    .toList();
            destinosPermitidos = List.copyOf(
                    java.util.stream.Stream.concat(paraFrente.stream(), paraTras.stream()).toList());
        }

        String ano = processo.getDataAbertura() != null ? String.valueOf(processo.getDataAbertura().getYear()) : String.valueOf(LocalDate.now().getYear());
        String idFormatado = String.format("%04d", processo.getId()) + "/" + ano;

        model.addAttribute("pageTitle", "Processo Previdenciário " + idFormatado);
        model.addAttribute("modoCriacao", false);
        model.addAttribute("processo", processo);
        model.addAttribute("dataAberturaFmt", processo.getDataAbertura() != null
                ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(processo.getDataAbertura())
                : "—");
        model.addAttribute("processoNumeroFmt", "Processo " + idFormatado);
        model.addAttribute("statusAtualFmt",
                processo.getStatusAtual() != null ? processo.getStatusAtual().name() : "—");
        model.addAttribute("etapaAtualFmt", processo.getEtapaAtual() != null ? processo.getEtapaAtual().name() : "—");
        model.addAttribute("etapas", etapas);
        model.addAttribute("ordemEtapaAtual", ordemEtapaAtual);
        model.addAttribute("destinosPermitidos", destinosPermitidos);
        model.addAttribute("historico", historicoService.listarPorProcesso(id));
        model.addAttribute("documentos", documentoService.listarPorProcesso(id));
        model.addAttribute("permiteAnexo", workflowService.permiteAnexo(processo.getEtapaAtual()));
        model.addAttribute("tiposDocumento", TipoDocumentoProcesso.values());
        model.addAttribute("etapaAtual", processo.getEtapaAtual());
        model.addAttribute("tiposDecisao", ProcessoDecisaoResultado.values());
        return "juridico/previdenciario/detalhe";
    }

    @PostMapping("/{id}/avancar-etapa")
    public String avancarEtapa(@PathVariable Long id,
            @RequestParam(value = "destino", required = false) EtapaWorkflowCodigo destino,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario executor = usuarioLogado(authentication);
            workflowService.avancarEtapa(id, destino, executor, authentication);
            redirectAttributes.addFlashAttribute("sucesso", "Etapa avançada com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/juridico/previdenciario/" + id;
    }

    @PostMapping("/{id}/protocolo-inss")
    public String salvarProtocoloInss(@PathVariable Long id,
            @RequestParam(value = "numeroProtocolo", required = false) String numeroProtocolo,
            @RequestParam(value = "dataProtocolo", required = false) LocalDate dataProtocolo,
            @RequestParam(value = "urlMeuInss", required = false) String urlMeuInss,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario executor = usuarioLogado(authentication);
            processoService.atualizarProtocoloInss(id, numeroProtocolo, dataProtocolo, urlMeuInss, executor);
            redirectAttributes.addFlashAttribute("sucesso", "Dados do protocolo salvos com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/juridico/previdenciario/" + id;
    }

    @PostMapping("/{id}/decisao")
    public String salvarDecisao(@PathVariable Long id,
            @RequestParam(value = "resultado", required = false) ProcessoDecisaoResultado resultado,
            @RequestParam(value = "ganhouCausa", required = false) Boolean ganhouCausa,
            @RequestParam(value = "valorCausa", required = false) String valorCausaStr,
            @RequestParam(value = "valorConcedido", required = false) String valorConcedidoStr,
            @RequestParam(value = "dataDecisao", required = false) LocalDate dataDecisao,
            @RequestParam(value = "observacaoDecisao", required = false) String observacaoDecisao,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario executor = usuarioLogado(authentication);
            BigDecimal valorCausa = parseDecimal(valorCausaStr);
            BigDecimal valorConcedido = parseDecimal(valorConcedidoStr);
            processoService.atualizarDecisao(id, resultado, ganhouCausa, valorCausa, valorConcedido, dataDecisao,
                    observacaoDecisao, executor);
            redirectAttributes.addFlashAttribute("sucesso", "Decisão registrada com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/juridico/previdenciario/" + id;
    }

    @GetMapping("/api/processos/{id}")
    @ResponseBody
    public ResponseEntity<?> obterDetalhesApi(@PathVariable Long id) {
        try {
            ProcessoPrevidenciario p = processoService.buscarPorId(id);
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            String ano = p.getDataAbertura() != null ? String.valueOf(p.getDataAbertura().getYear()) : String.valueOf(LocalDate.now().getYear());
            String idFormatado = String.format("%04d", p.getId()) + "/" + ano;
            m.put("numero", p.getNumeroProtocolo() != null ? p.getNumeroProtocolo() : ("Processo " + idFormatado));
            m.put("tipo", "Previdenciária");
            m.put("tribunal", "—");
            m.put("parte", "Instituto Nacional do Seguro Social – INSS");
            m.put("status", p.getStatusAtual() != null ? p.getStatusAtual().name() : null);
            m.put("etapa", p.getEtapaAtual() != null ? p.getEtapaAtual().name() : null);
            m.put("dataAbertura", p.getDataAbertura());
            m.put("dataProtocolo", p.getDataProtocolo());
            m.put("urlMeuInss", p.getUrlMeuInss());
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("erro", "Processo previdenciário não encontrado"));
        }
    }

    @GetMapping("/api/processos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarAjax(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "search", required = false) String search) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100), org.springframework.data.domain.Sort.by("dataAbertura").descending());
        
        org.springframework.data.domain.Page<ProcessoPrevidenciario> pagina = processoService.buscarComFiltros(status, search, pageable);

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", pagina.getContent().stream().map(this::toDTO).collect(Collectors.toList()));
        resp.put("currentPage", pagina.getNumber());
        resp.put("totalPages", pagina.getTotalPages());
        resp.put("totalElements", pagina.getTotalElements());
        resp.put("hasPrevious", pagina.hasPrevious());
        resp.put("hasNext", pagina.hasNext());
        
        return ResponseEntity.ok(resp);
    }
    
    private Map<String, Object> toDTO(ProcessoPrevidenciario p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("cliente", Map.of("nome", p.getCliente() != null ? p.getCliente().getNome() : "—"));
        m.put("etapaAtual", p.getEtapaAtual() != null ? p.getEtapaAtual().name() : "—");
        m.put("statusAtual", p.getStatusAtual() != null ? p.getStatusAtual().name() : "—");
        m.put("responsavel", Map.of("nome", p.getResponsavel() != null ? p.getResponsavel().getNome() : "—"));
        m.put("dataAbertura", p.getDataAbertura());
        m.put("resultadoDecisao", p.getResultadoDecisao());
        m.put("valorCausa", p.getValorCausa());
        return m;
    }

    @GetMapping("/api/processos/{id}/historico")
    @ResponseBody
    public ResponseEntity<?> obterHistoricoApi(@PathVariable Long id) {
        try {
            var lista = historicoService.listarPorProcesso(id).stream().map(h -> {
                Map<String, Object> m = new HashMap<>();
                m.put("data", h.getDataEvento());
                m.put("tipo", "ANDAMENTO");
                m.put("titulo", h.getEvento());
                m.put("descricao", h.getObservacao());
                m.put("usuario", h.getUsuario() != null ? h.getUsuario().getNome() : null);
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(List.of());
        }
    }

    @GetMapping("/api/processos/{id}/documentos")
    @ResponseBody
    public ResponseEntity<?> obterDocumentosApi(@PathVariable Long id) {
        try {
            var lista = documentoService.listarPorProcesso(id).stream().map(d -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", d.getId());
                m.put("tipo", d.getTipoDocumento() != null ? d.getTipoDocumento().name() : null);
                m.put("dataUpload", d.getDataUpload());
                m.put("enviadoPor", d.getEnviadoPor() != null ? d.getEnviadoPor().getNome() : null);
                m.put("caminhoArquivo", d.getCaminhoArquivo());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(List.of());
        }
    }

    @PostMapping("/api/processos/{id}/reabrir")
    @ResponseBody
    public ResponseEntity<?> reabrirApi(@PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            Usuario executor = usuarioLogado(authentication);
            String justificativa = body.get("justificativa") != null ? String.valueOf(body.get("justificativa")).trim() : null;
            String destinoStr = body.get("statusDestino") != null ? String.valueOf(body.get("statusDestino")).trim() : "EM_ANDAMENTO";
            ProcessoPrevidenciarioStatus destino = null;
            try {
                destino = ProcessoPrevidenciarioStatus.valueOf(destinoStr);
            } catch (Exception ignored) {}
            processoService.reabrir(id, destino, justificativa, executor);
            return ResponseEntity.ok(Map.of("sucesso", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/api/processos/{id}/ganho")
    @ResponseBody
    public ResponseEntity<?> registrarGanhoApi(@PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            Usuario executor = usuarioLogado(authentication);
            BigDecimal valor = parseDecimal(body.get("valor") != null ? String.valueOf(body.get("valor")) : null);
            LocalDate vencimento = null;
            try {
                String venc = body.get("vencimento") != null ? String.valueOf(body.get("vencimento")) : null;
                if (venc != null && !venc.isBlank()) vencimento = LocalDate.parse(venc);
            } catch (Exception ignored) {}
            String numeroDocumento = body.get("numeroDocumento") != null ? String.valueOf(body.get("numeroDocumento")) : null;
            String observacoes = body.get("observacoes") != null ? String.valueOf(body.get("observacoes")) : null;
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Valor inválido"));
            }
            processoService.registrarGanho(id, valor, vencimento, numeroDocumento, observacoes, executor);
            return ResponseEntity.ok(Map.of("sucesso", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    private Usuario usuarioLogado(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        String username = authentication.getName();
        return usuarioRepository.findByEmail(username)
                .or(() -> usuarioRepository.findByMatricula(username))
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }

    private BigDecimal parseDecimal(String v) {
        if (v == null) return null;
        String s = v.trim().replace(".", "").replace(",", ".");
        if (s.isBlank()) return null;
        try {
            return new BigDecimal(s);
        } catch (Exception ex) {
            return null;
        }
    }
}
