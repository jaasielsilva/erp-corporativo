package com.jaasielsilva.portalceo.controller.projetos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projetos/equipes")
public class EquipeController {

    @GetMapping("/membros")
    public String membros(Model model) {
        model.addAttribute("pageTitle", "Projetos - Equipes e Membros");
        return "projetos/equipes/membros";
    }
}
