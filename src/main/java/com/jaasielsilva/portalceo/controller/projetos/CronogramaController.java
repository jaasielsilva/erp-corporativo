package com.jaasielsilva.portalceo.controller.projetos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projetos/cronograma")
public class CronogramaController {

    @GetMapping("/visualizar")
    public String visualizar(Model model) {
        model.addAttribute("pageTitle", "Cronograma de Projetos");
        return "projetos/cronograma/visualizar";
    }
}
