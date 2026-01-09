package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.juridico.ProcessoJuridicoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.service.ProcessoPrevidenciarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/juridico/processos")
@RequiredArgsConstructor
@Slf4j
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('MENU_JURIDICO_PROCESSOS', 'ROLE_ADMIN', 'ROLE_MASTER')")
public class ProcessoJuridicoController {

    private final ProcessoJuridicoService processoService;
    private final ProcessoPrevidenciarioService processoPrevidenciarioService;
    private final ClienteService clienteService;

    @GetMapping("/novo")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('MENU_JURIDICO_PROCESSOS_NOVO', 'ROLE_ADMIN', 'ROLE_MASTER')")
    public String novo(@RequestParam(value = "clienteId", required = false) Long clienteId,
                       @RequestParam(value = "id", required = false) Long id,
                       Model model) {
        ProcessoJuridico processo;
        
        if (id != null) {
            processo = processoService.buscarPorId(id).orElse(new ProcessoJuridico());
        } else {
            processo = new ProcessoJuridico();
            if (clienteId != null) {
                clienteService.buscarPorId(clienteId).ifPresent(processo::setCliente);
            }
        }
        
        model.addAttribute("processo", processo);
        model.addAttribute("clientes", clienteService.buscarAtivos());
        model.addAttribute("tiposAcao", ProcessoJuridico.TipoAcaoJuridica.values());
        
        return "juridico/processo-form";
    }

    @GetMapping("/{id}/detalhes")
    public String detalhes(@PathVariable Long id, Model model) {
        return processoService.buscarPorId(id).map(processo -> {
            model.addAttribute("processo", processo);
            model.addAttribute("andamentos", processoService.listarAndamentos(id));
            model.addAttribute("audiencias", processoService.listarAudienciasDoProcesso(id));
            model.addAttribute("prazos", processoService.listarPrazosDoProcesso(id));
            return "juridico/processo-detalhes";
        }).orElse("redirect:/juridico/processos");
    }

    // --- Endpoints para Sub-recursos (Andamentos, Prazos, Audiências) ---

    @PostMapping("/andamentos/salvar")
    @ResponseBody
    public ResponseEntity<?> salvarAndamento(@RequestParam Long processoId, 
                                  @RequestParam String titulo,
                                  @RequestParam String descricao,
                                  @RequestParam(defaultValue = "ANDAMENTO") String tipoEtapa) {
        try {
            processoService.adicionarAndamento(processoId, titulo, descricao, tipoEtapa);
            return ResponseEntity.ok(Map.of("mensagem", "Andamento registrado!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao salvar andamento: " + e.getMessage()));
        }
    }

    @PostMapping("/prazos/salvar")
    @ResponseBody
    public ResponseEntity<?> salvarPrazo(@RequestParam Long processoId,
                              @RequestParam String dataLimite,
                              @RequestParam String descricao,
                              @RequestParam String responsabilidade) {
        try {
            processoService.adicionarPrazo(processoId, dataLimite, descricao, responsabilidade);
            return ResponseEntity.ok(Map.of("mensagem", "Prazo registrado!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao salvar prazo: " + e.getMessage()));
        }
    }

    @PostMapping("/prazos/concluir")
    public String concluirPrazo(@RequestParam Long prazoId, @RequestParam Long processoId, RedirectAttributes redirectAttributes) {
        processoService.concluirPrazo(prazoId);
        redirectAttributes.addFlashAttribute("mensagem", "Prazo concluído!");
        return "redirect:/juridico/processos/" + processoId + "/detalhes";
    }

    @PostMapping("/audiencias/salvar")
    @ResponseBody
    public ResponseEntity<?> salvarAudiencia(@RequestParam Long processoId,
                                  @RequestParam String dataHora,
                                  @RequestParam String tipo,
                                  @RequestParam String observacoes) {
        try {
            processoService.adicionarAudiencia(processoId, dataHora, tipo, observacoes);
            return ResponseEntity.ok(Map.of("mensagem", "Audiência agendada!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao agendar audiência: " + e.getMessage()));
        }
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ProcessoJuridico processo, RedirectAttributes redirectAttributes) {
        try {
            ProcessoJuridico salvo = processoService.salvar(processo);
            log.info("Processo salvo com sucesso: ID {}", salvo.getId());
            redirectAttributes.addFlashAttribute("mensagem", "Processo salvo com sucesso!");
            return "redirect:/juridico/processos/" + salvo.getId() + "/detalhes";
        } catch (Exception e) {
            log.error("Erro ao salvar processo", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar processo: " + e.getMessage());
            return "redirect:/juridico/processos/novo";
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public String listarPorCliente(@PathVariable Long clienteId, 
                                   @RequestParam(defaultValue = "0") int page,
                                   Model model) {
        Page<ProcessoJuridico> processos = processoService.listarPorCliente(clienteId, PageRequest.of(page, 10));
        clienteService.buscarPorId(clienteId).ifPresent(cliente -> model.addAttribute("cliente", cliente));
        
        model.addAttribute("processos", processos);
        model.addAttribute("processosPrevidenciarios", processoPrevidenciarioService.listarPorCliente(clienteId));
        return "juridico/lista-processos-cliente";
    }
}
