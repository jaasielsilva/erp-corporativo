package com.jaasielsilva.portalceo.controller;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.VendaService;

@Controller
public class DashboardController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VendaService vendaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
    Usuario usuarioLogado = usuarioService.buscarPorEmail(principal.getName()).orElse(null);
    boolean isAdmin = usuarioLogado != null && usuarioLogado.getPerfis().stream()
                                .anyMatch(p -> p.getNome().equalsIgnoreCase("ADMIN"));

    long totalClientes = clienteService.contarTotal(); // <-- AQUI
    BigDecimal totalVendas = vendaService.getTotalVendas();
    model.addAttribute("totalVendas", totalVendas);
    model.addAttribute("usuarioLogado", usuarioLogado);
    model.addAttribute("isAdmin", isAdmin);
    model.addAttribute("totalClientes", totalClientes);   // <-- Corrigido

    return "dashboard/index";
}

}