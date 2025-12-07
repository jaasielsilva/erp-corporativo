package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/rh/avaliacao")
public class AvaliacaoController {
    @GetMapping("/periodicidade")
    @PreAuthorize("hasAnyRole('ROLE_GERENCIAL','ROLE_ADMIN','ROLE_MASTER')")
    public String periodicidade(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Avaliação - Periodicidade");
        return "rh/avaliacao/periodicidade";
    }

    @GetMapping("/feedbacks")
    @PreAuthorize("hasAnyRole('ROLE_GERENCIAL','ROLE_ADMIN','ROLE_MASTER')")
    public String feedbacks(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Avaliação - Feedbacks");
        return "rh/avaliacao/feedbacks";
    }

    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String relatorios(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Avaliação - Relatórios");
        return "rh/avaliacao/relatorios";
    }
}
