package com.jaasielsilva.portalceo.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute("usuario")
    public Usuario getUsuarioLogado(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioService.buscarPorEmail(principal.getName()).orElse(null);
    }
}

