package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String listarCategorias(Model model) {
        model.addAttribute("titulo", "Categorias");
        model.addAttribute("categorias", categoriaService.findAll());
        return "categoria/lista";
    }

    // ✅ Página de formulário (nova categoria)
    @GetMapping("/nova")
    public String novaCategoria(Model model) {
        model.addAttribute("titulo", "Nova Categoria");
        model.addAttribute("categoria", new Categoria());
        return "categoria/form";
    }

    // ✅ Editar categoria existente
    @GetMapping("/editar/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        if (categoria == null) {
            return "redirect:/categoria";
        }
        model.addAttribute("titulo", "Editar Categoria");
        model.addAttribute("categoria", categoria);
        return "categoria/form";
    }

    // ✅ Salvar (novo ou edição)
    @PostMapping("/salvar")
    public String salvarCategoria(@ModelAttribute Categoria categoria) {
        categoriaService.salvar(categoria);
        return "redirect:/categoria";
    }

    // ✅ Excluir categoria
    @GetMapping("/excluir/{id}")
    public String excluirCategoria(@PathVariable Long id) {
        categoriaService.deletar(id);
        return "redirect:/categoria";
    }
}
