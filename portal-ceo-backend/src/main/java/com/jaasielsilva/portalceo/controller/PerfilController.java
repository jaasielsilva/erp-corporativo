package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Permissao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.PerfilService;
import com.jaasielsilva.portalceo.service.PermissaoService;
import com.jaasielsilva.portalceo.service.PermissaoUsuarioService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/perfis")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private PermissaoService permissaoService;

    @Autowired
    private PermissaoUsuarioService permissaoUsuarioService;

    // ===============================
    // PÁGINAS PRINCIPAIS
    // ===============================

    /**
     * Lista todos os perfis
     */
    @GetMapping
    public String listar(Model model, Principal principal) {
        // Verifica permissão
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        List<Perfil> perfis = perfilService.listarOrdenadosPorNome();
        Map<String, Object> estatisticas = perfilService.gerarRelatorioEstatisticas();
        
        model.addAttribute("perfis", perfis);
        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("paginaAtual", "perfis");
        
        return "perfis/listar";
    }

    /**
     * Exibe formulário para criar novo perfil
     */
    @GetMapping("/novo")
    public String novo(Model model, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        model.addAttribute("perfil", new Perfil());
        model.addAttribute("permissoes", permissaoService.listarOrdenadasPorNome());
        model.addAttribute("permissoesPorCategoria", permissaoService.listarPorCategoria());
        model.addAttribute("acao", "criar");
        
        return "perfis/formulario";
    }

    /**
     * Exibe formulário para editar perfil
     */
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        Optional<Perfil> perfilOpt = perfilService.buscarPorId(id);
        if (perfilOpt.isEmpty()) {
            return "redirect:/perfis?erro=perfil-nao-encontrado";
        }

        Perfil perfil = perfilOpt.get();
        if (!perfilService.podeSerModificado(id)) {
            return "redirect:/perfis?erro=perfil-sistema";
        }

        model.addAttribute("perfil", perfil);
        model.addAttribute("permissoes", permissaoService.listarOrdenadasPorNome());
        model.addAttribute("permissoesPorCategoria", permissaoService.listarPorCategoria());
        model.addAttribute("permissoesAtivas", perfil.getPermissoes());
        model.addAttribute("acao", "editar");
        
        return "perfis/formulario";
    }

    /**
     * Exibe detalhes de um perfil
     */
    @GetMapping("/detalhes/{id}")
    public String detalhes(@PathVariable Long id, Model model, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        Optional<Perfil> perfilOpt = perfilService.buscarPorId(id);
        if (perfilOpt.isEmpty()) {
            return "redirect:/perfis?erro=perfil-nao-encontrado";
        }

        Perfil perfil = perfilOpt.get();
        List<Usuario> usuarios = perfilService.listarUsuariosDoPerfil(id);
        
        model.addAttribute("perfil", perfil);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("podeModificar", perfilService.podeSerModificado(id));
        
        return "perfis/detalhes";
    }

    // ===============================
    // AÇÕES DE CRUD
    // ===============================

    /**
     * Salva um novo perfil
     */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Perfil perfil, 
                        @RequestParam(value = "permissoes", required = false) List<Long> permissaoIds,
                        RedirectAttributes redirectAttributes,
                        Principal principal) {
        
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            // Salva o perfil
            Perfil perfilSalvo = perfilService.salvar(perfil);
            
            // Adiciona permissões se foram selecionadas
            if (permissaoIds != null && !permissaoIds.isEmpty()) {
                perfilService.definirPermissoes(perfilSalvo.getId(), new HashSet<>(permissaoIds));
            }
            
            redirectAttributes.addFlashAttribute("sucesso", "Perfil criado com sucesso!");
            return "redirect:/perfis";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar perfil: " + e.getMessage());
            return "redirect:/perfis/novo";
        }
    }

    /**
     * Atualiza um perfil existente
     */
    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                           @ModelAttribute Perfil perfil,
                           @RequestParam(value = "permissoes", required = false) List<Long> permissaoIds,
                           RedirectAttributes redirectAttributes,
                           Principal principal) {
        
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            if (!perfilService.podeSerModificado(id)) {
                redirectAttributes.addFlashAttribute("erro", "Este perfil não pode ser modificado.");
                return "redirect:/perfis";
            }
            
            // Atualiza o perfil
            perfilService.atualizar(id, perfil);
            
            // Atualiza permissões
            Set<Long> novasPermissoes = permissaoIds != null ? new HashSet<>(permissaoIds) : new HashSet<>();
            perfilService.definirPermissoes(id, novasPermissoes);
            
            redirectAttributes.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
            return "redirect:/perfis";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar perfil: " + e.getMessage());
            return "redirect:/perfis/editar/" + id;
        }
    }

    /**
     * Exclui um perfil
     */
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, 
                         RedirectAttributes redirectAttributes,
                         Principal principal) {
        
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        try {
            perfilService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Perfil excluído com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir perfil: " + e.getMessage());
        }
        
        return "redirect:/perfis";
    }

    // ===============================
    // ENDPOINTS AJAX
    // ===============================

    /**
     * Busca perfis via AJAX
     */
    @GetMapping("/buscar")
    @ResponseBody
    public ResponseEntity<List<Perfil>> buscar(@RequestParam String termo, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Perfil> perfis = perfilService.buscarPorNomeParcial(termo);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Retorna permissões de um perfil via AJAX
     */
    @GetMapping("/{id}/permissoes")
    @ResponseBody
    public ResponseEntity<Set<Permissao>> obterPermissoes(@PathVariable Long id, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Set<Permissao> permissoes = perfilService.listarPermissoesDoPerfil(id);
            return ResponseEntity.ok(permissoes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retorna usuários de um perfil via AJAX
     */
    @GetMapping("/{id}/usuarios")
    @ResponseBody
    public ResponseEntity<List<Usuario>> obterUsuarios(@PathVariable Long id, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Usuario> usuarios = perfilService.listarUsuariosDoPerfil(id);
            return ResponseEntity.ok(usuarios);
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
        if (!temPermissaoGerenciarPerfis(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> estatisticas = perfilService.gerarRelatorioEstatisticas();
        return ResponseEntity.ok(estatisticas);
    }

    // ===============================
    // RELATÓRIOS E EXPORTAÇÃO
    // ===============================

    /**
     * Página de relatórios de perfis
     */
    @GetMapping("/relatorios")
    public String relatorios(Model model, Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return "redirect:/dashboard?erro=sem-permissao";
        }

        Map<String, Object> estatisticas = perfilService.gerarRelatorioEstatisticas();
        Map<String, List<Permissao>> permissoesPorCategoria = permissaoService.listarPorCategoria();
        
        model.addAttribute("estatisticas", estatisticas);
        model.addAttribute("permissoesPorCategoria", permissoesPorCategoria);
        
        return "perfis/relatorios";
    }

    /**
     * Exporta relatório de perfis em JSON
     */
    @GetMapping("/exportar/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportarJson(Principal principal) {
        if (!temPermissaoGerenciarPerfis(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("perfis", perfilService.listarTodos());
        relatorio.put("estatisticas", perfilService.gerarRelatorioEstatisticas());
        relatorio.put("dataExportacao", new Date());
        
        return ResponseEntity.ok(relatorio);
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    /**
     * Verifica se o usuário tem permissão para gerenciar perfis
     */
    private boolean temPermissaoGerenciarPerfis(Principal principal) {
        if (principal == null) {
            return false;
        }
        
        // Aqui você pode implementar a lógica específica de verificação
        // Por exemplo, verificar se o usuário tem nível ADMIN ou MASTER
        Authentication auth = (Authentication) principal;
        return auth.getAuthorities().stream()
                .anyMatch(authority -> 
                    authority.getAuthority().equals("ROLE_ADMIN") ||
                    authority.getAuthority().equals("ROLE_MASTER") ||
                    authority.getAuthority().equals("ROLE_CONFIG_WRITE")
                );
    }

    /**
     * Método para tratamento de erros globais
     */
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Erro interno: " + e.getMessage());
        return "redirect:/perfis";
    }
}