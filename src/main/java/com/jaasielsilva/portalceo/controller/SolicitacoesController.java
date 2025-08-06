package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.service.SolicitacaoAcessoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/solicitacoes")
public class SolicitacoesController {

    @Autowired
    private SolicitacaoAcessoService solicitacaoAcessoService;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Listar todas as solicitações
     */
    @GetMapping
    public String listarSolicitacoes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataSolicitacao").descending());
        Page<SolicitacaoAcesso> solicitacoes;

        if (status != null && !status.trim().isEmpty()) {
            StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status.toUpperCase());
            solicitacoes = solicitacaoAcessoService.listarPorStatus(statusEnum, pageable);
            model.addAttribute("statusFiltro", status);
        } else if (busca != null && !busca.trim().isEmpty()) {
            solicitacoes = solicitacaoAcessoService.buscarPorTexto(busca, pageable);
            model.addAttribute("busca", busca);
        } else {
            solicitacoes = solicitacaoAcessoService.listarTodas(pageable);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", solicitacoes.getTotalPages());
        model.addAttribute("statusOptions", StatusSolicitacao.values());

        Map<String, Object> estatisticas = solicitacaoAcessoService.obterEstatisticas();
        model.addAttribute("estatisticas", estatisticas);

        return "solicitacoes/listar";
    }

    /**
     * Exibir formulário de nova solicitação
     */
   @GetMapping("/nova")
public String exibirFormularioNova(Model model, Principal principal) {
    System.out.println("===== INÍCIO DO MÉTODO /nova =====");

    try {
        if (principal == null || principal.getName() == null) {
            System.out.println("Principal ou nome do usuário é nulo.");
            return "redirect:/login";
        }

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            System.out.println("Usuário não encontrado.");
            return "redirect:/login";
        }

        System.out.println("Usuário logado: " + usuarioLogado.getEmail());

        // Cria instância da solicitação para o formulário
        SolicitacaoAcesso solicitacao = new SolicitacaoAcesso();
        solicitacao.setColaborador(new Colaborador());

        // Adiciona no model
        model.addAttribute("solicitacaoAcesso", solicitacao);
        model.addAttribute("usuarioLogado", usuarioLogado);

        // Não carregar colaboradores na renderização inicial para evitar loop infinito
        // Os colaboradores serão carregados via AJAX
        model.addAttribute("colaboradores", java.util.Collections.emptyList());

        return "solicitacoes/nova";

    } catch (Exception e) {
        System.out.println("##### ERRO AO CARREGAR FORMULÁRIO /nova #####");
        e.printStackTrace();
        return "redirect:/solicitacoes?erro=Erro+ao+carregar+formulario";
    }
}


    /**
     * Processar nova solicitação
     */
    @PostMapping("/nova")
    public String processarNovaSolicitacao(@Valid @ModelAttribute("solicitacaoAcesso") SolicitacaoAcesso solicitacao,
            BindingResult result,
            @RequestParam(value = "modulos", required = false) List<String> modulosSelecionados,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        try {
            if (modulosSelecionados == null || modulosSelecionados.isEmpty()) {
                result.rejectValue("modulos", "error.modulos", "Selecione pelo menos um módulo");
            }

            if (result.hasErrors()) {
                List<Colaborador> colaboradores = colaboradorService.listarTodos();
                model.addAttribute("colaboradores", colaboradores);
                model.addAttribute("usuarioLogado", usuarioLogado);
                return "solicitacoes/nova";
            }

            Set<SolicitacaoAcesso.ModuloSistema> modulos = modulosSelecionados.stream()
                    .map(SolicitacaoAcesso.ModuloSistema::valueOf)
                    .collect(java.util.stream.Collectors.toSet());
            solicitacao.setModulos(modulos);

            Colaborador colaborador = colaboradorService.findById(solicitacao.getColaborador().getId());
            solicitacao.setColaborador(colaborador);

            SolicitacaoAcesso solicitacaoSalva = solicitacaoAcessoService.criarSolicitacao(solicitacao, usuarioLogado);

            redirectAttributes.addFlashAttribute("mensagem",
                    "Solicitação criada com sucesso! Protocolo: " + solicitacaoSalva.getProtocolo());

            return "redirect:/solicitacoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar solicitação: " + e.getMessage());
            return "redirect:/solicitacoes/nova";
        }
    }

    /**
     * Exibir detalhes da solicitação
     */
    @GetMapping("/{id}")
    public String exibirDetalhes(@PathVariable Long id,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<SolicitacaoAcesso> solicitacaoOpt = solicitacaoAcessoService.buscarPorId(id);
        if (solicitacaoOpt.isEmpty()) {
            return "redirect:/solicitacoes?erro=Solicitação não encontrada";
        }

        SolicitacaoAcesso solicitacao = solicitacaoOpt.get();
        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "solicitacoes/detalhes";
    }

    /**
     * Aprovar solicitação
     */
    @PostMapping("/{id}/aprovar")
    public String aprovarSolicitacao(@PathVariable Long id,
            @RequestParam String nivelAprovado,
            @RequestParam(required = false) String emailCorporativo,
            @RequestParam(required = false) String observacoesAprovador,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para aprovar solicitações");
            return "redirect:/solicitacoes";
        }

        try {
            SolicitacaoAcesso.NivelAcesso nivel = SolicitacaoAcesso.NivelAcesso.valueOf(nivelAprovado);
            solicitacaoAcessoService.aprovarSolicitacao(id, usuarioLogado, nivel, emailCorporativo,
                    observacoesAprovador);

            redirectAttributes.addFlashAttribute("mensagem", "Solicitação aprovada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao aprovar solicitação: " + e.getMessage());
        }

        return "redirect:/solicitacoes";
    }

    /**
     * Rejeitar solicitação
     */
    @PostMapping("/{id}/rejeitar")
    public String rejeitarSolicitacao(@PathVariable Long id,
            @RequestParam String observacoesAprovador,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para rejeitar solicitações");
            return "redirect:/solicitacoes";
        }

        try {
            solicitacaoAcessoService.rejeitarSolicitacao(id, usuarioLogado, observacoesAprovador);
            redirectAttributes.addFlashAttribute("mensagem", "Solicitação rejeitada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao rejeitar solicitação: " + e.getMessage());
        }

        return "redirect:/solicitacoes";
    }

    /**
     * Carregar colaboradores via AJAX
     */
    @GetMapping("/colaboradores")
    @ResponseBody
    public List<Colaborador> carregarColaboradores() {
        try {
            return colaboradorService.listarTodos();
        } catch (Exception e) {
            System.out.println("Erro ao carregar colaboradores via AJAX: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Buscar solicitação por protocolo (AJAX)
     */
    @GetMapping("/buscar-protocolo")
    @ResponseBody
    public Map<String, Object> buscarPorProtocolo(@RequestParam String protocolo) {
        Map<String, Object> response = new java.util.HashMap<>();

        try {
            Optional<SolicitacaoAcesso> solicitacao = solicitacaoAcessoService.buscarPorProtocolo(protocolo);

            if (solicitacao.isPresent()) {
                response.put("success", true);
                response.put("solicitacao", solicitacao.get());
            } else {
                response.put("success", false);
                response.put("message", "Protocolo não encontrado");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao buscar protocolo: " + e.getMessage());
        }

        return response;
    }

    /**
     * Listar solicitações do usuário logado
     */
    @GetMapping("/minhas")
    public String listarMinhasSolicitacoes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataSolicitacao").descending());
        Page<SolicitacaoAcesso> solicitacoes;

        if (status != null && !status.trim().isEmpty()) {
            StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status);
            solicitacoes = solicitacaoAcessoService.listarPorUsuarioEStatus(usuarioLogado, statusEnum, pageable);
            model.addAttribute("statusFiltro", status);
        } else {
            solicitacoes = solicitacaoAcessoService.listarPorUsuario(usuarioLogado, pageable);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", solicitacoes.getTotalPages());
        model.addAttribute("statusOptions", StatusSolicitacao.values());

        return "solicitacoes/minhas";
    }

    /**
     * Listar solicitações pendentes (para aprovadores)
     */
    @GetMapping("/pendentes")
    public String listarSolicitacoesPendentes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String busca,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            return "redirect:/dashboard?erro=Acesso negado";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataSolicitacao").ascending());
        Page<SolicitacaoAcesso> solicitacoes;

        if (busca != null && !busca.trim().isEmpty()) {
            solicitacoes = solicitacaoAcessoService.buscarPendentesPorTexto(busca, pageable);
            model.addAttribute("busca", busca);
        } else {
            solicitacoes = solicitacaoAcessoService.listarPendentes(pageable);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", solicitacoes.getTotalPages());

        return "solicitacoes/pendentes";
    }

    /**
     * Exibir formulário de aprovação
     */
    @GetMapping("/aprovar/{id}")
    public String exibirFormularioAprovacao(@PathVariable Long id,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            return "redirect:/dashboard?erro=Acesso negado";
        }

        Optional<SolicitacaoAcesso> solicitacaoOpt = solicitacaoAcessoService.buscarPorId(id);
        if (solicitacaoOpt.isEmpty()) {
            return "redirect:/solicitacoes/pendentes?erro=Solicitação não encontrada";
        }

        SolicitacaoAcesso solicitacao = solicitacaoOpt.get();

        if (!solicitacao.isPendente()) {
            return "redirect:/solicitacoes/" + id + "?erro=Solicitação já foi processada";
        }

        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "solicitacoes/aprovar";
    }

    /**
     * Processar aprovação da solicitação
     */
    @PostMapping("/aprovar/{id}")
    public String processarAprovacao(@PathVariable Long id,
            @RequestParam("decisao") String decisao,
            @RequestParam(required = false) String nivelAprovado,
            @RequestParam(required = false) String emailCorporativo,
            @RequestParam(required = false) String observacoesAprovador,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            redirectAttributes.addFlashAttribute("erro", "Acesso negado");
            return "redirect:/dashboard";
        }

        try {
            if ("APROVADO".equals(decisao) || "APROVADO_PARCIAL".equals(decisao)) {
                // Validar campos obrigatórios para aprovação
                if (nivelAprovado == null || emailCorporativo == null || emailCorporativo.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("erro",
                            "Nível de acesso e email corporativo são obrigatórios para aprovação");
                    return "redirect:/solicitacoes/aprovar/" + id;
                }

                SolicitacaoAcesso.NivelAcesso nivel = SolicitacaoAcesso.NivelAcesso.valueOf(nivelAprovado);
                solicitacaoAcessoService.aprovarSolicitacao(id, usuarioLogado, nivel, emailCorporativo,
                        observacoesAprovador);

                redirectAttributes.addFlashAttribute("mensagem", "Solicitação aprovada com sucesso!");

            } else if ("REJEITADO".equals(decisao)) {
                if (observacoesAprovador == null || observacoesAprovador.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("erro", "Motivo da rejeição é obrigatório");
                    return "redirect:/solicitacoes/aprovar/" + id;
                }

                solicitacaoAcessoService.rejeitarSolicitacao(id, usuarioLogado, observacoesAprovador);

                redirectAttributes.addFlashAttribute("mensagem", "Solicitação rejeitada");
            }

            return "redirect:/solicitacoes/pendentes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao processar aprovação: " + e.getMessage());
            return "redirect:/solicitacoes/aprovar/" + id;
        }
    }

    /**
     * Criar usuário após aprovação
     */
    @PostMapping("/criar-usuario/{id}")
    public String criarUsuario(@PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            redirectAttributes.addFlashAttribute("erro", "Acesso negado");
            return "redirect:/dashboard";
        }

        try {
            Usuario novoUsuario = solicitacaoAcessoService.criarUsuarioAposAprovacao(id);

            redirectAttributes.addFlashAttribute("mensagem",
                    "Usuário criado com sucesso! Credenciais enviadas por email para: " + novoUsuario.getEmail());

            return "redirect:/solicitacoes/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar usuário: " + e.getMessage());
            return "redirect:/solicitacoes/" + id;
        }
    }

    /**
     * Dashboard de solicitações
     */
    @GetMapping("/dashboard")
    public String dashboardSolicitacoes(Principal principal, Model model) {
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            return "redirect:/dashboard?erro=Acesso negado";
        }

        // Obter estatísticas
        Map<String, Object> estatisticas = solicitacaoAcessoService.obterEstatisticas();
        model.addAttribute("estatisticas", estatisticas);

        // Solicitações urgentes
        List<SolicitacaoAcesso> urgentes = solicitacaoAcessoService.buscarUrgentes();
        model.addAttribute("solicitacoesUrgentes", urgentes);

        // Solicitações que precisam de atenção
        List<SolicitacaoAcesso> atencao = solicitacaoAcessoService.buscarQueNecessitamAtencao();
        model.addAttribute("solicitacoesAtencao", atencao);

        // Últimas atividades
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dataSolicitacao").descending());
        Page<SolicitacaoAcesso> ultimasAtividades = solicitacaoAcessoService.listarTodas(pageable);
        model.addAttribute("ultimasAtividades", ultimasAtividades.getContent());

        return "solicitacoes/dashboard";
    }

    /**
     * Listar todas as solicitações (para administradores)
     */
    @GetMapping("/todas")
    public String listarTodasSolicitacoes(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            Principal principal,
            Model model) {

        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (!usuarioLogado.podeGerenciarUsuarios()) {
            return "redirect:/dashboard?erro=Acesso negado";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("dataSolicitacao").descending());
        Page<SolicitacaoAcesso> solicitacoes;

        if (busca != null && !busca.trim().isEmpty()) {
            solicitacoes = solicitacaoAcessoService.buscarPorTexto(busca, pageable);
            model.addAttribute("busca", busca);
        } else if (status != null && !status.trim().isEmpty()) {
            StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status);
            solicitacoes = solicitacaoAcessoService.listarPorStatus(statusEnum, pageable);
            model.addAttribute("statusFiltro", status);
        } else {
            solicitacoes = solicitacaoAcessoService.listarTodas(pageable);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", solicitacoes.getTotalPages());
        model.addAttribute("statusOptions", StatusSolicitacao.values());

        return "solicitacoes/todas";
    }
}
