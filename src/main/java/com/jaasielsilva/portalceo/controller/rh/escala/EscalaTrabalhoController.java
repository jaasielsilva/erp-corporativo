package com.jaasielsilva.portalceo.controller.rh.escala;

import com.jaasielsilva.portalceo.dto.rh.escala.EscalaTrabalhoDTO;
import com.jaasielsilva.portalceo.model.EscalaTrabalho;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.rh.escala.EscalaTrabalhoService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escalas")
public class EscalaTrabalhoController {

    private final EscalaTrabalhoService service;

    public EscalaTrabalhoController(EscalaTrabalhoService service) {
        this.service = service;
    }

    @PostMapping("/criar")
    public EscalaTrabalho criarEscala(@RequestBody EscalaTrabalhoDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado(); // Implementar de acordo com sua autenticação
        return service.criarEscala(dto, usuarioLogado);
    }

    private Usuario getUsuarioLogado() {
        // Exemplo: pegar usuário logado do Spring Security
        return new Usuario(); // Placeholder
    }
}
