package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estoque/relatorios")
public class RelatoriosEstoqueController {

    // Página inicial de relatórios de estoque
    @GetMapping
    public String mostrarRelatorios(Model model) {
        // Futuro: gráficos, exportação PDF, filtros por período
        return "estoque/relatorios";
    }
}
