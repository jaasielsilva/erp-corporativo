package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.juridico.ProcessoJuridicoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.service.ProcessoPrevidenciarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/juridico/processos")
@RequiredArgsConstructor
public class ProcessoJuridicoController {

    private final ProcessoJuridicoService processoService;
    private final ProcessoPrevidenciarioService processoPrevidenciarioService;
    private final ClienteService clienteService;

    @GetMapping("/novo")
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
    public String salvarAndamento(@RequestParam Long processoId, 
                                  @RequestParam String titulo,
                                  @RequestParam String descricao,
                                  @RequestParam(defaultValue = "ANDAMENTO") String tipoEtapa,
                                  RedirectAttributes redirectAttributes) {
        try {
            processoService.adicionarAndamento(processoId, titulo, descricao, tipoEtapa);
            redirectAttributes.addFlashAttribute("mensagem", "Andamento registrado!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar andamento.");
        }
        return "redirect:/juridico/processos/" + processoId + "/detalhes";
    }

    @PostMapping("/prazos/salvar")
    public String salvarPrazo(@RequestParam Long processoId,
                              @RequestParam String dataLimite,
                              @RequestParam String descricao,
                              @RequestParam String responsabilidade,
                              RedirectAttributes redirectAttributes) {
        try {
            processoService.adicionarPrazo(processoId, dataLimite, descricao, responsabilidade);
            redirectAttributes.addFlashAttribute("mensagem", "Prazo registrado!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar prazo.");
        }
        return "redirect:/juridico/processos/" + processoId + "/detalhes";
    }

    @PostMapping("/prazos/concluir")
    public String concluirPrazo(@RequestParam Long prazoId, @RequestParam Long processoId, RedirectAttributes redirectAttributes) {
        processoService.concluirPrazo(prazoId);
        redirectAttributes.addFlashAttribute("mensagem", "Prazo concluído!");
        return "redirect:/juridico/processos/" + processoId + "/detalhes";
    }

    @PostMapping("/audiencias/salvar")
    public String salvarAudiencia(@RequestParam Long processoId,
                                  @RequestParam String dataHora,
                                  @RequestParam String tipo,
                                  @RequestParam String observacoes,
                                  RedirectAttributes redirectAttributes) {
        try {
            processoService.adicionarAudiencia(processoId, dataHora, tipo, observacoes);
            redirectAttributes.addFlashAttribute("mensagem", "Audiência agendada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao agendar audiência.");
        }
        return "redirect:/juridico/processos/" + processoId + "/detalhes";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ProcessoJuridico processo, RedirectAttributes redirectAttributes) {
        try {
            processoService.salvar(processo);
            redirectAttributes.addFlashAttribute("mensagem", "Processo salvo com sucesso!");
            return "redirect:/juridico/processos/cliente/" + processo.getCliente().getId();
        } catch (Exception e) {
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
