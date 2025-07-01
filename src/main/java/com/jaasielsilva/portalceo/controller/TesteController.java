package com.jaasielsilva.portalceo.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;

@RestController
@RequestMapping("/teste")
public class TesteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/usuario-perfis")
    public String testePerfis(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return "Usuário não encontrado";
        }
        String perfis = usuario.getPerfis().stream()
                .map(Perfil::getNome)
                .collect(Collectors.joining(", "));
        return "Usuário: " + usuario.getNome() + " - Perfis: " + perfis;
    }
}
