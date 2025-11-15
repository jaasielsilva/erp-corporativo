package com.jaasielsilva.portalceo.controller.chat;

import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatPageController {
    private final UsuarioService usuarioService;
    public ChatPageController(UsuarioService usuarioService) { this.usuarioService = usuarioService; }
    @GetMapping
    public String index(org.springframework.ui.Model model, org.springframework.security.core.Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
        if (u != null) { model.addAttribute("usuarioId", u.getId()); model.addAttribute("usuarioNome", u.getNome()); model.addAttribute("pageTitle", "Chat Interno"); }
        return "chat/index";
    }
}