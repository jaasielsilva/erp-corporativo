package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.SolicitacaoFerias;
import com.jaasielsilva.portalceo.dto.SolicitacaoFeriasDTO;
import com.jaasielsilva.portalceo.model.SolicitacaoFerias.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.rh.SolicitacaoFeriasService;
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
@RequestMapping("/api/rh/ferias")
public class FeriasApiController {

    @Autowired
    private SolicitacaoFeriasService feriasService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/solicitacoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> solicitar(
            @RequestParam Long colaboradorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String observacoes,
            Principal principal,
            HttpServletRequest request) {

        Usuario usuarioLogado = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
        String ip = request != null ? request.getRemoteAddr() : null;

        try {
            SolicitacaoFerias s = feriasService.solicitar(colaboradorId, inicio, fim, observacoes, usuarioLogado, ip);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", s.getId(),
                    "status", s.getStatus().name()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/solicitacoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public ResponseEntity<Page<SolicitacaoFeriasDTO>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        StatusSolicitacao statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusSolicitacao.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Page<SolicitacaoFerias> resultado = feriasService.listar(statusEnum, inicio, fim, page, size);
        java.util.List<SolicitacaoFeriasDTO> dtos = resultado.getContent().stream().map(s ->
                new SolicitacaoFeriasDTO(
                        s.getId(),
                        s.getColaborador() != null ? s.getColaborador().getNome() : null,
                        s.getPeriodoInicio(),
                        s.getPeriodoFim(),
                        s.getStatus() != null ? s.getStatus().name() : null,
                        s.getObservacoes()
                )
        ).toList();
        Page<SolicitacaoFeriasDTO> pageDto = new PageImpl<>(dtos, PageRequest.of(page, size), resultado.getTotalElements());
        return ResponseEntity.ok(pageDto);
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ROLE_GERENCIAL','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String, Object>> aprovar(
            @PathVariable Long id,
            @RequestParam(required = false) String observacoes,
            Principal principal,
            HttpServletRequest request) {

        Usuario aprovador = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
        String ip = request != null ? request.getRemoteAddr() : null;

        try {
            SolicitacaoFerias s = feriasService.aprovar(id, aprovador, observacoes, ip);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", s.getId(),
                    "status", s.getStatus().name()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reprovar")
    @PreAuthorize("hasAnyRole('ROLE_GERENCIAL','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String, Object>> reprovar(
            @PathVariable Long id,
            @RequestParam(required = false) String observacoes,
            Principal principal,
            HttpServletRequest request) {

        Usuario aprovador = principal != null ? usuarioService.buscarPorEmail(principal.getName()).orElse(null) : null;
        String ip = request != null ? request.getRemoteAddr() : null;

        try {
            SolicitacaoFerias s = feriasService.reprovar(id, aprovador, observacoes, ip);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", s.getId(),
                    "status", s.getStatus().name()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
}
}

