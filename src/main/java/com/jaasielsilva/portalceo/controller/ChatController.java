package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatController {

    // Página principal do chat
    @GetMapping
    public String chat(Model model) {
        // TODO: Implementar lógica do chat
        // Chat interno entre colaboradores
        // Grupos, canais, mensagens diretas
        // Histórico, arquivos, notificações
        return "chat/index";
    }
}