package com.jaasielsilva.portalceo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/chat")
public class ChatController {
    
    // Mostrar chat
    @GetMapping
    public String ajuda(Model model) {
        // Configurações da página
        model.addAttribute("pageTitle", "Chat");
        model.addAttribute("pageSubtitle", "Comunicação em tempo real");
        model.addAttribute("moduleIcon", "fas fa-comments");
        model.addAttribute("moduleCSS", "chat");
        
        return "chat/index";
    }
}
