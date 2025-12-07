package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/rh/ferias")
public class FeriasController {
    @GetMapping("/solicitar")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String solicitar(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Férias - Solicitar");
        return "rh/ferias/solicitar";
    }

    @GetMapping("/aprovar")
    @PreAuthorize("hasAnyRole('ROLE_GERENCIAL','ROLE_ADMIN','ROLE_MASTER')")
    public String aprovar(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Férias - Aprovar");
        return "rh/ferias/aprovar";
    }

    @GetMapping("/planejamento")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String planejamento(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Férias - Planejamento");
        return "rh/ferias/planejamento";
    }

    @GetMapping("/calendario")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String calendario(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Férias - Calendário");
        return "rh/ferias/calendario";
    }
}

