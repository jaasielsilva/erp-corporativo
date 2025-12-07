package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.dto.AvaliacaoDesempenhoDTO;
import com.jaasielsilva.portalceo.model.AvaliacaoDesempenho;
import com.jaasielsilva.portalceo.model.AvaliacaoDesempenho.StatusAvaliacao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.rh.AvaliacaoDesempenhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/rh/avaliacao")
public class AvaliacaoApiController {

    @Autowired
    private AvaliacaoDesempenhoService service;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/ciclos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String,Object>> abrir(@RequestParam Long colaboradorId,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                                    Principal principal,
                                                    HttpServletRequest request){
        try {
            Usuario usuario = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
            String ip = request != null ? request.getRemoteAddr() : null;
            AvaliacaoDesempenho a = service.abrir(colaboradorId, inicio, fim, usuario, ip);
            return ResponseEntity.ok(Map.of("success", true, "id", a.getId()));
        } catch (IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/ciclos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Page<AvaliacaoDesempenhoDTO>> listar(@RequestParam(required = false) String status,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size){
        StatusAvaliacao statusEnum = null;
        if (status != null && !status.isBlank()){
            try { statusEnum = StatusAvaliacao.valueOf(status.trim().toUpperCase()); } catch (IllegalArgumentException ignored){}
        }
        Page<AvaliacaoDesempenho> result = service.listar(statusEnum, inicio, fim, page, size);
        var dtos = result.getContent().stream().map(a -> new AvaliacaoDesempenhoDTO(
                a.getId(),
                a.getColaborador() != null ? a.getColaborador().getNome() : null,
                a.getAvaliador() != null ? a.getAvaliador().getEmail() : null,
                a.getPeriodoInicio(), a.getPeriodoFim(), a.getStatus() != null ? a.getStatus().name() : null,
                a.getNota(), a.getFeedback()
        )).toList();
        return ResponseEntity.ok(new PageImpl<>(dtos, PageRequest.of(page,size), result.getTotalElements()));
    }

    @PostMapping("/{id}/submeter")
    @PreAuthorize("hasAnyRole('ROLE_GERENCIAL','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> submeter(@PathVariable Long id,
                                                       @RequestParam Double nota,
                                                       @RequestParam(required = false) String feedback,
                                                       Principal principal,
                                                       HttpServletRequest request){
        try {
            Usuario usuario = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
            String ip = request != null ? request.getRemoteAddr() : null;
            AvaliacaoDesempenho a = service.submeter(id, nota, feedback, usuario, ip);
            return ResponseEntity.ok(Map.of("success", true, "status", a.getStatus().name()));
        } catch (IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> aprovar(@PathVariable Long id,
                                                      @RequestParam(required = false) String observacoes,
                                                      Principal principal,
                                                      HttpServletRequest request){
        try {
            Usuario usuario = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
            String ip = request != null ? request.getRemoteAddr() : null;
            AvaliacaoDesempenho a = service.aprovar(id, usuario, observacoes, ip);
            return ResponseEntity.ok(Map.of("success", true, "status", a.getStatus().name()));
        } catch (IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reprovar")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> reprovar(@PathVariable Long id,
                                                       @RequestParam(required = false) String observacoes,
                                                       Principal principal,
                                                       HttpServletRequest request){
        try {
            Usuario usuario = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
            String ip = request != null ? request.getRemoteAddr() : null;
            AvaliacaoDesempenho a = service.reprovar(id, usuario, observacoes, ip);
            return ResponseEntity.ok(Map.of("success", true, "status", a.getStatus().name()));
        } catch (IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

