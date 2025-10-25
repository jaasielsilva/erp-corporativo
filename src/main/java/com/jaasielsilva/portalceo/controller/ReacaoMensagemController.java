package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Mensagem;
import com.jaasielsilva.portalceo.model.ReacaoMensagem;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.MensagemRepository;
import com.jaasielsilva.portalceo.service.ReacaoMensagemService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/reacoes")
public class ReacaoMensagemController {

    @Autowired
    private ReacaoMensagemService reacaoMensagemService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MensagemRepository mensagemRepository;

    @PostMapping("/{mensagemId}")
    public ResponseEntity<?> adicionarOuRemoverReacao(
            @PathVariable Long mensagemId,
            @RequestBody Map<String, String> payload,
            Authentication auth) {
        try {
            String emoji = payload.get("emoji");
            if (emoji == null || emoji.isEmpty()) {
                return ResponseEntity.badRequest().body("Emoji não pode ser vazio");
            }

            Long usuarioId = usuarioService.buscarPorEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

            // Recuperar a mensagem para obter o ID da conversa
            Mensagem mensagem = mensagemRepository.findById(mensagemId)
                    .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));

            // Recuperar o usuário completo
            Usuario usuario = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            ReacaoMensagem reacao = reacaoMensagemService.adicionarReacao(mensagem, usuario, emoji);

            if (reacao == null) {
                return ResponseEntity.ok().body("Reação removida com sucesso");
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(reacao);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @GetMapping("/{mensagemId}")
    public ResponseEntity<List<ReacaoMensagem>> buscarReacoesPorMensagem(@PathVariable Long mensagemId) {
        List<ReacaoMensagem> reacoes = reacaoMensagemService.buscarReacoesPorMensagem(mensagemId);
        return ResponseEntity.ok(reacoes);
    }
}