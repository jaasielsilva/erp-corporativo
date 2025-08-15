package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/termos")
public class TermosController {

    // Termos de Uso
    @GetMapping
    public String termos(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Termos de Uso");
        model.addAttribute("pageSubtitle", "Termos de uso e política de privacidade");
        model.addAttribute("moduleIcon", "fas fa-file-contract");
        model.addAttribute("moduleCSS", "termos");
        
        // Funcionalidades planejadas
        java.util.List<String> todoItems = java.util.Arrays.asList(
            "Termos de uso completos e atualizados",
            "Política de privacidade detalhada",
            "Controle de versões dos documentos",
            "Histórico de alterações com datas",
            "Sistema de aceite eletrônico",
            "Assinatura digital dos termos",
            "Notificações de atualizações",
            "Relatório de aceites por usuário"
        );
        model.addAttribute("todoItems", todoItems);
        
        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();
        java.util.Map<String, String> privacidade = new java.util.HashMap<>();
        privacidade.put("type", "link");
        privacidade.put("url", "/termos/privacidade");
        privacidade.put("label", "Privacidade");
        privacidade.put("icon", "fas fa-shield-alt");
        pageActions.add(privacidade);
        
        java.util.Map<String, String> historico = new java.util.HashMap<>();
        historico.put("type", "link");
        historico.put("url", "/termos/historico");
        historico.put("label", "Histórico");
        historico.put("icon", "fas fa-history");
        pageActions.add(historico);
        
        model.addAttribute("pageActions", pageActions);
        
        return "termos/index";
    }
}