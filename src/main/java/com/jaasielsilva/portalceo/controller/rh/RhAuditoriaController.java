package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/rh/auditoria")
public class RhAuditoriaController {

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String index(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Início");
        return "rh/auditoria/index";
    }

    @GetMapping("/acessos")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String acessos(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Log de Acessos");
        model.addAttribute("categoria", "ACESSO");
        return "rh/auditoria/acessos";
    }

    @GetMapping("/alteracoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String alteracoes(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Alterações de Dados");
        model.addAttribute("categoria", "ALTERACAO");
        return "rh/auditoria/alteracoes";
    }

    @GetMapping("/exportacoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String exportacoes(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Exportações");
        model.addAttribute("categoria", "EXPORTACAO");
        return "rh/auditoria/exportacoes";
    }

    @GetMapping("/revisoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH_GERENTE')")
    public String revisoes(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Auditoria RH - Revisões Periódicas");
        model.addAttribute("categoria", "REVISAO");
        return "rh/auditoria/revisoes";
    }
}
