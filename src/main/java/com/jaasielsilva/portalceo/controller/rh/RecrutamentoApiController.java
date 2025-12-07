package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.recrutamento.*;
import com.jaasielsilva.portalceo.service.rh.recrutamento.RecrutamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rh/recrutamento")
public class RecrutamentoApiController {
    @Autowired private RecrutamentoService service;

    @PostMapping("/candidatos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> criarCandidato(@RequestParam String nome,
                                                             @RequestParam(required = false) String email,
                                                             @RequestParam(required = false) String telefone,
                                                             @RequestParam(required = false) String genero,
                                                             @RequestParam(required = false) String dataNascimento) {
        RecrutamentoCandidato c = service.criarCandidato(nome, email, telefone, genero, dataNascimento!=null? LocalDate.parse(dataNascimento): null);
        return ResponseEntity.ok(Map.of("success", true, "id", c.getId()));
    }

    @PostMapping("/candidatos/{id}/experiencias")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> adicionarExperiencia(@PathVariable Long id,
                                                                   @RequestParam String empresa,
                                                                   @RequestParam String cargo,
                                                                   @RequestParam(required = false) String inicio,
                                                                   @RequestParam(required = false) String fim,
                                                                   @RequestParam(required = false) String descricao) {
        var e = service.adicionarExperiencia(id, empresa, cargo, inicio!=null? LocalDate.parse(inicio): null, fim!=null? LocalDate.parse(fim): null, descricao);
        return ResponseEntity.ok(Map.of("success", true, "id", e.getId()));
    }

    @PostMapping("/candidatos/{id}/formacoes")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> adicionarFormacao(@PathVariable Long id,
                                                                @RequestParam String instituicao,
                                                                @RequestParam String curso,
                                                                @RequestParam String nivel,
                                                                @RequestParam(required = false) String inicio,
                                                                @RequestParam(required = false) String fim) {
        var f = service.adicionarFormacao(id, instituicao, curso, nivel, inicio!=null? LocalDate.parse(inicio): null, fim!=null? LocalDate.parse(fim): null);
        return ResponseEntity.ok(Map.of("success", true, "id", f.getId()));
    }

    @PostMapping("/candidatos/{id}/habilidades")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> adicionarHabilidade(@PathVariable Long id,
                                                                  @RequestParam String nome,
                                                                  @RequestParam(required = false) String nivel) {
        var h = service.adicionarHabilidade(id, nome, nivel);
        return ResponseEntity.ok(Map.of("success", true, "id", h.getId()));
    }

    @PostMapping("/vagas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> criarVaga(@RequestParam String titulo,
                                                        @RequestParam(required = false) String descricao,
                                                        @RequestParam(required = false) String departamento,
                                                        @RequestParam(required = false) String senioridade,
                                                        @RequestParam(required = false) String localidade,
                                                        @RequestParam(required = false) String tipoContrato) {
        var v = service.criarVaga(titulo, descricao, departamento, senioridade, localidade, tipoContrato);
        return ResponseEntity.ok(Map.of("success", true, "id", v.getId()));
    }

    @GetMapping("/vagas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Page<RecrutamentoVaga>> listarVagas(@RequestParam(required = false) String status,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listarVagas(status, page, size));
    }

    @GetMapping("/candidatos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Page<RecrutamentoCandidato>> listarCandidatos(@RequestParam(required = false) String q,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listarCandidatos(q, page, size));
    }

    @PostMapping("/candidaturas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> candidatar(@RequestParam Long candidatoId,
                                                         @RequestParam Long vagaId,
                                                         @RequestParam(required = false) String origem) {
        var c = service.candidatar(candidatoId, vagaId, origem);
        return ResponseEntity.ok(Map.of("success", true, "id", c.getId()));
    }

    @GetMapping("/candidaturas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<RecrutamentoCandidatura>> listarCandidaturas(@RequestParam(required = false) Long vagaId,
                                                                                       @RequestParam(required = false) String etapa) {
        return ResponseEntity.ok(service.listarCandidaturas(vagaId, etapa));
    }

    @PostMapping("/candidaturas/{id}/entrevistas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> agendarEntrevista(@PathVariable Long id,
                                                                @RequestParam String inicio,
                                                                @RequestParam String fim,
                                                                @RequestParam(required = false) String local,
                                                                @RequestParam(required = false) String tipo) {
        var e = service.agendarEntrevista(id, LocalDateTime.parse(inicio), LocalDateTime.parse(fim), local, tipo);
        return ResponseEntity.ok(Map.of("success", true, "id", e.getId(), "agendaEventoId", e.getAgendaEventoId()));
    }

    @PostMapping("/candidaturas/{id}/avaliacao")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String,Object>> avaliar(@PathVariable Long id,
                                                      @RequestParam Integer nota,
                                                      @RequestParam(required = false) String feedback) {
        var a = service.avaliar(id, nota, feedback);
        return ResponseEntity.ok(Map.of("success", true, "id", a.getId()));
    }

    @PostMapping("/candidaturas/{id}/etapa")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> alterarEtapa(@PathVariable Long id,
                                                           @RequestParam String etapa) {
        var c = service.alterarEtapa(id, etapa);
        return ResponseEntity.ok(Map.of("success", true, "etapa", c.getEtapa()));
    }

    @PostMapping("/vagas/{id}/divulgacoes")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> divulgar(@PathVariable Long id,
                                                       @RequestParam String plataforma,
                                                       @RequestParam String url) {
        var d = service.registrarDivulgacao(id, plataforma, url);
        return ResponseEntity.ok(Map.of("success", true, "id", d.getId()));
    }

    @GetMapping("/relatorios/metrics")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<List<Map<String,Object>>> relatorio(@RequestParam(required = false) String etapa) {
        return ResponseEntity.ok(service.relatorioMetrics(etapa));
    }

    @GetMapping("/relatorios/por-etapa")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<java.util.Map<String,Object>>> porEtapa() {
        return ResponseEntity.ok(service.relatorioPorEtapa());
    }

    @GetMapping(value = "/relatorios/export.csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) String etapa) { return ResponseEntity.ok(service.exportCsv(etapa)); }

    @GetMapping(value = "/relatorios/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String etapa) {
        byte[] pdf = service.exportPdf(etapa);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @PostMapping("/vagas/{id}/divulgar-externo")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<java.util.Map<String,Object>> divulgarExterno(@PathVariable Long id,
                                                                        @RequestParam String plataforma,
                                                                        @RequestParam String url) {
        return ResponseEntity.ok(service.enviarDivulgacaoExterna(id, plataforma, url));
    }
}
