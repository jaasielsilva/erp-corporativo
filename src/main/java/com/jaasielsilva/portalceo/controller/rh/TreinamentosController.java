package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/rh/treinamentos")
public class TreinamentosController {
    @GetMapping("/cadastro")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String cadastro(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Cadastro");
        return "rh/treinamentos/cadastro";
    }

    @GetMapping("/inscricao")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String inscricao(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Inscrição");
        return "rh/treinamentos/inscricao";
    }

    @GetMapping("/certificado")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String certificado(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Treinamentos - Certificado");
        return "rh/treinamentos/certificado";
    }
}
