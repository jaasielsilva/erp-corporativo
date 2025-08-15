package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/metas")
public class MetasController {

    // Página principal de metas
    @GetMapping
    public String metas(Model model) {
        // TODO: Implementar lógica de metas
        // Definição de metas por departamento, colaborador
        // Acompanhamento de performance, indicadores
        // Gráficos de evolução, rankings
        return "metas/index";
    }
}