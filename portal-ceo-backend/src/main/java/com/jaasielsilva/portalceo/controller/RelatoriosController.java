package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/relatorios")
public class RelatoriosController {

    // Página principal de relatórios
    @GetMapping
    public String relatorios(Model model) {
        // TODO: Implementar lógica de relatórios
        // Dashboard de relatórios executivos
        // Relatórios financeiros, operacionais, estratégicos
        // Exportação em PDF, Excel, gráficos interativos
        return "relatorios/index";
    }
}