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
     * Atualizar status do chamado
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Chamado> atualizarStatus(
            @PathVariable Long id,
            @RequestParam Chamado.StatusChamado status,
            @RequestParam(required = false) String tecnicoResponsavel) {
        
        try {
            Chamado chamado = chamadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
            
            // Atualizar status baseado no valor recebido
            switch (status) {
                case EM_ANDAMENTO:
                    if (tecnicoResponsavel != null) {
                        chamado = chamadoService.iniciarAtendimento(id, tecnicoResponsavel);
                    }
                    break;
                case RESOLVIDO:
                    chamado = chamadoService.resolverChamado(id);
                    break;
                case FECHADO:
                    chamado = chamadoService.fecharChamado(id);
                    break;
                default:
                    throw new IllegalArgumentException("Status inválido: " + status);
            }
            
            return ResponseEntity.ok(chamado);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Avaliar chamado resolvido
     */
    @PostMapping("/{id}/avaliacao")
    public ResponseEntity<Chamado> avaliarChamado(
            @PathVariable Long id,
            @RequestParam Integer avaliacao,
            @RequestParam(required = false) String comentario) {
        
        try {
            Chamado chamado = chamadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
            
            if (chamado.getStatus() != Chamado.StatusChamado.RESOLVIDO && chamado.getStatus() != Chamado.StatusChamado.FECHADO) {
                return ResponseEntity.badRequest().build();
            }
            
            if (avaliacao < 1 || avaliacao > 5) {
                return ResponseEntity.badRequest().build();
            }
            
            chamado.setAvaliacao(avaliacao);
            chamado.setComentarioAvaliacao(comentario);
            
            Chamado chamadoAtualizado = chamadoService.atualizarChamado(chamado);
            return ResponseEntity.ok(chamadoAtualizado);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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