package com.jaasielsilva.portalceo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;

@Controller
public class SenhaController {

    @Autowired
    private UsuarioService usuarioService;

    // Formulário para solicitar link de redefinição
    @GetMapping("/esqueci-senha")
    public String exibirFormularioEsqueciSenha() {
        return "senha/esqueci-senha";
    }

    // Envia e-mail com link
    @PostMapping("/esqueci-senha")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processarPedidoDeRedefinicao(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean enviado = usuarioService.enviarLinkRedefinicaoSenha(email);
            
            if (enviado) {
                response.put("status", "sucesso");
                response.put("mensagem", "Link de redefinição enviado com sucesso para " + email);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "erro");
                response.put("mensagem", "Email não encontrado no sistema.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "erro");
            response.put("mensagem", "Erro interno do servidor. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Página com formulário de nova senha (usando token)
    @GetMapping("/resetar-senha")
    public String exibirFormularioNovaSenha(@RequestParam String token, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.validarToken(token);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("erro", "Token inválido ou expirado.");
            return "senha/token-expirado";
        }
        model.addAttribute("token", token);
        return "senha/nova-senha";
    }

    // Processa nova senha
    @PostMapping("/resetar-senha")
    public String redefinirSenha(@RequestParam String token,
                                 @RequestParam String novaSenha,
                                 Model model) {
        boolean redefinido = usuarioService.redefinirSenhaComToken(token, novaSenha);
        model.addAttribute("mensagem", redefinido
                ? "Senha redefinida com sucesso!"
                : "Token inválido ou expirado.");
        return "senha/nova-senha";
    }
}
