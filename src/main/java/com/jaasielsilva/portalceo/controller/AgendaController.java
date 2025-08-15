package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    // Página principal da agenda
    @GetMapping
    public String agenda(Model model) {
        // TODO: Implementar lógica da agenda
        // Calendário de compromissos, reuniões
        // Agendamentos, lembretes, notificações
        // Integração com outros módulos (RH, vendas)
        return "agenda/index";
    }
}