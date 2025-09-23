package com.jaasielsilva.portalceo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.portalceo.service.NotificacaoService;
import com.jaasielsilva.portalceo.service.NotificacaoService.Notificacao;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Obtém todas as notificações do usuário logado
     */
    @GetMapping
    public ResponseEntity<List<Notificacao>> obterNotificacoes(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            List<Notificacao> notificacoes = notificacaoService.obterNotificacoes(emailUsuario);
            return ResponseEntity.ok(notificacoes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém apenas as notificações não lidas do usuário logado
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<List<Notificacao>> obterNotificacaoesNaoLidas(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            List<Notificacao> notificacoes = notificacaoService.obterNotificacaoesNaoLidas(emailUsuario);
            return ResponseEntity.ok(notificacoes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém o contador de notificações não lidas
     */
    @GetMapping("/contador")
    public ResponseEntity<Map<String, Object>> obterContadorNotificacoes(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            long contador = notificacaoService.contarNotificacaoesNaoLidas(emailUsuario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("contador", contador);
            response.put("temNovas", contador > 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marca uma notificação específica como lida
     */
    @PostMapping("/{notificacaoId}/marcar-lida")
    public ResponseEntity<Map<String, String>> marcarComoLida(
            @PathVariable String notificacaoId, 
            Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            notificacaoService.marcarComoLida(emailUsuario, notificacaoId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notificação marcada como lida");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erro ao marcar notificação como lida");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Marca todas as notificações como lidas
     */
    @PostMapping("/marcar-todas-lidas")
    public ResponseEntity<Map<String, String>> marcarTodasComoLidas(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            notificacaoService.marcarTodasComoLidas(emailUsuario);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Todas as notificações foram marcadas como lidas");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erro ao marcar notificações como lidas");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint para testar notificações (apenas para desenvolvimento)
     */
    @PostMapping("/teste")
    public ResponseEntity<Map<String, String>> criarNotificacaoTeste(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            
            notificacaoService.adicionarNotificacao(
                emailUsuario,
                "Notificação de Teste",
                "Esta é uma notificação de teste do sistema",
                "info",
                "/dashboard"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notificação de teste criada");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erro ao criar notificação de teste");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}