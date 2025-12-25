package com.jaasielsilva.portalceo.controller.whatchat;

import com.jaasielsilva.portalceo.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("whaTchatChatController")
@RequestMapping("/whatchat")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL','ROLE_JURIDICO')")
public class ChatController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String index(Model model, Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
        if (u != null) {
            model.addAttribute("usuarioId", u.getId());
            model.addAttribute("usuarioNome", u.getNome());
        }
        model.addAttribute("pageTitle", "WhaTchat - WhatsApp");
        return "whatchat/index";
    }
}
