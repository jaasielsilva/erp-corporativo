package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.service.AuditoriaEstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estoque/auditoria")
public class AuditoriaEstoqueController {

    @Autowired
    private AuditoriaEstoqueService auditoriaService;

    // Exibe o log de movimentações e alterações de estoque
    @GetMapping
    public String listarAuditoria(Model model) {
        model.addAttribute("registros", auditoriaService.listarTudo());
        return "estoque/auditoria";
    }
}
