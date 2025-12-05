package com.jaasielsilva.portalceo.controller.ajuda;

import com.jaasielsilva.portalceo.service.ajuda.HelpService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AjudaConteudoController {
    private final HelpService helpService;
    public AjudaConteudoController(HelpService helpService) { this.helpService = helpService; }

    @GetMapping("/ajuda/conteudo/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        return helpService.obterConteudo(id)
                .map(c -> { model.addAttribute("conteudo", c); return "ajuda/conteudo"; })
                .orElse("redirect:/ajuda");
    }
}
