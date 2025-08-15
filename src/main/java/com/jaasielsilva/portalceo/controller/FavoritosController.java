package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/favoritos")
public class FavoritosController {

    // Favoritos
    @GetMapping
    public String favoritos(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Favoritos");
        model.addAttribute("pageSubtitle", "Acesso rápido aos seus itens favoritos");
        model.addAttribute("moduleIcon", "fas fa-star");
        model.addAttribute("moduleCSS", "favoritos");
        
        // Funcionalidades planejadas
        java.util.List<String> todoItems = java.util.Arrays.asList(
            "Páginas e módulos favoritos do usuário",
            "Relatórios e documentos marcados como favoritos",
            "Organização por categorias personalizadas",
            "Acesso rápido com atalhos de teclado",
            "Compartilhamento de favoritos entre usuários",
            "Sincronização entre dispositivos",
            "Histórico de itens mais acessados",
            "Sugestões baseadas no uso frequente"
        );
        model.addAttribute("todoItems", todoItems);
        
        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();
        java.util.Map<String, String> organizar = new java.util.HashMap<>();
        organizar.put("type", "link");
        organizar.put("url", "/favoritos/organizar");
        organizar.put("label", "Organizar");
        organizar.put("icon", "fas fa-sort");
        pageActions.add(organizar);
        
        java.util.Map<String, String> categorias = new java.util.HashMap<>();
        categorias.put("type", "link");
        categorias.put("url", "/favoritos/categorias");
        categorias.put("label", "Categorias");
        categorias.put("icon", "fas fa-tags");
        pageActions.add(categorias);
        
        model.addAttribute("pageActions", pageActions);
        
        return "favoritos/index";
    }
}