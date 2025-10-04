package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        try {
            Usuario user = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("nome", user.getNome());
            userData.put("matricula", user.getMatricula());
            userData.put("nivelAcesso", user.getNivelAcesso());
            userData.put("perfis", user.getPerfis());

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
