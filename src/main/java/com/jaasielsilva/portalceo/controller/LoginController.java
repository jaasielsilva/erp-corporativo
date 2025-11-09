package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ti.AlertaSeguranca;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.ti.AlertaSegurancaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AlertaSegurancaRepository alertaSegurancaRepository;

    @GetMapping({ "/", "/login" })
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
            @RequestParam String password,
            Model model) {

        var usuarioOpt = usuarioRepository.findByEmail(username);

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(password, usuarioOpt.get().getSenha())) {
            alertaSegurancaRepository.save(new AlertaSeguranca(
                    null,
                    "Tentativa de login suspeita",
                    "MEDIA",
                    LocalDateTime.now(),
                    "Web",
                    "Usuário ou senha incorretos para: " + username));

            model.addAttribute("erro", "Usuário ou senha inválidos.");
            return "login";
        }

        // login ok
        return "redirect:/dashboard";
    }

}
