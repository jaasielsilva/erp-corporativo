package com.jaasielsilva.portalceo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;

@Controller
public class DashboardController {

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
    Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
    boolean isAdmin = usuarioLogado != null && usuarioLogado.getPerfis().stream()
                            .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

    model.addAttribute("usuarioLogado", usuarioLogado);
    model.addAttribute("isAdmin", isAdmin);

    return "dashboard/index";
}

}