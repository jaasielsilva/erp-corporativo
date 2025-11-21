package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendas")
public class VendasController {

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Vendas - Dashboard");
        model.addAttribute("moduleIcon", "fas fa-shopping-cart");
        model.addAttribute("moduleCSS", "vendas");
        return "vendas/index";
    }

    @GetMapping("/pdv")
    public String pdv(Model model) {
        model.addAttribute("pageTitle", "Vendas - PDV");
        model.addAttribute("moduleIcon", "fas fa-cash-register");
        model.addAttribute("moduleCSS", "vendas");
        return "vendas/pdv";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("pageTitle", "Vendas - Pedidos");
        model.addAttribute("moduleIcon", "fas fa-clipboard-list");
        model.addAttribute("moduleCSS", "vendas");
        return "vendas/pedidos";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("pageTitle", "Vendas - Relatórios");
        model.addAttribute("moduleIcon", "fas fa-chart-bar");
        model.addAttribute("moduleCSS", "vendas");
        return "vendas/relatorios";
    }
}