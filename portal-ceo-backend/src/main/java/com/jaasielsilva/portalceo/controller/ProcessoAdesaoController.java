package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.HistoricoProcessoAdesao;
import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.service.ProcessoAdesaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rh/processos-adesao")
public class ProcessoAdesaoController {
    
    @Autowired
    private ProcessoAdesaoService processoService;
    
    /**
     * Lista processos aguardando aprovação
     */
    @GetMapping("/aguardando-aprovacao")
    public ResponseEntity<List<ProcessoAdesao>> listarProcessosAguardandoAprovacao() {
        try {
            List<ProcessoAdesao> processos = processoService.listarProcessosAguardandoAprovacao();
            return ResponseEntity.ok(processos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lista processos por status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProcessoAdesao>> listarProcessosPorStatus(
            @PathVariable String status) {
        try {
            ProcessoAdesao.StatusProcesso statusEnum = ProcessoAdesao.StatusProcesso.valueOf(status.toUpperCase());
            List<ProcessoAdesao> processos = processoService.listarProcessosPorStatus(statusEnum);
            return ResponseEntity.ok(processos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Busca processo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProcessoAdesao> buscarProcessoPorId(@PathVariable Long id) {
        try {
            ProcessoAdesao processo = processoService.buscarPorId(id);
            return ResponseEntity.ok(processo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Busca processo por sessionId
     */
    @GetMapping("/sessao/{sessionId}")
    public ResponseEntity<ProcessoAdesao> buscarProcessoPorSessionId(@PathVariable String sessionId) {
        try {
            ProcessoAdesao processo = processoService.buscarPorSessionId(sessionId);
            return ResponseEntity.ok(processo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Aprova processo
     */
    @PostMapping("/{id}/aprovar")
    public ResponseEntity<Map<String, Object>> aprovarProcesso(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados) {
        try {
            String aprovadoPor = dados.get("aprovadoPor");
            String observacoes = dados.get("observacoes");
            
            if (aprovadoPor == null || aprovadoPor.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Campo 'aprovadoPor' é obrigatório"
                ));
            }
            
            ProcessoAdesao processo = processoService.aprovarProcesso(id, aprovadoPor, observacoes);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Processo aprovado com sucesso",
                "processo", processo
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Erro interno do servidor"
            ));
        }
    }
    
    /**
     * Rejeita processo
     */
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<Map<String, Object>> rejeitarProcesso(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados) {
        try {
            String rejeitadoPor = dados.get("rejeitadoPor");
            String motivoRejeicao = dados.get("motivoRejeicao");
            
            if (rejeitadoPor == null || rejeitadoPor.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Campo 'rejeitadoPor' é obrigatório"
                ));
            }
            
            if (motivoRejeicao == null || motivoRejeicao.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Campo 'motivoRejeicao' é obrigatório"
                ));
            }
            
            ProcessoAdesao processo = processoService.rejeitarProcesso(id, rejeitadoPor, motivoRejeicao);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Processo rejeitado com sucesso",
                "processo", processo
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Erro interno do servidor"
            ));
        }
    }
    
    /**
     * Busca histórico de um processo
     */
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoProcessoAdesao>> buscarHistoricoProcesso(@PathVariable Long id) {
        try {
            List<HistoricoProcessoAdesao> historico = processoService.buscarHistoricoProcesso(id);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Busca histórico por sessionId
     */
    @GetMapping("/sessao/{sessionId}/historico")
    public ResponseEntity<List<HistoricoProcessoAdesao>> buscarHistoricoPorSessionId(@PathVariable String sessionId) {
        try {
            List<HistoricoProcessoAdesao> historico = processoService.buscarHistoricoPorSessionId(sessionId);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtém dados completos para revisão
     */
    @GetMapping("/sessao/{sessionId}/revisao")
    public ResponseEntity<Map<String, Object>> obterDadosRevisao(@PathVariable String sessionId) {
        try {
            Map<String, Object> dadosRevisao = processoService.obterDadosRevisao(sessionId);
            return ResponseEntity.ok(dadosRevisao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtém estatísticas de processos
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        try {
            Map<String, Object> estatisticas = processoService.obterEstatisticas();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cancela processo por sessionId
     */
    @PostMapping("/sessao/{sessionId}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelarProcesso(
            @PathVariable String sessionId,
            @RequestBody(required = false) Map<String, String> dados) {
        try {
            String motivo = dados != null ? dados.get("motivo") : "Cancelado pelo usuário";
            
            ProcessoAdesao processo = processoService.cancelarProcesso(sessionId, motivo);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Processo cancelado com sucesso",
                "processo", processo
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Erro interno do servidor"
            ));
        }
    }
    
    /**
     * Finaliza processo e envia para aprovação
     */
    @PostMapping("/sessao/{sessionId}/finalizar")
    public ResponseEntity<Map<String, Object>> finalizarProcesso(
            @PathVariable String sessionId,
            @RequestBody(required = false) Map<String, String> dados) {
        try {
            String observacoes = dados != null ? dados.get("observacoes") : null;
            
            ProcessoAdesao processo = processoService.finalizarProcesso(sessionId, observacoes);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Processo finalizado e enviado para aprovação",
                "processo", processo
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Erro interno do servidor"
            ));
        }
    }
}