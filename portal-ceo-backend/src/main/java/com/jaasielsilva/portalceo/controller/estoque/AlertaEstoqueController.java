package com.jaasielsilva.portalceo.controller.estoque;

import com.jaasielsilva.portalceo.service.AlertaEstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estoque/alertas")
public class AlertaEstoqueController {

    @Autowired
    private AlertaEstoqueService alertaService;

    // Página que exibe alertas de produtos abaixo do mínimo
    @GetMapping
    public String mostrarAlertas(Model model) {
        alertaService.verificarAlertas(); // Atualiza os alertas primeiro
        model.addAttribute("alertas", alertaService.listarPendentes());
        return "estoque/alertas";
    }
}
