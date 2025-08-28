package com.jaasielsilva.portalceo.controller.rh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller principal do módulo de Recursos Humanos (RH).
 * Responsável por gerenciar as rotas principais e redirecionamentos do módulo RH.
 */
@Controller
@RequestMapping("/rh")
public class RhController {

    private static final Logger logger = LoggerFactory.getLogger(RhController.class);

    /**
     * Página principal do módulo RH
     * Redireciona para a página de adesão de colaboradores
     */
    @GetMapping
    public String moduloRh(Model model) {
        logger.info("Acessando módulo RH - redirecionando para adesão de colaboradores");
        return "redirect:/rh/colaboradores/adesao";
    }

    /**
     * Dashboard do módulo RH
     */
    @GetMapping("/dashboard")
    public String dashboardRh(Model model) {
        logger.info("Acessando dashboard do módulo RH");
        model.addAttribute("modulo", "RH");
        model.addAttribute("titulo", "Dashboard - Recursos Humanos");
        return "rh/dashboard";
    }
}