package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class CategoriaPageController {

    @Autowired
    private CategoriaService categoriaService;

    // ✅ Página de listagem
    @GetMapping
    @PreAuthorize("hasAuthority('MENU_ESTOQUE_CATEGORIAS_LISTAR')")
    public String listarCategorias(Model model) {
        model.addAttribute("titulo", "Categorias");
        model.addAttribute("categorias", categoriaService.findAll());

        // Verificações de permissão para o frontend
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean podeEditar = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("MENU_ESTOQUE_CATEGORIAS_NOVO"));
        boolean podeExcluir = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("MENU_ESTOQUE_CATEGORIAS_NOVO"));
        
        model.addAttribute("podeEditar", podeEditar);
        model.addAttribute("podeExcluir", podeExcluir);

        return "categoria/lista";
    }

    // ✅ Página de formulário (nova categoria)
    @GetMapping("/nova")
    @PreAuthorize("hasAuthority('MENU_ESTOQUE_CATEGORIAS_NOVO')")
    public String novaCategoria(Model model) {
        model.addAttribute("titulo", "Nova Categoria");
        model.addAttribute("categoria", new Categoria());
        return "categoria/form";
    }

    // ✅ Editar categoria existente
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAuthority('MENU_ESTOQUE_CATEGORIAS_NOVO')")
    public String editarCategoria(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        if (categoria == null) {
            return "redirect:/categorias";
        }
        model.addAttribute("titulo", "Editar Categoria");
        model.addAttribute("categoria", categoria);
        return "categoria/form";
    }

    // ✅ Salvar (novo ou edição)
    @PostMapping("/salvar")
    @PreAuthorize("hasAuthority('MENU_ESTOQUE_CATEGORIAS_NOVO')")
    public String salvarCategoria(@ModelAttribute Categoria categoria) {
        categoriaService.salvar(categoria);
        return "redirect:/categorias";
    }

    // ✅ Excluir categoria
    @GetMapping("/excluir/{id}")
    @PreAuthorize("hasAuthority('MENU_ESTOQUE_CATEGORIAS_NOVO')")
    public String excluirCategoria(@PathVariable Long id) {
        categoriaService.deletar(id);
        return "redirect:/categorias";
    }
}
