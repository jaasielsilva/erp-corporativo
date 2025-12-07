package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import com.jaasielsilva.portalceo.service.rh.WorkflowAdesaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jaasielsilva.portalceo.service.AuditoriaRhLogService;

@Controller
@RequestMapping("/rh/workflow")
public class WorkflowAdesaoController {
    
    @Autowired
    private WorkflowAdesaoService workflowService;
    @Autowired
    private AuditoriaRhLogService auditoriaRhLogService;
    
    /**
     * Página principal do workflow de aprovação
     */
    @GetMapping("/aprovacao")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public String paginaAprovacao(Model model) {
        try {
            // Carregar estatísticas para o dashboard
            WorkflowAdesaoService.DashboardEstatisticas stats = workflowService.obterEstatisticas();
            model.addAttribute("estatisticas", stats);
            
            // Carregar processos aguardando aprovação
            List<WorkflowAdesaoService.ProcessoAdesaoInfo> processosAprovacao = 
                workflowService.listarProcessosAguardandoAprovacao();
            model.addAttribute("processosAprovacao", processosAprovacao);
            
            return "rh/workflow-aprovacao";
            
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar dados: " + e.getMessage());
            return "rh/workflow-aprovacao";
        }
    }
    
    /**
     * API: Lista processos por status com paginação e busca
     */
    @GetMapping("/api/processos")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> listarProcessos(
            @RequestParam(defaultValue = "AGUARDANDO_APROVACAO") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        try {
            ProcessoAdesao.StatusProcesso statusEnum = ProcessoAdesao.StatusProcesso.valueOf(status);
            Page<WorkflowAdesaoService.ProcessoAdesaoInfo> processos;
            
            if (search != null && !search.trim().isEmpty()) {
                // Busca com filtro
                processos = workflowService.buscarProcessosComFiltro(statusEnum, search.trim(), page, size);
            } else {
                // Busca normal por status
                processos = workflowService.listarProcessosPorStatus(statusEnum, page, size);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("processos", processos.getContent());
            response.put("totalElements", processos.getTotalElements());
            response.put("totalPages", processos.getTotalPages());
            response.put("currentPage", processos.getNumber());
            response.put("hasNext", processos.hasNext());
            response.put("hasPrevious", processos.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao listar processos: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Busca detalhes de um processo
     */
    @GetMapping("/api/processo/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> buscarProcesso(@PathVariable Long id) {
        try {
            // Validação de ID
            if (id == null || id <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "ID do processo inválido");
                return ResponseEntity.badRequest().body(error);
            }
            
            WorkflowAdesaoService.ProcessoAdesaoInfo processo = workflowService.buscarProcessoPorId(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("processo", processo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao buscar processo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Busca histórico de um processo
     */
    @GetMapping("/api/processo/{id}/historico")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> buscarHistoricoProcesso(@PathVariable Long id) {
        try {
            // Validação de ID
            if (id == null || id <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "ID do processo inválido");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<WorkflowAdesaoService.HistoricoEventoInfo> historico = 
                workflowService.buscarHistoricoProcesso(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("historico", historico);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao buscar histórico: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Aprova um processo
     */
    @PostMapping("/api/processo/{id}/aprovar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH')")
    public ResponseEntity<Map<String, Object>> aprovarProcesso(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados) {
        
        try {
            // Validação de ID
            if (id == null || id <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "ID do processo inválido");
                return ResponseEntity.badRequest().body(error);
            }
            
            String aprovadoPor = dados.get("aprovadoPor");
            String observacoes = dados.get("observacoes");
            
            // Validação do aprovador
            if (aprovadoPor == null || aprovadoPor.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Nome do aprovador é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validação de tamanho e caracteres
            if (aprovadoPor.trim().length() > 100) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Nome do aprovador muito longo (máximo 100 caracteres)");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (observacoes != null && observacoes.length() > 500) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Observações muito longas (máximo 500 caracteres)");
                return ResponseEntity.badRequest().body(error);
            }
            
            workflowService.aprovarProcesso(id, aprovadoPor, observacoes);
            try {
                auditoriaRhLogService.registrar(
                        "ALTERACAO",
                        "APROVAR_PROCESSO",
                        "/rh/workflow/api/processo/" + id + "/aprovar",
                        aprovadoPor,
                        null,
                        observacoes,
                        true);
            } catch (Exception ignore) {}
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Processo aprovado com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao aprovar processo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Rejeita um processo
     */
    @PostMapping("/api/processo/{id}/rejeitar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH')")
    public ResponseEntity<Map<String, Object>> rejeitarProcesso(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados) {
        
        try {
            // Validação de ID
            if (id == null || id <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "ID do processo inválido");
                return ResponseEntity.badRequest().body(error);
            }
            
            String motivoRejeicao = dados.get("motivoRejeicao");
            String usuarioResponsavel = dados.get("usuarioResponsavel");
            
            // Validação do motivo
            if (motivoRejeicao == null || motivoRejeicao.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Motivo da rejeição é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (motivoRejeicao.trim().length() < 10) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Motivo da rejeição deve ter pelo menos 10 caracteres");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (motivoRejeicao.length() > 500) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Motivo da rejeição muito longo (máximo 500 caracteres)");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validação do usuário responsável
            if (usuarioResponsavel == null || usuarioResponsavel.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Usuário responsável é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (usuarioResponsavel.trim().length() > 100) {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Nome do usuário responsável muito longo (máximo 100 caracteres)");
                return ResponseEntity.badRequest().body(error);
            }
            
            workflowService.rejeitarProcesso(id, motivoRejeicao, usuarioResponsavel);
            try {
                auditoriaRhLogService.registrar(
                        "ALTERACAO",
                        "REJEITAR_PROCESSO",
                        "/rh/workflow/api/processo/" + id + "/rejeitar",
                        usuarioResponsavel,
                        null,
                        motivoRejeicao,
                        true);
            } catch (Exception ignore) {}
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Processo rejeitado com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao rejeitar processo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Obter estatísticas do dashboard
     */
    @GetMapping("/api/estatisticas")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        try {
            WorkflowAdesaoService.DashboardEstatisticas stats = workflowService.obterEstatisticas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("estatisticas", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Página de detalhes de um processo
     */
    @GetMapping("/processo/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public String detalheProcesso(@PathVariable Long id, Model model) {
        try {
            // Validação de ID
            if (id == null || id <= 0) {
                model.addAttribute("erro", "ID do processo inválido");
                return "rh/workflow-aprovacao";
            }
            
            WorkflowAdesaoService.ProcessoAdesaoInfo processo = workflowService.buscarProcessoPorId(id);
            List<WorkflowAdesaoService.HistoricoEventoInfo> historico = 
                workflowService.buscarHistoricoProcesso(id);
            
            model.addAttribute("processo", processo);
            model.addAttribute("historico", historico);
            
            return "rh/processo-detalhes";
            
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar processo: " + e.getMessage());
            return "rh/processo-detalhes";
        }
    }
    
    /**
     * Página de relatórios
     */
    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public String paginaRelatorios(Model model) {
        try {
            // Carregar estatísticas
            WorkflowAdesaoService.DashboardEstatisticas stats = workflowService.obterEstatisticas();
            model.addAttribute("estatisticas", stats);
            
            return "rh/workflow-relatorios";
            
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao carregar relatórios: " + e.getMessage());
            return "rh/workflow-relatorios";
        }
    }
    
    /**
     * API: Buscar processos para relatórios
     */
    @GetMapping("/api/relatorios/processos")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> buscarProcessosRelatorio(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            // Por enquanto, usar o método existente
            // Futuramente pode ser expandido para filtros mais específicos
            ProcessoAdesao.StatusProcesso statusEnum = null;
            if (status != null && !status.isEmpty()) {
                statusEnum = ProcessoAdesao.StatusProcesso.valueOf(status);
            } else {
                statusEnum = ProcessoAdesao.StatusProcesso.APROVADO; // Default para relatórios
            }
            
            Page<WorkflowAdesaoService.ProcessoAdesaoInfo> processos = 
                workflowService.listarProcessosPorStatus(statusEnum, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("processos", processos.getContent());
            response.put("totalElements", processos.getTotalElements());
            response.put("totalPages", processos.getTotalPages());
            response.put("currentPage", processos.getNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao buscar processos para relatório: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Métricas para relatórios
     */
    @GetMapping("/api/relatorios/metricas")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> obterMetricasRelatorio(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String periodo) {
        
        try {
            WorkflowAdesaoService.DashboardEstatisticas stats = workflowService.obterEstatisticas();
            
            // Calcular total de processos somando todos os status
            long totalProcessos = stats.getProcessosPorStatus().values().stream()
                .mapToLong(Long::longValue).sum();
            
            // Obter valores específicos do mapa de status
            long totalAprovados = stats.getProcessosPorStatus().getOrDefault("APROVADO", 0L);
            
            Map<String, Object> metricas = new HashMap<>();
            metricas.put("totalProcessos", totalProcessos);
            metricas.put("totalAprovados", totalAprovados);
            metricas.put("processosHoje", stats.getProcessosHoje());
            metricas.put("aguardandoAprovacao", stats.getProcessosAguardandoAprovacao());
            metricas.put("tempoMedioAprovacao", 3.5); // Valor fixo por enquanto
            metricas.put("custoMedioMensal", 2500.00); // Valor fixo por enquanto
            
            return ResponseEntity.ok(metricas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao obter métricas: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * API: Gráficos para relatórios
     */
    @GetMapping("/api/relatorios/graficos")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MASTER', 'ROLE_RH', 'ROLE_GERENCIAL')")
    public ResponseEntity<Map<String, Object>> obterGraficosRelatorio(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String periodo) {
        
        try {
            WorkflowAdesaoService.DashboardEstatisticas stats = workflowService.obterEstatisticas();
            
            Map<String, Object> graficos = new HashMap<>();
            
            // Gráfico de status dos processos usando dados reais
            Map<String, Long> statusMap = stats.getProcessosPorStatus();
            Map<String, Object> statusProcessos = new HashMap<>();
            statusProcessos.put("labels", List.of("Aguardando", "Aprovados", "Rejeitados", "Em Andamento"));
            statusProcessos.put("data", List.of(
                statusMap.getOrDefault("AGUARDANDO_APROVACAO", 0L),
                statusMap.getOrDefault("APROVADO", 0L),
                statusMap.getOrDefault("REJEITADO", 0L),
                statusMap.getOrDefault("EM_ANDAMENTO", 0L)
            ));
            graficos.put("statusProcessos", statusProcessos);
            
            // Gráfico de evolução mensal (dados fictícios)
            Map<String, Object> evolucaoMensal = new HashMap<>();
            evolucaoMensal.put("labels", List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun"));
            evolucaoMensal.put("data", List.of(12, 19, 15, 25, 22, 30));
            graficos.put("evolucaoMensal", evolucaoMensal);
            
            // Gráfico de tempo de aprovação (dados fictícios)
            Map<String, Object> tempoAprovacao = new HashMap<>();
            tempoAprovacao.put("labels", List.of("0-1 dia", "1-3 dias", "3-7 dias", "+7 dias"));
            tempoAprovacao.put("data", List.of(45, 30, 20, 5));
            graficos.put("tempoAprovacao", tempoAprovacao);
            
            // Gráfico de custos por departamento (dados fictícios)
            Map<String, Object> custosDepartamento = new HashMap<>();
            custosDepartamento.put("labels", List.of("TI", "Vendas", "Marketing", "RH", "Financeiro"));
            custosDepartamento.put("data", List.of(15000, 25000, 18000, 12000, 20000));
            graficos.put("custosDepartamento", custosDepartamento);
            
            return ResponseEntity.ok(graficos);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao obter gráficos: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
