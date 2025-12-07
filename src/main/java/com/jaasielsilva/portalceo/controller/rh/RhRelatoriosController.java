package com.jaasielsilva.portalceo.controller.rh;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/rh/relatorios")
public class RhRelatoriosController {
    @GetMapping("/turnover")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String turnover(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Turnover");
        return "rh/relatorios/turnover";
    }

    @GetMapping("/absenteismo")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String absenteismo(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Absenteísmo");
        return "rh/relatorios/absenteismo";
    }

    @GetMapping("/headcount")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String headcount(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Headcount");
        return "rh/relatorios/headcount";
    }

    @GetMapping("/indicadores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public String indicadores(Model model) {
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Relatórios RH - Indicadores");
        return "rh/relatorios/indicadores";
    }
}
