package com.jaasielsilva.portalceo.controller.marketing;

import com.jaasielsilva.portalceo.model.CampanhaMarketing;
import com.jaasielsilva.portalceo.service.CampanhaMarketingService;
import com.jaasielsilva.portalceo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/marketing/campanhas/{id}/publico-alvo")
@RequiredArgsConstructor
@PreAuthorize("@globalControllerAdvice.podeAcessarMarketing()")
public class PublicoAlvoController {

    private final CampanhaMarketingService campanhaService;
    private final ClienteService clienteService;

    @GetMapping
    public String gerenciarPublicoAlvo(@PathVariable Long id, Model model) {
        Optional<CampanhaMarketing> campanhaOpt = campanhaService.findById(id);
        if (campanhaOpt.isEmpty()) return "redirect:/marketing/campanhas";

        CampanhaMarketing campanha = campanhaOpt.get();
        model.addAttribute("campanha", campanha);
        model.addAttribute("clientes", clienteService.buscarTodos());
        return "marketing/campanhas/publico-alvo";
    }

    @PostMapping("/adicionar")
    @ResponseBody
    public ResponseEntity<?> adicionarClientes(@PathVariable Long id, @RequestParam List<Long> clienteIds) {
        try {
            campanhaService.adicionarClientesPublicoAlvo(id, clienteIds);
            return ResponseEntity.ok("Clientes adicionados ao público-alvo com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
