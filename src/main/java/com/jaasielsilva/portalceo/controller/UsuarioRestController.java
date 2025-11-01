package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.dto.UsuarioBuscaDTO;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Busca usuários para iniciar nova conversa (exclui o usuário logado e retorna apenas ATIVOS)
     * Endpoint: GET /api/usuarios/busca?q=termo
     */
    @GetMapping("/busca")
    public ResponseEntity<List<UsuarioBuscaDTO>> buscarUsuarios(
            @RequestParam(name = "q", required = false) String q,
            Authentication auth) {
        try {
            Usuario current = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
            if (current == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Usuario> base = (q == null || q.isBlank())
                    ? usuarioService.buscarTodos()
                    : usuarioService.buscarPorNomeOuEmail(q);

            List<UsuarioBuscaDTO> result = base.stream()
                    .filter(u -> u != null)
                    .filter(u -> u.getStatus() == Usuario.Status.ATIVO)
                    .filter(u -> !u.getId().equals(current.getId()))
                    .map(u -> new UsuarioBuscaDTO(
                            u.getId(),
                            u.getNome(),
                            u.getDepartamento() != null ? u.getDepartamento().getNome() : null,
                            u.isOnline()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}