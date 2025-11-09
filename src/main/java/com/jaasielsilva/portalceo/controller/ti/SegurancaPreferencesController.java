package com.jaasielsilva.portalceo.controller.ti;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/ti/api/seguranca/preferences")
public class SegurancaPreferencesController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<?> getMinhasPreferencias(Authentication authentication) {
        return usuarioService.buscarPorEmail(authentication.getName())
                .map(u -> ResponseEntity.ok(Map.of(
                        "mutarToasts", u.getMutarToasts(),
                        "preferirBannerSeguranca", u.getPreferirBannerSeguranca(),
                        "volumeNotificacao", u.getVolumeNotificacao(),
                        "naoPerturbeAtivo", u.getNaoPerturbeAtivo(),
                        "naoPerturbeAte", u.getNaoPerturbeAte()
                )))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping
    public ResponseEntity<?> atualizarPreferencias(@RequestBody Map<String, Object> body,
                                                   Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.badRequest().build();

        Boolean mutarToasts = (Boolean) body.getOrDefault("mutarToasts", usuario.getMutarToasts());
        Boolean preferirBannerSeguranca = (Boolean) body.getOrDefault("preferirBannerSeguranca", usuario.getPreferirBannerSeguranca());
        Integer volumeNotificacao = body.get("volumeNotificacao") instanceof Number ? ((Number) body.get("volumeNotificacao")).intValue() : usuario.getVolumeNotificacao();
        Boolean naoPerturbeAtivo = (Boolean) body.getOrDefault("naoPerturbeAtivo", usuario.getNaoPerturbeAtivo());
        LocalDateTime naoPerturbeAte = null;
        Object dnd = body.get("naoPerturbeAte");
        if (dnd instanceof String) {
            try {
                naoPerturbeAte = LocalDateTime.parse((String) dnd);
            } catch (Exception ignored) {}
        }

        usuarioService.atualizarPreferenciasSeguranca(usuario.getId(), mutarToasts, preferirBannerSeguranca,
                volumeNotificacao, naoPerturbeAtivo, naoPerturbeAte);

        return ResponseEntity.ok(Map.of("ok", true));
    }
}