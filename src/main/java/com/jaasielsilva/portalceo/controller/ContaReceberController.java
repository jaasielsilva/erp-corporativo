package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.ContaReceber;
import com.jaasielsilva.portalceo.service.ContaReceberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaReceberController {

    private final ContaReceberService contaReceberService;

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Long>> getResumoContas() {
        Map<String, Long> resumo = contaReceberService.getResumoContasParaFront();
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/vencidas")
    public ResponseEntity<?> getContasVencidas() {
        return ResponseEntity.ok(contaReceberService.findVencidas());
    }

    @GetMapping("/inadimplentes")
    public ResponseEntity<?> getContasInadimplentes() {
        return ResponseEntity.ok(contaReceberService.findInadimplentes());
    }
}
