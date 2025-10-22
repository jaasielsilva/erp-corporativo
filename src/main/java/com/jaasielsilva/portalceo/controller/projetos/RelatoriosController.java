package com.jaasielsilva.portalceo.controller.projetos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projetos/relatorios")
public class RelatoriosController {

    @GetMapping("/desempenho")
    public String desempenho(Model model) {
        // lógica para gerar relatório de desempenho
        // TODO: Implementar lógica de relatórios
        // Dashboard de relatórios executivos
        // Relatórios financeiros, operacionais, estratégicos
        // Exportação em PDF, Excel, gráficos interativos
        return "projetos/relatorios/desempenho";
    }
}
