package com.jaasielsilva.portalceo.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String processarPedidoDeRedefinicao(@RequestParam String email, Model model) {
        boolean enviado = usuarioService.enviarLinkRedefinicaoSenha(email);
        model.addAttribute("mensagem", enviado
                ? "Link de redefinição enviado com sucesso para " + email
                : "Email não encontrado no sistema.");
        return "senha/esqueci-senha";
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
