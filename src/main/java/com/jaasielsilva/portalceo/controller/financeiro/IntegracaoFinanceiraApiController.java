package com.jaasielsilva.portalceo.controller.financeiro;

import com.jaasielsilva.portalceo.dto.financeiro.IntegracaoFolhaDTO;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.IntegracaoFinanceiraService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/integracao/financeiro")
public class IntegracaoFinanceiraApiController {

    @Autowired
    private IntegracaoFinanceiraService integracaoService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/folha/{folhaId}/enviar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER', 'RH_GESTOR')")
    public ResponseEntity<?> enviarFolha(@PathVariable Long folhaId, Principal principal) {
        try {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            IntegracaoFolhaDTO dto = integracaoService.enviarFolhaParaFinanceiro(folhaId, usuario);
            return ResponseEntity.ok(dto);
        } catch (ConstraintViolationException e) {
            String erros = e.getConstraintViolations().stream()
                    .map(cv -> cv.getMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body("Erro de validação: " + erros);
        } catch (Exception e) {
            // Tenta extrair mensagem mais limpa se for exceção envelopada
            String message = e.getMessage();
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
                message = cve.getConstraintViolations().stream()
                        .map(cv -> cv.getMessage())
                        .collect(Collectors.joining("; "));
            }
            return ResponseEntity.badRequest().body("Erro na integração: " + message);
        }
    }
}
