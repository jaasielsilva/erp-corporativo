package com.jaasielsilva.portalceo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RelatoriosHubController {

    @GetMapping("/relatorios")
    @PreAuthorize("hasAuthority('MENU_ADMIN_RELATORIOS')")
    public String hub(Model model) {
        model.addAttribute("pageTitle", "Relatórios");
        return "admin/relatorios";
    }
}
