package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.AcaoUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/impersonacao")
public class ImpersonacaoController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private AcaoUsuarioService acaoUsuarioService;

    @GetMapping
    @ResponseBody
    public Map<String, Object> estado(HttpSession session) {
        boolean ativa = Boolean.TRUE.equals(session.getAttribute("impersonacaoAtiva"));
        String email = (String) session.getAttribute("impersonacaoEmail");
        String nome = (String) session.getAttribute("impersonacaoNome");
        Map<String, Object> resp = new HashMap<>();
        resp.put("ativa", ativa);
        resp.put("email", email);
        resp.put("nome", nome);
        return resp;
    }

    @PostMapping("/iniciar")
    @ResponseBody
    public ResponseEntity<?> iniciar(@RequestParam("query") String query,
                                     Authentication auth,
                                     HttpSession session,
                                     HttpServletRequest request) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Não autenticado"));
        }

        // Somente usuários com perfil de administração podem trocar visão
        boolean podeImpersonar = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equalsIgnoreCase("ROLE_ADMIN") || a.equalsIgnoreCase("ROLE_MASTER") || a.equalsIgnoreCase("ROLE_ADMINISTRADOR"));

        if (!podeImpersonar) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Permissão insuficiente para trocar visão"));
        }

        // Normalizar consulta (email ou nome)
        String busca = Optional.ofNullable(query).map(String::trim).orElse("");
        if (busca.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe nome ou e-mail"));
        }

        // Tentativa por e-mail
        Optional<Usuario> alvoOpt = usuarioService.buscarPorEmail(busca);
        if (alvoOpt.isEmpty()) {
            // Tentar por nome
            List<Usuario> candidatos = usuarioService.buscarPorNomeOuEmail(busca);
            if (!candidatos.isEmpty()) {
                alvoOpt = Optional.of(candidatos.get(0));
            }
        }

        if (alvoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Usuário não encontrado pelo nome/e-mail informado"));
        }

        Usuario alvo = alvoOpt.get();

        // Não permitir trocar visão para si mesmo (desnecessário)
        if (alvo.getEmail().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Você já está na sua própria visão"));
        }

        // Configurar estado de impersonação em sessão
        session.setAttribute("impersonacaoAtiva", true);
        session.setAttribute("impersonacaoEmail", alvo.getEmail());
        session.setAttribute("impersonacaoNome", alvo.getNome());

        // Auditoria: início da impersonação
        var responsavelOpt = usuarioService.buscarPorEmail(auth.getName());
        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For")).map(h -> h.split(",")[0]).orElse(request.getRemoteAddr());
        responsavelOpt.ifPresent(resp -> acaoUsuarioService.registrarAcao("IMPERSONACAO_INICIO", alvo, resp, ip));

        return ResponseEntity.ok(Map.of(
                "ativa", true,
                "email", alvo.getEmail(),
                "nome", alvo.getNome()
        ));
    }

    @PostMapping("/encerrar")
    @ResponseBody
    public ResponseEntity<?> encerrar(HttpSession session,
                                      Authentication auth,
                                      HttpServletRequest request) {
        // Dados para auditoria antes de limpar
        String emailAlvo = (String) session.getAttribute("impersonacaoEmail");
        Optional<Usuario> alvoOpt = Optional.ofNullable(emailAlvo).flatMap(usuarioService::buscarPorEmail);
        Optional<Usuario> responsavelOpt = auth != null ? usuarioService.buscarPorEmail(auth.getName()) : Optional.<Usuario>empty();
        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For")).map(h -> h.split(",")[0]).orElse(request.getRemoteAddr());

        // Limpar sessão
        session.removeAttribute("impersonacaoAtiva");
        session.removeAttribute("impersonacaoEmail");
        session.removeAttribute("impersonacaoNome");

        // Auditoria: encerramento da impersonação
        if (alvoOpt.isPresent() && responsavelOpt.isPresent()) {
            acaoUsuarioService.registrarAcao("IMPERSONACAO_ENCERRAR", alvoOpt.get(), responsavelOpt.get(), ip);
        }

        return ResponseEntity.ok(Map.of("ativa", false));
    }
}