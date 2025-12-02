package com.jaasielsilva.portalceo.controller.utilidades;

import com.jaasielsilva.portalceo.service.cnpj.ProcessamentoCnpjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/utilidades")
public class ProcessamentoCnpjController {

    @Autowired
    private ProcessamentoCnpjService processamentoCnpjService;

    @GetMapping("/processar-cnpj")
    public String processarCnpjPage() {
        return "utilidades/processar-cnpj";
    }

    @PostMapping("/processar")
    @ResponseBody
    public ResponseEntity<String> iniciarProcessamento() {
        processamentoCnpjService.processarAsync();
        return ResponseEntity.accepted().body("Processamento iniciado");
    }

    @GetMapping("/processamento-status")
    @ResponseBody
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(processamentoCnpjService.status());
    }
}

