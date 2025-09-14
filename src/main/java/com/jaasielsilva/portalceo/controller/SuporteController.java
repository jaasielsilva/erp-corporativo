package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.service.ChamadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/suporte")
public class SuporteController {

    private static final Logger logger = LoggerFactory.getLogger(SuporteController.class);

    @Autowired
    private ChamadoService chamadoService;

    // Dashboard principal do suporte
    @GetMapping
    public String suporte(Model model) {
        try {
            logger.info("Carregando dashboard de suporte");
            
            // Estatísticas gerais
            long chamadosAbertos = chamadoService.contarPorStatus(StatusChamado.ABERTO);
            long chamadosEmAndamento = chamadoService.contarPorStatus(StatusChamado.EM_ANDAMENTO);
            long chamadosResolvidos = chamadoService.contarPorStatus(StatusChamado.RESOLVIDO);
            
            // SLA médio
            Double slaMedio = chamadoService.calcularSlaMedio();
            
            // Últimos chamados para tabela
            List<Chamado> ultimosChamados = chamadoService.listarUltimosChamados(10);
            
            // Estatísticas por prioridade para gráfico
            List<Object[]> estatisticasPrioridade = chamadoService.getEstatisticasPorPrioridade();
            
            // Estatísticas por status para gráfico
            List<Object[]> estatisticasStatus = chamadoService.getEstatisticasPorStatus();
            
            // Chamados com SLA crítico
            List<Chamado> chamadosSlaVencido = chamadoService.buscarChamadosComSlaVencido();
            List<Chamado> chamadosSlaProximo = chamadoService.buscarChamadosComSlaProximoVencimento();
            
            // Adicionar dados ao model
            model.addAttribute("chamadosAbertos", chamadosAbertos);
            model.addAttribute("chamadosEmAndamento", chamadosEmAndamento);
            model.addAttribute("chamadosResolvidos", chamadosResolvidos);
            model.addAttribute("slaMedio", slaMedio != null ? Math.round(slaMedio * 100.0) / 100.0 : 0.0);
            model.addAttribute("ultimosChamados", ultimosChamados);
            model.addAttribute("estatisticasPrioridade", estatisticasPrioridade);
            model.addAttribute("estatisticasStatus", estatisticasStatus);
            model.addAttribute("chamadosSlaVencido", chamadosSlaVencido.size());
            model.addAttribute("chamadosSlaProximo", chamadosSlaProximo.size());
            
            logger.info("Dashboard carregado: {} abertos, {} em andamento, {} resolvidos, SLA médio: {}h", 
                       chamadosAbertos, chamadosEmAndamento, chamadosResolvidos, slaMedio);
            
        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard de suporte: {}", e.getMessage(), e);
            
            // Valores padrão em caso de erro
            model.addAttribute("chamadosAbertos", 0L);
            model.addAttribute("chamadosEmAndamento", 0L);
            model.addAttribute("chamadosResolvidos", 0L);
            model.addAttribute("slaMedio", 0.0);
            model.addAttribute("ultimosChamados", List.of());
            model.addAttribute("estatisticasPrioridade", List.of());
            model.addAttribute("estatisticasStatus", List.of());
            model.addAttribute("chamadosSlaVencido", 0);
            model.addAttribute("chamadosSlaProximo", 0);
        }
        
        return "suporte/index";
    }
    
    // API para dados do gráfico de prioridades
    @GetMapping("/api/prioridades")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDadosPrioridades() {
        try {
            List<Object[]> estatisticas = chamadoService.getEstatisticasPorPrioridade();
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", estatisticas.stream().map(e -> ((Prioridade) e[0]).getDescricao()).toList());
            response.put("data", estatisticas.stream().map(e -> e[1]).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao buscar dados de prioridades: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // API para dados do gráfico de status
    @GetMapping("/api/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDadosStatus() {
        try {
            List<Object[]> estatisticas = chamadoService.getEstatisticasPorStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", estatisticas.stream().map(e -> ((StatusChamado) e[0]).getDescricao()).toList());
            response.put("data", estatisticas.stream().map(e -> e[1]).toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao buscar dados de status: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Listar todos os chamados
    @GetMapping("/chamados")
    public String listarChamados(Model model) {
        try {
            List<Chamado> chamados = chamadoService.listarTodos();
            model.addAttribute("chamados", chamados);
        } catch (Exception e) {
            logger.error("Erro ao listar chamados: {}", e.getMessage());
            model.addAttribute("chamados", List.of());
        }
        
        return "suporte/chamados";
    }
    
    // Visualizar chamado específico
    @GetMapping("/chamados/{id}")
    public String visualizarChamado(@PathVariable Long id, Model model) {
        try {
            Chamado chamado = chamadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
            
            model.addAttribute("chamado", chamado);
        } catch (Exception e) {
            logger.error("Erro ao visualizar chamado {}: {}", id, e.getMessage());
            return "redirect:/suporte";
        }
        
        return "suporte/visualizar";
    }
    
    // Formulário para novo chamado
    @GetMapping("/novo")
    public String novoChamado(Model model) {
        try {
            logger.info("Iniciando carregamento da página novo chamado");
            
            // Gerar próximo número do chamado
            String proximoNumero = chamadoService.gerarProximoNumero();
            model.addAttribute("proximoNumero", proximoNumero);
            logger.info("Próximo número gerado: {}", proximoNumero);
            
            model.addAttribute("prioridades", Prioridade.values());
            logger.info("Prioridades adicionadas ao model: {}", Prioridade.values().length);
            
            logger.info("Retornando template suporte/novo");
            return "suporte/novo";
            
        } catch (Exception e) {
            logger.error("Erro ao carregar página novo chamado: ", e);
            throw e;
        }
    }
    
    // Página de teste para novo chamado
    @GetMapping("/teste-chamado")
    public String testeChamado(Model model) {
        model.addAttribute("proximoNumero", chamadoService.gerarProximoNumero());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("chamado", new Chamado());
        return "suporte/teste-chamado";
    }
    
    @GetMapping("/teste-simples")
    public String testeSimples(Model model) {
        model.addAttribute("proximoNumero", chamadoService.gerarProximoNumero());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("chamado", new Chamado());
        return "suporte/teste-simples";
    }
    
    // Criar novo chamado
    @PostMapping("/novo")
    public String criarChamado(@ModelAttribute Chamado chamado) {
        try {
            Chamado chamadoCriado = chamadoService.criarChamado(chamado);
            logger.info("Novo chamado criado: {}", chamadoCriado.getNumero());
            return "redirect:/suporte/chamados/" + chamadoCriado.getId();
        } catch (Exception e) {
            logger.error("Erro ao criar chamado: {}", e.getMessage());
            return "redirect:/suporte/novo?erro=true";
        }
    }
    
    // Atualizar status do chamado
    @PostMapping("/chamados/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atualizarStatus(
            @PathVariable Long id, 
            @RequestParam String acao,
            @RequestParam(required = false) String tecnico) {
        
        try {
            Chamado chamado;
            
            switch (acao.toLowerCase()) {
                case "iniciar":
                    chamado = chamadoService.iniciarAtendimento(id, tecnico != null ? tecnico : "Sistema");
                    break;
                case "resolver":
                    chamado = chamadoService.resolverChamado(id);
                    break;
                case "fechar":
                    chamado = chamadoService.fecharChamado(id);
                    break;
                default:
                    throw new IllegalArgumentException("Ação inválida: " + acao);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("status", chamado.getStatus().getDescricao());
            response.put("slaRestante", chamado.getSlaRestante());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao atualizar status do chamado {}: {}", id, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Página de debug para novo chamado
    @GetMapping("/debug-novo")
    public String debugNovo(Model model) {
        model.addAttribute("proximoNumero", chamadoService.gerarProximoNumero());
        return "suporte/debug-novo";
    }
    

}