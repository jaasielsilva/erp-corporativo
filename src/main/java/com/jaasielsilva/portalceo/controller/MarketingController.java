package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/marketing")
public class MarketingController {

    // Página principal do Marketing
    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Marketing");
        model.addAttribute("moduleCSS", "marketing");
        return "marketing/index";
    }

    // Campanhas de Marketing
    @GetMapping("/campanhas")
    public String campanhas(Model model) {
        // TODO: Implementar lógica de campanhas
        // Listar campanhas ativas, pausadas, finalizadas
        // Métricas de performance, ROI, conversões
        return "marketing/campanhas";
    }

    // Leads
    @GetMapping("/leads")
    public String leads(Model model) {
        // TODO: Implementar lógica de leads
        // Listar leads por status (novo, qualificado, convertido)
        // Funil de vendas, origem dos leads
        return "marketing/leads";
    }

    // Eventos
    @GetMapping("/eventos")
    public String eventos(Model model) {
        // TODO: Implementar lógica de eventos
        // Calendário de eventos, webinars, feiras
        // Participantes, custos, resultados
        return "marketing/eventos";
    }

    // Materiais de Marketing
    @GetMapping("/materiais")
    public String materiais(Model model) {
        // TODO: Implementar lógica de materiais
        // Biblioteca de materiais (banners, flyers, vídeos)
        // Controle de versões, aprovações
        return "marketing/materiais";
    }
}