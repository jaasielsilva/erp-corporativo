package com.jaasielsilva.portalceo.controller.rh.ponto;

import com.jaasielsilva.portalceo.dto.CorrecaoPontoListDTO;
import com.jaasielsilva.portalceo.dto.CorrecaoPontoCreateDTO;
import com.jaasielsilva.portalceo.dto.CorrecaoPontoResumoDTO;
import com.jaasielsilva.portalceo.model.CorrecaoPonto;
import com.jaasielsilva.portalceo.service.CorrecaoPontoService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/rh/ponto-escalas/correcoes")
public class CorrecaoPontoApiController {

    private final CorrecaoPontoService service;

    public CorrecaoPontoApiController(CorrecaoPontoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CorrecaoPontoListDTO> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long colaboradorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.listar(inicio, fim, colaboradorId, status, tipo, page, size);
    }

    @GetMapping("/resumo")
    public CorrecaoPontoResumoDTO resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return service.resumo(inicio, fim);
    }

    @PostMapping
    public CorrecaoPonto criar(@RequestBody CorrecaoPontoCreateDTO dto, Authentication auth) {
        String login = auth != null ? auth.getName() : null;
        return service.criar(dto, login);
    }
}