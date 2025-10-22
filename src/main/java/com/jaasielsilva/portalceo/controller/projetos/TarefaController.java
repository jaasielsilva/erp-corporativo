package com.jaasielsilva.portalceo.controller.projetos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projetos/tarefas")
public class TarefaController {

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Projetos - Tarefas");
        return "projetos/tarefas/listar";
    }
}
