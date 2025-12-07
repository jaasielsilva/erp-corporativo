package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/rh/recrutamento")
public class RecrutamentoController {
    @GetMapping("/vagas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String vagas(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Vagas");
        return "rh/recrutamento/vagas";
    }

    @GetMapping("/triagem")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String triagem(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Triagem");
        return "rh/recrutamento/triagem";
    }

    @GetMapping("/entrevistas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String entrevistas(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Entrevistas");
        return "rh/recrutamento/entrevistas";
    }

    @GetMapping("/historico")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String historico(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Histórico");
        return "rh/recrutamento/historico";
    }
}
