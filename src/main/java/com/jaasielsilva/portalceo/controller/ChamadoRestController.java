package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.service.ChamadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller para o módulo de suporte - Chamados
 * Fornece APIs para gerenciamento de chamados
 */
@RestController
@RequestMapping("/api/chamados")
public class ChamadoRestController {

    @Autowired
    private ChamadoService chamadoService;

    /**
     * Lista todos os chamados
     */
    @GetMapping
    public ResponseEntity<List<Chamado>> listarTodos() {
        try {
            List<Chamado> chamados = chamadoService.listarTodos();
            return ResponseEntity.ok(chamados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Busca chamado por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Chamado> buscarPorId(@PathVariable Long id) {
        try {
            Optional<Chamado> chamado = chamadoService.buscarPorId(id);
            return chamado.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Busca chamado por número
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<Chamado> buscarPorNumero(@PathVariable String numero) {
        try {
            Optional<Chamado> chamado = chamadoService.buscarPorNumero(numero);
            return chamado.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista chamados abertos
     */
    @GetMapping("/abertos")
    public ResponseEntity<List<Chamado>> listarAbertos() {
        try {
            List<Chamado> chamados = chamadoService.listarAbertos();
            return ResponseEntity.ok(chamados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista chamados em andamento
     */
    @GetMapping("/em-andamento")
    public ResponseEntity<List<Chamado>> listarEmAndamento() {
        try {
            List<Chamado> chamados = chamadoService.listarEmAndamento();
            return ResponseEntity.ok(chamados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista chamados resolvidos
     */
    @GetMapping("/resolvidos")
    public ResponseEntity<List<Chamado>> listarResolvidos() {
        try {
            List<Chamado> chamados = chamadoService.listarResolvidos();
            return ResponseEntity.ok(chamados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cria novo chamado
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> criarChamado(@RequestBody Chamado chamado) {
        Map<String, Object> response = new HashMap<>();
        try {
            Chamado chamadoCriado = chamadoService.criarChamado(chamado);
            response.put("sucesso", true);
            response.put("chamado", chamadoCriado);
            response.put("mensagem", "Chamado criado com sucesso!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Atualiza status do chamado
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String tecnicoResponsavel) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Chamado> chamadoOpt = chamadoService.buscarPorId(id);
            if (chamadoOpt.isEmpty()) {
                response.put("sucesso", false);
                response.put("erro", "Chamado não encontrado");
                return ResponseEntity.notFound().build();
            }

            Chamado chamado = chamadoOpt.get();
            
            // Atualizar status baseado no parâmetro
            switch (status.toUpperCase()) {
                case "ABERTO":
                    chamado.setStatus(Chamado.StatusChamado.ABERTO);
                    break;
                case "EM_ANDAMENTO":
                    chamado.setStatus(Chamado.StatusChamado.EM_ANDAMENTO);
                    if (tecnicoResponsavel != null) {
                        chamado.setTecnicoResponsavel(tecnicoResponsavel);
                    }
                    break;
                case "RESOLVIDO":
                    chamado.setStatus(Chamado.StatusChamado.RESOLVIDO);
                    break;
                case "FECHADO":
                    chamado.setStatus(Chamado.StatusChamado.FECHADO);
                    break;
                default:
                    response.put("sucesso", false);
                    response.put("erro", "Status inválido");
                    return ResponseEntity.badRequest().body(response);
            }

            Chamado chamadoAtualizado = chamadoService.atualizarChamado(chamado);
            response.put("sucesso", true);
            response.put("chamado", chamadoAtualizado);
            response.put("mensagem", "Status atualizado com sucesso!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Estatísticas dos chamados
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Chamado> todosChamados = chamadoService.listarTodos();
            List<Chamado> abertos = chamadoService.listarAbertos();
            List<Chamado> emAndamento = chamadoService.listarEmAndamento();
            List<Chamado> resolvidos = chamadoService.listarResolvidos();

            response.put("total", todosChamados.size());
            response.put("abertos", abertos.size());
            response.put("emAndamento", emAndamento.size());
            response.put("resolvidos", resolvidos.size());
            response.put("sucesso", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}