package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoCurso;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoInstrutor;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoTurma;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoMatricula;
import com.jaasielsilva.portalceo.model.treinamentos.TreinamentoFrequencia;
import com.jaasielsilva.portalceo.service.rh.treinamentos.TreinamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/rh/treinamentos")
public class TreinamentosApiController {
    @Autowired
    private TreinamentoService service;

    @GetMapping("/cursos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<TreinamentoCurso>> listarCursos() { return ResponseEntity.ok(service.listarCursos()); }

    @GetMapping("/instrutores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<TreinamentoInstrutor>> listarInstrutores() { return ResponseEntity.ok(service.listarInstrutores()); }

    @PostMapping("/cursos")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> criarCurso(@RequestParam String titulo,
                                                         @RequestParam(required = false) String descricao,
                                                         @RequestParam(required = false) String categoria,
                                                         @RequestParam(required = false) Integer cargaHoraria) {
        TreinamentoCurso c = service.criarCurso(titulo, descricao, categoria, cargaHoraria);
        return ResponseEntity.ok(Map.of("success", true, "id", c.getId()));
    }

    @PostMapping("/instrutores")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> criarInstrutor(@RequestParam String nome,
                                                             @RequestParam(required = false) String email,
                                                             @RequestParam(required = false) String bio) {
        TreinamentoInstrutor i = service.criarInstrutor(nome, email, bio);
        return ResponseEntity.ok(Map.of("success", true, "id", i.getId()));
    }

    @PostMapping("/turmas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> criarTurma(@RequestParam Long cursoId,
                                                         @RequestParam(required = false) Long instrutorId,
                                                         @RequestParam String inicio,
                                                         @RequestParam String fim,
                                                         @RequestParam(required = false) String local,
                                                         @RequestParam(required = false) Integer capacidade) {
        TreinamentoTurma t = service.criarTurma(cursoId, instrutorId,
                LocalDateTime.parse(inicio), LocalDateTime.parse(fim), local, capacidade);
        return ResponseEntity.ok(Map.of("success", true, "id", t.getId(), "agendaEventoId", t.getAgendaEventoId()));
    }

    @PostMapping("/turmas/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<Map<String,Object>> alterarStatusTurma(@PathVariable Long id,
                                                                 @RequestParam String status) {
        TreinamentoTurma t = service.alterarStatusTurma(id, status);
        return ResponseEntity.ok(Map.of("success", true, "status", t.getStatus()));
    }

    @GetMapping("/turmas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Page<TreinamentoTurma>> listarTurmas(@RequestParam(required = false) String status,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listarTurmas(status, page, size));
    }

    @PostMapping("/turmas/{turmaId}/matriculas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String,Object>> matricular(@PathVariable Long turmaId,
                                                         @RequestParam Long colaboradorId) {
        TreinamentoMatricula m = service.matricular(turmaId, colaboradorId);
        return ResponseEntity.ok(Map.of("success", true, "id", m.getId()));
    }

    @PostMapping("/matriculas/{matriculaId}/frequencia")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String,Object>> registrarFrequencia(@PathVariable Long matriculaId,
                                                                  @RequestParam String dataHora,
                                                                  @RequestParam boolean presente,
                                                                  @RequestParam(required = false) String observacoes) {
        TreinamentoFrequencia f = service.registrarFrequencia(matriculaId, LocalDateTime.parse(dataHora), presente, observacoes);
        return ResponseEntity.ok(Map.of("success", true, "id", f.getId()));
    }

    @PostMapping("/matriculas/{matriculaId}/avaliacao")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<Map<String,Object>> avaliar(@PathVariable Long matriculaId,
                                                      @RequestParam Integer nota,
                                                      @RequestParam(required = false) String feedback) {
        var a = service.avaliar(matriculaId, nota, feedback);
        return ResponseEntity.ok(Map.of("success", true, "id", a.getId()));
    }

    @GetMapping("/relatorios/turmas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<java.util.Map<String,Object>>> relatorioTurmas(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.relatorioTurmas(status));
    }

    @GetMapping(value = "/relatorios/export.csv", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.exportCsv(status));
    }

    @GetMapping(value = "/relatorios/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String status) {
        byte[] pdf = service.exportPdf(status);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @PostMapping("/turmas/{id}/enviar-lembretes")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER')")
    public ResponseEntity<java.util.Map<String,Object>> enviarLembretes(@PathVariable Long id) {
        service.enviarLembretesTurma(id);
        return ResponseEntity.ok(java.util.Map.of("success", true));
    }

    @GetMapping(value = "/certificados/{matriculaId}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<byte[]> certificado(@PathVariable Long matriculaId) {
        byte[] pdf = service.gerarCertificado(matriculaId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/turmas/{id}")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<TreinamentoTurma> getTurma(@PathVariable Long id) {
        return ResponseEntity.of(service.findTurma(id));
    }

    @GetMapping("/turmas/{id}/matriculas")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<TreinamentoMatricula>> listarMatriculas(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarMatriculasPorTurma(id));
    }

    @GetMapping("/matriculas/{id}/frequencias")
    @PreAuthorize("hasAnyRole('ROLE_RH','ROLE_ADMIN','ROLE_MASTER','ROLE_GERENCIAL')")
    public ResponseEntity<java.util.List<TreinamentoFrequencia>> listarFrequencias(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarFrequenciasPorMatricula(id));
    }
}
