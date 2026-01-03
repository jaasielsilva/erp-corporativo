package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Permissao;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.service.PermissaoService;
import com.jaasielsilva.portalceo.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/permissoes")
public class PermissaoController {

    private final PermissaoService permissaoService;
    private final PerfilService perfilService;

    @Autowired
    public PermissaoController(PermissaoService permissaoService, PerfilService perfilService) {
        this.permissaoService = permissaoService;
        this.perfilService = perfilService;
    }

    // ===============================
    // PÁGINAS PRINCIPAIS
    // ===============================

    /**
     * Lista todas as permissões
     */
    @GetMapping
    public String listar(Model model, Principal principal) {

        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        List<Permissao> permissoes = permissaoService.listarOrdenadasPorNome();
        Map<String, List<Permissao>> permissoesPorCategoria = permissaoService.listarPorCategoria();
        Map<String, Object> estatisticas = permissaoService.gerarRelatorioEstatisticas();

        model.addAttribute("permissoes", permissoes != null ? permissoes : Collections.emptyList());
        model.addAttribute("permissoesPorCategoria",
                permissoesPorCategoria != null ? permissoesPorCategoria : new HashMap<>());
        model.addAttribute("estatisticas", estatisticas != null ? estatisticas : new HashMap<>());
        model.addAttribute("categorias", getCategorias());
        model.addAttribute("paginaAtual", "permissoes");

        return "permissoes/listar";
    }

    /**
     * Exibe formulário para criar nova permissão
     */
    @GetMapping("/nova")
    public String nova(Model model, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        model.addAttribute("permissao", new Permissao());
        model.addAttribute("categorias", getCategorias());
        model.addAttribute("acao", "criar");

        return "permissoes/form";
    }

    /**
     * Exibe formulário para editar permissão
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        Optional<Permissao> permissaoOpt = permissaoService.buscarPorId(id);
        if (permissaoOpt.isEmpty()) {
            return "redirect:/permissoes?erro=permissao-nao-encontrada";
        }

        Permissao permissao = permissaoOpt.get();
        if (!permissaoService.podeSerModificada(id)) {
            return "redirect:/permissoes?erro=permissao-sistema";
        }

        model.addAttribute("permissao", permissao);
        model.addAttribute("categorias", getCategorias());
        model.addAttribute("acao", "editar");

        return "permissoes/form";
    }

    /**
     * Exibe detalhes de uma permissão
     */
    @GetMapping("/detalhes/{id}")
    public String detalhes(@PathVariable Long id, Model model, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        Optional<Permissao> permissaoOpt = permissaoService.buscarPorId(id);
        if (permissaoOpt.isEmpty()) {
            return "redirect:/permissoes?erro=permissao-nao-encontrada";
        }

        Permissao permissao = permissaoOpt.get();
        List<Perfil> perfis = permissaoService.listarPerfisComPermissao(id);

        model.addAttribute("permissao", permissao);
        model.addAttribute("perfis", perfis);
        model.addAttribute("totalPerfis", perfis.size());
        model.addAttribute("podeModificar", permissaoService.podeSerModificada(id));

        return "permissoes/detalhes";
    }

    // ===============================
    // AÇÕES DE CRUD
    // ===============================

    /**
     * Salva uma nova permissão
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Permissao permissao,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            permissaoService.salvar(permissao);
            redirectAttributes.addFlashAttribute("sucesso", "Permissão criada com sucesso!");
            return "redirect:/permissoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar permissão: " + e.getMessage());
            return "redirect:/permissoes/nova";
        }
    }

    /**
     * Atualiza uma permissão existente
     */
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
            @ModelAttribute Permissao permissao,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            if (!permissaoService.podeSerModificada(id)) {
                redirectAttributes.addFlashAttribute("erro", "Esta permissão não pode ser modificada.");
                return "redirect:/permissoes";
            }

            permissaoService.atualizar(id, permissao);
            redirectAttributes.addFlashAttribute("sucesso", "Permissão atualizada com sucesso!");
            return "redirect:/permissoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar permissão: " + e.getMessage());
            return "redirect:/permissoes/editar/" + id;
        }
    }

    /**
     * Exclui uma permissão
     */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            permissaoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Permissão excluída com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir permissão: " + e.getMessage());
        }

        return "redirect:/permissoes";
    }

    // ===============================
    // ENDPOINTS AJAX
    // ===============================

    /**
     * Busca permissões via AJAX
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<List<Permissao>> buscar(@RequestParam String termo, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Permissao> permissoes = permissaoService.buscarPorNomeParcial(termo);
        return ResponseEntity.ok(permissoes);
    }

    /**
     * Retorna perfis que possuem uma permissão via AJAX
     */
    @GetMapping("/{id}/perfis")
    @ResponseBody
    public ResponseEntity<List<Perfil>> obterPerfis(@PathVariable Long id, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Perfil> perfis = permissaoService.listarPerfisComPermissao(id);
            return ResponseEntity.ok(perfis);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retorna estatísticas via AJAX
     */
    @GetMapping("/estatisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obterEstatisticas(Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> estatisticas = permissaoService.gerarRelatorioEstatisticas();
        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Retorna permissões por categoria via AJAX
     */
    @GetMapping("/categorias")
    @ResponseBody
    public ResponseEntity<Map<String, List<Permissao>>> obterPorCategoria(Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, List<Permissao>> permissoesPorCategoria = permissaoService.listarPorCategoria();
        return ResponseEntity.ok(permissoesPorCategoria);
    }

    // ===============================
    // FUNCIONALIDADES ESPECIAIS
    // ===============================

    /**
     * Página para criação em lote de permissões
     */
    @GetMapping("/criar-lote")
    public String criarLote(Model model, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        model.addAttribute("categorias", getCategorias());
        return "permissoes/criar-lote";
    }

    /**
     * Processa criação em lote de permissões
     */
    @PostMapping("/criar-lote")
    public String processarCriacaoLote(@RequestParam("nomes") String nomesTexto,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            // Processa os nomes (um por linha)
            List<String> nomes = Arrays.asList(nomesTexto.split("\\r?\\n"));
            nomes = nomes.stream()
                    .map(String::trim)
                    .filter(nome -> !nome.isEmpty())
                    .toList();

            if (nomes.isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "Nenhuma permissão válida foi informada.");
                return "redirect:/permissoes/criar-lote";
            }

            List<Permissao> permissoesCriadas = permissaoService.criarPermissoes(nomes);

            redirectAttributes.addFlashAttribute("sucesso",
                    String.format("%d permissão(ões) criada(s) com sucesso!", permissoesCriadas.size()));

            return "redirect:/permissoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar permissões: " + e.getMessage());
            return "redirect:/permissoes/criar-lote";
        }
    }

    /**
     * Inicializa permissões padrão do sistema
     */
    @PostMapping("/inicializar-padrao")
    public String inicializarPermissoesPadrao(RedirectAttributes redirectAttributes, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            permissaoService.criarPermissoesPadrao();
            redirectAttributes.addFlashAttribute("sucesso", "Permissões padrão inicializadas com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao inicializar permissões: " + e.getMessage());
        }

        return "redirect:/permissoes";
    }

    // ===============================
    // RELATÓRIOS E EXPORTAÇÃO
    // ===============================

    /**
     * Página de relatórios de permissões
     */
    @GetMapping("/relatorios")
    public String relatorios(Model model, Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        Map<String, Object> estatisticas = permissaoService.gerarRelatorioEstatisticas();
        Map<String, List<Permissao>> permissoesPorCategoria = permissaoService.listarPorCategoria();

        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("permissoesPorCategoria", permissoesPorCategoria);

        return "permissoes/relatorios";
    }

    /**
     * Exporta relatório de permissões em JSON
     */
    @GetMapping("/exportar/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportarJson(Principal principal) {
        if (!temPermissaoGerenciarPermissoes(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("permissoes", permissaoService.listarTodas());
        relatorio.put("permissoesPorCategoria", permissaoService.listarPorCategoria());
        relatorio.put("estatisticas", permissaoService.gerarRelatorioEstatisticas());
        relatorio.put("dataExportacao", new Date());

        return ResponseEntity.ok(relatorio);
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    /**
     * Verifica se o usuário tem permissão para gerenciar permissões
     */
    private boolean temPermissaoGerenciarPermissoes(Principal principal) {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("MENU_ADMIN_GESTAO_ACESSO_PERMISSOES"));
    }

    /**
     * Retorna as categorias disponíveis para permissões
     */
    private List<String> getCategorias() {
        return Arrays.asList(
                "Usuários",
                "Recursos Humanos",
                "Financeiro",
                "Relatórios",
                "Configurações",
                "Sistema",
                "Geral");
    }

    /**
     * Método para tratamento de erros globais
     */
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Erro interno: " + e.getMessage());
        return "redirect:/permissoes";
    }
}
