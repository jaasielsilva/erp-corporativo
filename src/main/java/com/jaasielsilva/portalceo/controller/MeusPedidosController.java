package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/meus-pedidos")
public class MeusPedidosController {

    // Meus Pedidos
    @GetMapping
    public String meusPedidos(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Meus Pedidos");
        model.addAttribute("pageSubtitle", "Acompanhe seus pedidos e solicitações");
        model.addAttribute("moduleIcon", "fas fa-shopping-cart");
        model.addAttribute("moduleCSS", "meus-pedidos");
        
        // Funcionalidades planejadas
        java.util.List<String> todoItems = java.util.Arrays.asList(
            "Lista de pedidos do usuário logado",
            "Status detalhado de cada pedido",
            "Histórico completo de movimentações",
            "Sistema de rastreamento em tempo real",
            "Opções de cancelamento de pedidos",
            "Solicitações de devolução e troca",
            "Notificações de mudança de status",
            "Relatórios personalizados de pedidos"
        );
        model.addAttribute("todoItems", todoItems);
        
        // Ações da página
        java.util.List<java.util.Map<String, String>> pageActions = new java.util.ArrayList<>();
        java.util.Map<String, String> novoPedido = new java.util.HashMap<>();
        novoPedido.put("type", "link");
        novoPedido.put("url", "/meus-pedidos/novo");
        novoPedido.put("label", "Novo Pedido");
        novoPedido.put("icon", "fas fa-plus");
        pageActions.add(novoPedido);
        
        java.util.Map<String, String> historico = new java.util.HashMap<>();
        historico.put("type", "link");
        historico.put("url", "/meus-pedidos/historico");
        historico.put("label", "Histórico");
        historico.put("icon", "fas fa-history");
        pageActions.add(historico);
        
        model.addAttribute("pageActions", pageActions);
        
        return "meus-pedidos/index";
    }
}