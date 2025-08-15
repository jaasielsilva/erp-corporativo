package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/suporte")
public class SuporteController {

    // Suporte
    @GetMapping
    public String suporte(Model model) {
        // TODO: Implementar lógica do suporte
        // Sistema de tickets, chat ao vivo
        // Base de conhecimento, FAQ
        // Escalação, SLA, satisfação
        // Integração com equipe de suporte
        return "suporte/index";
    }
}