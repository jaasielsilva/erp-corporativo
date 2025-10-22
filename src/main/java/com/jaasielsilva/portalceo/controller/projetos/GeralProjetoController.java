package com.jaasielsilva.portalceo.controller.projetos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projetos/geral")
public class GeralProjetoController {

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Projetos - Geral");
        return "projetos/geral/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Projeto");
        return "projetos/geral/novo";
    }
}
