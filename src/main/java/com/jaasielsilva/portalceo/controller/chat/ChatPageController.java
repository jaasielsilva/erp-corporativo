package com.jaasielsilva.portalceo.controller.chat;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatPageController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String index(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            String email = auth.getName();
            Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            model.addAttribute("usuarioId", usuario.getId());
            model.addAttribute("usuarioNome", usuario.getNome());
            
            return "chat/index";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard?error=ChatError";
        }
    }
}