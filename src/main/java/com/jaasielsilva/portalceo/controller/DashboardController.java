package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;  // 1

    @GetMapping("/dashboard")               // 2
    public String dashboard(Model model) {  // 3

        // 4: Pega o objeto autenticado do contexto de segurança do Spring
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email;

        // 5: Se o principal for um UserDetails (padrão do Spring Security), pega o username, que no seu caso é o email
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();  // Se não for UserDetails, usa o toString (caso raro)
        }

        // 6: Busca o usuário no banco usando o email
        Usuario usuario = usuarioService.buscarPorEmail(email);

        // 7: Se usuário não encontrado, redireciona para login
        if (usuario == null) {
            return "redirect:/login";
        }

        // 8: Verifica se o usuário tem o perfil ADMIN
        boolean isAdmin = usuario.getPerfis().stream()
                .anyMatch(perfil -> perfil.getNome().equalsIgnoreCase("ADMIN"));

        // 9: Adiciona dados do usuário e o flag de admin ao modelo para usar na view
        model.addAttribute("usuario", usuario);
        model.addAttribute("isAdmin", isAdmin);

        // 10: Retorna a view do dashboard (Thymeleaf: src/main/resources/templates/dashboard/index.html)
        return "dashboard/index";
    }
}
