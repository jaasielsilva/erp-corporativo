package com.jaasielsilva.portalceo.controller.ajuda;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.ajuda.AjudaCategoria;
import com.jaasielsilva.portalceo.model.ajuda.AjudaConteudo;
import com.jaasielsilva.portalceo.service.ajuda.HelpService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.ajuda.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ajuda")
public class AjudaRestController {
    @Autowired private HelpService helpService;
    @Autowired private AiAssistantService aiService;
    @Autowired private UsuarioService usuarioService;

    @GetMapping("/categorias")
    public List<AjudaCategoria> categorias() { return helpService.listarCategorias(); }

    @GetMapping("/busca")
    public Map<String,Object> busca(@RequestParam(required=false) String q,
                                    @RequestParam(required=false) String categoria,
                                    @RequestParam(defaultValue="0") int page,
                                    @RequestParam(defaultValue="10") int size,
                                    Authentication auth) {
        Usuario usuario = null;
        if (auth != null && auth.isAuthenticated()) {
            usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
        }
        Page<AjudaConteudo> res = helpService.buscar(q, categoria, page, size, usuario);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("total", res.getTotalElements());
        out.put("page", res.getNumber());
        out.put("size", res.getSize());
        out.put("items", res.getContent());
        return out;
    }

    @GetMapping("/conteudo/{id}")
    public ResponseEntity<AjudaConteudo> conteudo(@PathVariable Long id) {
        return helpService.obterConteudoIncrementandoView(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> feedback(@RequestParam Long conteudoId,
                                      @RequestParam boolean upvote,
                                      @RequestParam(required=false) String comentario,
                                      Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(helpService.registrarFeedback(conteudoId, usuario, upvote, comentario));
    }

    @PostMapping("/ia/answer")
    public ResponseEntity<Map<String,Object>> iaAnswer(@RequestParam String query, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(aiService.answer(query, usuario));
    }

    @PostMapping("/ia/generate")
    public ResponseEntity<Map<String,Object>> iaGenerate(@RequestParam String query, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(aiService.generateWithOllama(query, usuario));
    }

    @PostMapping("/handoff")
    public ResponseEntity<?> handoff(@RequestParam Long conversaId) {
        return ResponseEntity.ok(aiService.marcarEscalonado(conversaId));
    }
}
