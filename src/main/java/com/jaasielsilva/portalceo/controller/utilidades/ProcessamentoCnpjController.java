package com.jaasielsilva.portalceo.controller.utilidades;

import com.jaasielsilva.portalceo.service.cnpj.ProcessamentoCnpjService;
import com.jaasielsilva.portalceo.dto.cnpj.ProcessamentoStatusDto;
import com.jaasielsilva.portalceo.service.cnpj.SanitizadorCnpjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/utilidades")
public class ProcessamentoCnpjController {

    @Autowired
    private ProcessamentoCnpjService processamentoCnpjService;

    @Autowired
    private SanitizadorCnpjService sanitizadorCnpjService;

    @GetMapping("/processar-cnpj")
    public String processarCnpjPage() {
        return "utilidades/processar-cnpj";
    }

    @PostMapping("/processar")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> iniciarProcessamento(@RequestParam(name = "concurrency", required = false) Integer concurrency) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        String protocolo = "PROC" + System.currentTimeMillis();
        if (concurrency != null) {
            processamentoCnpjService.setConcurrency(concurrency);
        }
        processamentoCnpjService.processarAsync(email, protocolo);
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("message", "Processamento iniciado");
        body.put("protocol", protocolo);
        return ResponseEntity.accepted().body(body);
    }

    @GetMapping("/processamento-status")
    @ResponseBody
    public ResponseEntity<ProcessamentoStatusDto> status() {
        return ResponseEntity.ok(processamentoCnpjService.status());
    }

    @PostMapping("/processar/pause")
    @ResponseBody
    public ResponseEntity<String> pause() {
        processamentoCnpjService.pause();
        return ResponseEntity.ok("paused");
    }

    @PostMapping("/processar/resume")
    @ResponseBody
    public ResponseEntity<String> resume() {
        processamentoCnpjService.resume();
        return ResponseEntity.ok("resumed");
    }

    @PostMapping("/processar/cancel")
    @ResponseBody
    public ResponseEntity<String> cancel() {
        processamentoCnpjService.cancel();
        return ResponseEntity.ok("canceled");
    }
    @PostMapping("/sanitizar-cnpj")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> sanitizar(@RequestParam(name = "apply", defaultValue = "false") boolean apply) {
        return ResponseEntity.ok(sanitizadorCnpjService.sanitizar(apply));
    }
}