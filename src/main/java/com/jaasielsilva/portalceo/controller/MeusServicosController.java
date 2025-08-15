package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/meus-servicos")
public class MeusServicosController {

    // Meus Serviços
    @GetMapping
    public String meusServicos(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Meus Serviços");
        model.addAttribute("pageSubtitle", "Gerencie seus serviços contratados");
        model.addAttribute("moduleIcon", "fas fa-cogs");
        model.addAttribute("moduleCSS", "meus-servicos");
        
        // Funcionalidades planejadas
        java.util.List<String> todoItems = java.util.Arrays.asList(
            "Lista de serviços contratados pelo usuário",
            "Status detalhado de cada serviço",
            "Controle de renovações automáticas",
            "Opções de cancelamento de serviços",
            "Histórico completo de contratações",
            "Faturas e comprovantes de pagamento",
            "Notificações de vencimento",
            "Suporte técnico integrado"
        );
        model.addAttribute("todoItems", todoItems);
        
        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();
        java.util.Map<String, String> contratar = new java.util.HashMap<>();
        contratar.put("type", "link");
        contratar.put("url", "/meus-servicos/contratar");
        contratar.put("label", "Contratar");
        contratar.put("icon", "fas fa-plus");
        pageActions.add(contratar);
        
        java.util.Map<String, String> faturas = new java.util.HashMap<>();
        faturas.put("type", "link");
        faturas.put("url", "/meus-servicos/faturas");
        faturas.put("label", "Faturas");
        faturas.put("icon", "fas fa-file-invoice");
        pageActions.add(faturas);
        
        model.addAttribute("pageActions", pageActions);
        
        return "meus-servicos/index";
    }
}