package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rh/recrutamento")
public class RecrutamentoController {

    @GetMapping("/triagem")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String triagem(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Triagem");
        return "rh/recrutamento/triagem";
    }

    @GetMapping("/entrevistas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String entrevistas(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Entrevistas");
        return "rh/recrutamento/entrevistas";
    }

    @GetMapping("/historico")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String historico(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Histórico");
        return "rh/recrutamento/historico";
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String pipeline(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Pipeline");
        return "rh/recrutamento/pipeline";
    }

    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String relatorios(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Relatórios");
        return "rh/recrutamento/relatorios";
    }

    @GetMapping("/candidatos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String candidatos(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Candidatos");
        return "rh/recrutamento/candidatos";
    }

    @GetMapping("/vagas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public String vagas(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Recrutamento - Vagas");
        return "rh/recrutamento/vagas";
    }
}

