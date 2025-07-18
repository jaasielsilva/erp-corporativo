package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.findAll());
        return "categoria/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categoria/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Categoria categoria) {
        categoriaService.salvar(categoria);
        return "redirect:/categorias";
    }
}
