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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/juridico/previdenciario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL','ROLE_JURIDICO')")
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

        model.addAttribute("pageTitle", "Processo Previdenciário #" + processo.getId());
        model.addAttribute("modoCriacao", false);
        model.addAttribute("processo", processo);
        model.addAttribute("dataAberturaFmt", processo.getDataAbertura() != null
                ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(processo.getDataAbertura())
                : "—");
        model.addAttribute("processoNumeroFmt", "Processo #" + processo.getId());
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

    private Usuario usuarioLogado(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        String username = authentication.getName();
        return usuarioRepository.findByEmail(username)
                .or(() -> usuarioRepository.findByMatricula(username))
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }
}
