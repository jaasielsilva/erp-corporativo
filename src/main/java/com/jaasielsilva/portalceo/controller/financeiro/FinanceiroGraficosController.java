package com.jaasielsilva.portalceo.controller.financeiro;

import com.jaasielsilva.portalceo.dto.financeiro.RelatorioDREDTO;
import com.jaasielsilva.portalceo.dto.financeiro.RelatorioFluxoCaixaDTO;
import com.jaasielsilva.portalceo.service.RelatorioFinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financeiro/graficos")
@PreAuthorize("hasAnyRole('ADMIN', 'MASTER', 'FINANCEIRO_GESTOR')")
public class FinanceiroGraficosController {

    @Autowired
    private RelatorioFinanceiroService relatorioService;

    @GetMapping("/dre")
    public ResponseEntity<RelatorioDREDTO> getDadosDRE(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        return ResponseEntity.ok(relatorioService.gerarDRE(inicio, fim));
    }

    @GetMapping("/fluxo-caixa")
    public ResponseEntity<RelatorioFluxoCaixaDTO> getDadosFluxoCaixa(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        return ResponseEntity.ok(relatorioService.gerarFluxoCaixa(inicio, fim));
    }
}
