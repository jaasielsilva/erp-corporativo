package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recomendados")
public class RecomendacoesController {

    // Recomendados
    @GetMapping
    public String recomendados(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Recomendados");
        model.addAttribute("pageSubtitle", "Produtos e serviços personalizados para você");
        model.addAttribute("moduleIcon", "fas fa-thumbs-up");
        model.addAttribute("moduleCSS", "recomendados");
        
        // Funcionalidades planejadas
        java.util.List<String> todoItems = java.util.Arrays.asList(
            "Produtos recomendados baseados no histórico",
            "Serviços personalizados por preferências",
            "Algoritmos de machine learning",
            "Análise comportamental do usuário",
            "Recomendações por categoria",
            "Sistema de avaliação e feedback",
            "Tendências do mercado",
            "Ofertas exclusivas personalizadas"
        );
        model.addAttribute("todoItems", todoItems);
        
        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();
        java.util.Map<String, String> preferencias = new java.util.HashMap<>();
        preferencias.put("type", "link");
        preferencias.put("url", "/recomendados/preferencias");
        preferencias.put("label", "Preferências");
        preferencias.put("icon", "fas fa-cog");
        pageActions.add(preferencias);
        
        java.util.Map<String, String> categorias = new java.util.HashMap<>();
        categorias.put("type", "link");
        categorias.put("url", "/recomendados/categorias");
        categorias.put("label", "Categorias");
        categorias.put("icon", "fas fa-tags");
        pageActions.add(categorias);
        
        model.addAttribute("pageActions", pageActions);
        
        return "recomendados/index";
    }
}