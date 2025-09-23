package com.jaasielsilva.portalceo.controller.ajuda;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ajuda")
public class AjudaController {

    // Central de Ajuda
    @GetMapping
    public String ajuda(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Central de Ajuda");
        model.addAttribute("pageSubtitle", "Base de conhecimento e suporte ao usuário");
        model.addAttribute("moduleIcon", "fas fa-question-circle");
        model.addAttribute("moduleCSS", "ajuda");
        
        return "ajuda/index";
    }
}