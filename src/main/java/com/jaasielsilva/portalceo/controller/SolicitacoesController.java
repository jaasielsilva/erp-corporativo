package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.repository.SolicitacaoAcessoRepository;
import com.jaasielsilva.portalceo.service.SolicitacaoAcessoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.NotificationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.jaasielsilva.portalceo.dto.SolicitacaoSimpleDTO;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private SolicitacaoAcessoRepository solicitacaoAcessoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ColaboradorService colaboradorService;
    
    @Autowired
    private NotificationService notificationService;

    /**
 * Listar todas as solicitações (página principal)
 */
@GetMapping
public String listarSolicitacoes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String busca,
        Principal principal,
        Model model) {

    // Buscar usuário logado
    Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
    if (usuarioLogado == null) {
        return "redirect:/login";
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("dataSolicitacao").descending());
    Page<SolicitacaoAcesso> solicitacoes;

    try {
        // Filtros: status + busca, apenas status, apenas busca ou nenhum filtro
        if (status != null && !status.isEmpty() && busca != null && !busca.isEmpty()) {
            Page<SolicitacaoAcesso> todasPorTexto = solicitacaoAcessoService.buscarPorTexto(busca, pageable);
            StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status);
            List<SolicitacaoAcesso> filtradas = todasPorTexto.getContent().stream()
                    .filter(s -> s.getStatus() == statusEnum)
                    .collect(Collectors.toList());
            solicitacoes = new PageImpl<>(filtradas, pageable, filtradas.size());

        } else if (status != null && !status.isEmpty()) {
            StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status);
            solicitacoes = solicitacaoAcessoService.listarPorStatus(statusEnum, pageable);

        } else if (busca != null && !busca.isEmpty()) {
            solicitacoes = solicitacaoAcessoService.buscarPorTexto(busca, pageable);

        } else {
            solicitacoes = solicitacaoAcessoService.listarTodas(pageable);
        }
    } catch (Exception e) {
        System.err.println("Erro ao buscar solicitações: " + e.getMessage());
        e.printStackTrace();
        solicitacoes = new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    // Obter estatísticas gerais
    Map<String, Object> estatisticas;
    try {
        estatisticas = solicitacaoAcessoService.obterEstatisticas();
    } catch (Exception e) {
        System.err.println("Erro ao obter estatísticas: " + e.getMessage());
        estatisticas = new HashMap<>();
        estatisticas.put("solicitacoesMes", 0L);
        estatisticas.put("totalPendentes", 0L);
        estatisticas.put("totalAprovadas", 0L);
        estatisticas.put("totalRejeitadas", 0L);
        estatisticas.put("totalConcluidas", 0L);
    }

    // Extrair valores para gráfico de status
    Long pendentes = (Long) estatisticas.getOrDefault("totalPendentes", 0L);
    Long aprovadas = (Long) estatisticas.getOrDefault("totalAprovadas", 0L);
    Long rejeitadas = (Long) estatisticas.getOrDefault("totalRejeitadas", 0L);
    Long concluidas = (Long) estatisticas.getOrDefault("totalConcluidas", 0L);

    // Obter dados para gráfico de tempo (últimos 6 meses)
    List<String> nomesMeses = solicitacaoAcessoService.obterNomesUltimosMeses(6);

    // Adicionar atributos ao model
    model.addAttribute("valores", List.of(pendentes, aprovadas, rejeitadas, concluidas));
    model.addAttribute("estatisticas", estatisticas);
    model.addAttribute("nomesMeses", nomesMeses);
    model.addAttribute("solicitacoes", solicitacoes);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", solicitacoes.getTotalPages());
    model.addAttribute("statusFiltro", status);
    model.addAttribute("busca", busca);
    model.addAttribute("statusOptions", StatusSolicitacao.values());
    model.addAttribute("usuarioLogado", usuarioLogado);

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
     * Exibir detalhes de usuário relacionado à solicitação
     */
    @GetMapping("/detalhes-usuario/{id}")
    public String exibirDetalhesUsuario(@PathVariable Long id,
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

        // Verificar se a solicitação tem usuário criado
        if (solicitacao.getUsuarioCriado() == null) {
            return "redirect:/solicitacoes/" + id + "?erro=Usuário ainda não foi criado para esta solicitação";
        }

        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("usuario", solicitacao.getUsuarioCriado());
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "solicitacoes/detalhes-usuario";
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
            try {
                StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status.toUpperCase());
                solicitacoes = solicitacaoAcessoService.listarPorUsuarioEStatus(usuarioLogado, statusEnum, pageable);
                model.addAttribute("statusFiltro", status);
            } catch (IllegalArgumentException e) {
                // Status inválido, listar todas do usuário
                solicitacoes = solicitacaoAcessoService.listarPorUsuario(usuarioLogado, pageable);
            }
        } else {
            solicitacoes = solicitacaoAcessoService.listarPorUsuario(usuarioLogado, pageable);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", solicitacoes.getTotalPages());
        model.addAttribute("statusOptions", StatusSolicitacao.values());
        model.addAttribute("usuarioLogado", usuarioLogado);

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

        // Verificação de usuário otimizada
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
            if (usuarioLogado == null) {
                return "redirect:/login";
            }

            if (!usuarioLogado.podeGerenciarUsuarios()) {
                return "redirect:/dashboard?erro=Acesso negado";
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
            return "redirect:/login";
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
        model.addAttribute("usuarioLogado", usuarioLogado);

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
                SolicitacaoAcesso solicitacao = solicitacaoAcessoService.aprovarSolicitacao(id, usuarioLogado, nivel, emailCorporativo,
                        observacoesAprovador);
                
                // Enviar notificação para o solicitante
                if (solicitacao != null && solicitacao.getColaborador() != null && solicitacao.getColaborador().getEmail() != null) {
                    // Buscar usuário pelo email do colaborador (se existir)
                    usuarioService.buscarPorEmail(solicitacao.getColaborador().getEmail())
                        .ifPresent(usuario -> {
                            notificationService.notifyAccessRequestApproved(usuario, solicitacao.getProtocolo());
                        });
                }
                
                // Notificar administradores sobre a aprovação
                List<Usuario> admins = usuarioService.buscarUsuariosComPermissaoGerenciarUsuarios();
                for (Usuario admin : admins) {
                    if (!admin.equals(usuarioLogado)) { // Não notificar quem aprovou
                        notificationService.createNotification(
                            "access_request_approved",
                            "Solicitação Aprovada",
                            String.format("Solicitação %s foi aprovada por %s", solicitacao.getProtocolo(), usuarioLogado.getNome()),
                            com.jaasielsilva.portalceo.model.Notification.Priority.MEDIUM,
                            admin
                        );
                    }
                }

                redirectAttributes.addFlashAttribute("mensagem", "Solicitação aprovada com sucesso!");

            } else if ("REJEITADO".equals(decisao)) {
                if (observacoesAprovador == null || observacoesAprovador.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("erro", "Motivo da rejeição é obrigatório");
                    return "redirect:/solicitacoes/aprovar/" + id;
                }

                SolicitacaoAcesso solicitacaoRejeitada = solicitacaoAcessoService.rejeitarSolicitacao(id, usuarioLogado, observacoesAprovador);
                
                // Enviar notificação para o solicitante
                if (solicitacaoRejeitada != null && solicitacaoRejeitada.getColaborador() != null && solicitacaoRejeitada.getColaborador().getEmail() != null) {
                    // Buscar usuário pelo email do colaborador (se existir)
                    usuarioService.buscarPorEmail(solicitacaoRejeitada.getColaborador().getEmail())
                        .ifPresent(usuario -> {
                            notificationService.notifyAccessRequestRejected(usuario, solicitacaoRejeitada.getProtocolo(), observacoesAprovador);
                        });
                }
                
                // Notificar administradores sobre a rejeição
                List<Usuario> admins = usuarioService.buscarUsuariosComPermissaoGerenciarUsuarios();
                for (Usuario admin : admins) {
                    if (!admin.equals(usuarioLogado)) { // Não notificar quem rejeitou
                        notificationService.createNotification(
                            "access_request_rejected",
                            "Solicitação Rejeitada",
                            String.format("Solicitação %s foi rejeitada por %s", solicitacaoRejeitada.getProtocolo(), usuarioLogado.getNome()),
                            com.jaasielsilva.portalceo.model.Notification.Priority.MEDIUM,
                            admin
                        );
                    }
                }

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
            
            // Buscar a solicitação para obter informações
            Optional<SolicitacaoAcesso> solicitacaoOpt = solicitacaoAcessoService.buscarPorId(id);
            if (solicitacaoOpt.isPresent()) {
                SolicitacaoAcesso solicitacao = solicitacaoOpt.get();
                
                // Notificar o novo usuário sobre a criação da conta
                notificationService.createNotification(
                    "user_account_created",
                    "Conta Criada com Sucesso!",
                    String.format("Sua conta foi criada! Protocolo: %s. Verifique seu email para as credenciais de acesso.", solicitacao.getProtocolo()),
                    com.jaasielsilva.portalceo.model.Notification.Priority.HIGH,
                    novoUsuario
                );
                
                // Notificar administradores sobre a criação do usuário
                List<Usuario> admins = usuarioService.buscarUsuariosComPermissaoGerenciarUsuarios();
                for (Usuario admin : admins) {
                    if (!admin.equals(usuarioLogado)) { // Não notificar quem criou
                        notificationService.createNotification(
                            "user_created_by_admin",
                            "Novo Usuário Criado",
                            String.format("Usuário %s foi criado por %s (Protocolo: %s)", novoUsuario.getNome(), usuarioLogado.getNome(), solicitacao.getProtocolo()),
                            com.jaasielsilva.portalceo.model.Notification.Priority.MEDIUM,
                            admin
                        );
                    }
                }
            }

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

        try {
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

            // Valores para o gráfico de rosca
            List<Long> valores = solicitacaoAcessoService.obterValoresGraficoStatus();
            model.addAttribute("valores", valores);

        } catch (Exception e) {
            System.out.println("Erro ao carregar dashboard: " + e.getMessage());
            // Adicionar valores padrão em caso de erro
            model.addAttribute("estatisticas", new java.util.HashMap<>());
            model.addAttribute("solicitacoesUrgentes", new java.util.ArrayList<>());
            model.addAttribute("solicitacoesAtencao", new java.util.ArrayList<>());
            model.addAttribute("ultimasAtividades", new java.util.ArrayList<>());
        }

        model.addAttribute("usuarioLogado", usuarioLogado);

        return "solicitacoes/dashboard";
    }

    /**
     * Listar acessos temporários que estão expirando
     */
    @GetMapping("/expirando")
    public String listarAcessosExpirando(Principal principal, Model model) {
        Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Verificar se o usuário tem permissão
        if (!usuarioLogado.podeGerenciarUsuarios()) {
            return "redirect:/dashboard";
        }

        try {
            // Buscar acessos temporários que expiram nos próximos 7 dias
            LocalDate inicio = LocalDate.now();
            LocalDate fim = inicio.plusDays(7);
            List<SolicitacaoAcesso> acessosExpirando = solicitacaoAcessoRepository
                    .findAcessosTemporariosExpirando(inicio, fim);
            model.addAttribute("acessosExpirando", acessosExpirando);

            // Buscar solicitações que precisam de renovação
            List<SolicitacaoAcesso> paraRenovacao = solicitacaoAcessoService.buscarParaRenovacao();
            model.addAttribute("paraRenovacao", paraRenovacao);

        } catch (Exception e) {
            System.err.println("Erro ao buscar acessos expirando: " + e.getMessage());
            model.addAttribute("erro", "Erro ao carregar dados");
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        return "solicitacoes/expirando";
    }

    /**
     * Endpoint REST para obter dados dos últimos 6 meses para o gráfico
     */
    @GetMapping("/api/dados-ultimos-meses")
    @ResponseBody
    public Map<String, Object> obterDadosUltimosMeses() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> labels = solicitacaoAcessoService.obterNomesUltimosMeses(6);
            List<Long> dados = solicitacaoAcessoService.obterDadosUltimosMeses(6);
            
            response.put("labels", labels);
            response.put("dados", dados);
            response.put("success", true);
        } catch (Exception e) {
            System.err.println("Erro ao obter dados dos últimos meses: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            // Dados padrão em caso de erro
            response.put("labels", List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun"));
            response.put("dados", List.of(0, 0, 0, 0, 0, 0));
        }
        
        return response;
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
        Page<SolicitacaoSimpleDTO> solicitacoes;

        try {
            // Usar paginação padrão do JPA em vez de consulta nativa
            Page<SolicitacaoAcesso> solicitacoesPage;

            if (status != null && !status.isEmpty()) {
                StatusSolicitacao statusEnum = StatusSolicitacao.valueOf(status);
                if (busca != null && !busca.isEmpty()) {
                    // Buscar por texto primeiro e depois filtrar por status
                    Page<SolicitacaoAcesso> resultadoBusca = solicitacaoAcessoService.buscarPorTexto(busca, pageable);
                    List<SolicitacaoAcesso> filtrados = resultadoBusca.getContent().stream()
                            .filter(s -> s.getStatus() == statusEnum)
                            .collect(Collectors.toList());
                    solicitacoesPage = new PageImpl<>(filtrados, pageable, filtrados.size());
                } else {
                    solicitacoesPage = solicitacaoAcessoService.listarPorStatus(statusEnum, pageable);
                }
            } else if (busca != null && !busca.isEmpty()) {
                solicitacoesPage = solicitacaoAcessoService.buscarPorTexto(busca, pageable);
            } else {
                solicitacoesPage = solicitacaoAcessoService.listarTodas(pageable);
            }

            // Converter para DTOs se necessário
            List<SolicitacaoSimpleDTO> solicitacoesDTO = solicitacoesPage.getContent().stream()
                    .map(SolicitacaoSimpleDTO::new)
                    .collect(Collectors.toList());

            solicitacoes = new PageImpl<>(solicitacoesDTO, pageable, solicitacoesPage.getTotalElements());

        } catch (Exception e) {
            System.err.println("Erro ao buscar solicitações: " + e.getMessage());
            e.printStackTrace();
            solicitacoes = new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
        }

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", solicitacoes.getTotalPages());
        model.addAttribute("statusOptions", StatusSolicitacao.values());
        model.addAttribute("usuarioLogado", usuarioLogado);

        // Obter estatísticas com tratamento de erro
        Map<String, Object> estatisticas;
        try {
            estatisticas = solicitacaoAcessoService.obterEstatisticas();
        } catch (Exception e) {
            System.err.println("Erro ao obter estatísticas: " + e.getMessage());
            // Usar estatísticas padrão em caso de erro
            estatisticas = new java.util.HashMap<>();
            estatisticas.put("solicitacoesMes", 0L);
            estatisticas.put("totalPendentes", 0L);
            estatisticas.put("totalAprovadas", 0L);
            estatisticas.put("totalRejeitadas", 0L);
        }
        model.addAttribute("estatisticas", estatisticas);

        return "solicitacoes/todas";
    }
}
