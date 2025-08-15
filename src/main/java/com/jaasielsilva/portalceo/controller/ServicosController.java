package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/servicos")
public class ServicosController {

    // Página principal de serviços
    @GetMapping
    public String servicos(Model model) {
        // TODO: Implementar lógica de serviços
        // Catálogo de serviços disponíveis
        // Solicitações de serviços, status, histórico
        // Avaliações, SLA, custos
        return "servicos/index";
    }
}