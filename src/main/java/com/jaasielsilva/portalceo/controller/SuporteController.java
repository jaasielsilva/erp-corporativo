package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChamadoService;
import com.jaasielsilva.portalceo.service.BacklogChamadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/suporte")
public class SuporteController {

    private static final Logger logger = LoggerFactory.getLogger(SuporteController.class);

    @Autowired
    private ChamadoService chamadoService;
    
    @Autowired
    private BacklogChamadoService backlogChamadoService;

    // Dashboard principal do suporte
    @GetMapping
    public String suporte(Model model) {
        try {
            logger.info("Carregando dashboard de suporte");
            
            // Mock de usuário logado para o topbar
            Usuario usuarioLogado = new Usuario();
            usuarioLogado.setId(1L);
            usuarioLogado.setNome("Administrador");
            model.addAttribute("usuarioLogado", usuarioLogado);
            
            // Estatísticas gerais
            long chamadosAbertos = chamadoService.contarPorStatus(StatusChamado.ABERTO);
            long chamadosEmAndamento = chamadoService.contarPorStatus(StatusChamado.EM_ANDAMENTO);
            long chamadosResolvidos = chamadoService.contarPorStatus(StatusChamado.RESOLVIDO);
            
            // SLA médio
            Double slaMedio = chamadoService.calcularSlaMedio();
            
            // Avaliação média
            Double avaliacaoMedia = chamadoService.calcularAvaliacaoMedia();
            
            // Últimos chamados para tabela
            List<Chamado> ultimosChamados = chamadoService.listarUltimosChamados(10);
            
            // Estatísticas por prioridade para gráfico
            List<Object[]> estatisticasPrioridade = chamadoService.getEstatisticasPorPrioridade();
            
            // Estatísticas por status para gráfico
            List<Object[]> estatisticasStatus = chamadoService.getEstatisticasPorStatus();
            
            // Estatísticas por categoria para gráfico
            List<Object[]> estatisticasCategoria = chamadoService.getEstatisticasPorCategoria();
            
            // Chamados com SLA crítico
            List<Chamado> chamadosSlaVencido = chamadoService.buscarChamadosComSlaVencido();
            List<Chamado> chamadosSlaProximo = chamadoService.buscarChamadosComSlaProximoVencimento();
            
            // Métricas de SLA - Taxa de Resolução no Prazo
            Map<String, Object> metricasSLA = chamadoService.calcularMetricasSLA();
            Double taxaResolucaoPrazo = (Double) metricasSLA.get("percentualSLACumprido");
            
            // Calcular tempo médio de primeira resposta
            Double tempoMedioPrimeiraResposta = chamadoService.calcularTempoMedioPrimeiraResposta();
            
            // Contar chamados reabertos
            Long chamadosReabertos = chamadoService.contarChamadosReabertos();
            
            // Métricas do Backlog de Chamados
            Long totalBacklog = backlogChamadoService.contarTotalBacklog();
            Map<String, Object> estatisticasBacklog = backlogChamadoService.obterEstatisticasBacklog();
            Map<String, Object> metricasPerformanceBacklog = backlogChamadoService.obterMetricasPerformance();
            
            // Dados para gráfico de evolução de chamados (últimos 12 meses)
            List<String> ultimos12MesesLabels = chamadoService.obterLabelsUltimosMeses(12);
            List<Long> ultimos12MesesChamados = chamadoService.obterEvolucaoChamadosUltimosMeses(12);
            List<Long> metaChamadosMensal = chamadoService.calcularMetaMensalChamados(12);
            
            // Adicionar dados ao model
            model.addAttribute("chamadosAbertos", chamadosAbertos);
            model.addAttribute("chamadosEmAndamento", chamadosEmAndamento);
            model.addAttribute("chamadosResolvidos", chamadosResolvidos);
            model.addAttribute("slaMedio", slaMedio != null ? Math.round(slaMedio * 100.0) / 100.0 : 0.0);
            model.addAttribute("avaliacaoMedia", avaliacaoMedia != null ? Math.round(avaliacaoMedia * 100.0) / 100.0 : 0.0);
            model.addAttribute("ultimosChamados", ultimosChamados);
            model.addAttribute("estatisticasPrioridade", estatisticasPrioridade);
            model.addAttribute("estatisticasStatus", estatisticasStatus);
            model.addAttribute("estatisticasCategoria", estatisticasCategoria);
            model.addAttribute("chamadosSlaVencido", chamadosSlaVencido.size());
            model.addAttribute("chamadosSlaProximo", chamadosSlaProximo.size());
            model.addAttribute("taxaResolucaoPrazo", taxaResolucaoPrazo != null ? taxaResolucaoPrazo : 0.0);
            model.addAttribute("tempoMedioPrimeiraResposta", tempoMedioPrimeiraResposta != null ? tempoMedioPrimeiraResposta : 0.0);
            model.addAttribute("chamadosReabertos", chamadosReabertos != null ? chamadosReabertos : 0L);
            
            // Métricas do Backlog
            model.addAttribute("totalBacklog", totalBacklog != null ? totalBacklog : 0L);
            model.addAttribute("estatisticasBacklog", estatisticasBacklog);
            model.addAttribute("metricasPerformanceBacklog", metricasPerformanceBacklog);
            
            // Dados para gráfico de evolução
            model.addAttribute("ultimos12MesesLabels", ultimos12MesesLabels);
            model.addAttribute("ultimos12MesesChamados", ultimos12MesesChamados);
            model.addAttribute("metaChamadosMensal", metaChamadosMensal);
            
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
            model.addAttribute("estatisticasCategoria", List.of());
            model.addAttribute("chamadosSlaVencido", 0);
            model.addAttribute("chamadosSlaProximo", 0);
            model.addAttribute("taxaResolucaoPrazo", 0.0);
            model.addAttribute("tempoMedioPrimeiraResposta", 0.0);
            model.addAttribute("chamadosReabertos", 0L);
            model.addAttribute("totalBacklog", 0L);
            model.addAttribute("estatisticasBacklog", new HashMap<>());
            model.addAttribute("metricasPerformanceBacklog", new HashMap<>());
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
    
    // API para dados do gráfico de evolução de chamados
    @GetMapping("/api/evolucao-chamados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDadosEvolucaoChamados() {
        try {
            List<String> labels = chamadoService.obterLabelsUltimosMeses(12);
            List<Long> chamados = chamadoService.obterEvolucaoChamadosUltimosMeses(12);
            List<Long> metas = chamadoService.calcularMetaMensalChamados(12);
            
            Map<String, Object> response = new HashMap<>();
            response.put("labels", labels);
            response.put("chamados", chamados);
            response.put("metas", metas);
            response.put("success", true);
            
            logger.info("Dados de evolução enviados: {} meses, {} chamados totais", 
                       labels.size(), chamados.stream().mapToLong(Long::longValue).sum());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao buscar dados de evolução de chamados: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * API para dados de tempo de resolução
     */
    @GetMapping("/api/tempo-resolucao")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTempoResolucao() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> labels = chamadoService.obterLabelsUltimosMeses(12);
            List<Double> temposResolucao = chamadoService.obterTempoMedioResolucaoUltimosMeses();
            List<Double> metasResolucao = chamadoService.calcularMetaTempoResolucao();
            
            response.put("success", true);
            response.put("labels", labels);
            response.put("temposResolucao", temposResolucao);
            response.put("metasResolucao", metasResolucao);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter dados de tempo de resolução: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * API para métricas de SLA e tempo de resolução por categoria/prioridade
     */
    @GetMapping("/api/metricas-resolucao")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricasResolucao() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Tempo médio geral
            Double tempoMedioGeral = chamadoService.calcularTempoMedioResolucaoGeral();
            
            // Tempo médio por prioridade
            Map<String, Double> temposPorPrioridade = chamadoService.calcularTempoMedioResolucaoPorPrioridade();
            
            // Tempo médio por categoria
            Map<String, Double> temposPorCategoria = chamadoService.calcularTempoMedioResolucaoPorCategoria();
            
            // Métricas de SLA
            Map<String, Object> metricasSLA = chamadoService.calcularMetricasSLA();
            
            response.put("success", true);
            response.put("tempoMedioGeral", tempoMedioGeral);
            response.put("temposPorPrioridade", temposPorPrioridade);
            response.put("temposPorCategoria", temposPorCategoria);
            response.put("metricasSLA", metricasSLA);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter métricas de resolução: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Listar todos os chamados
    @GetMapping("/chamados")
    public String listarChamados(Model model) {
        try {
            // Mock de usuário logado para o topbar
            Usuario usuarioLogado = new Usuario();
            usuarioLogado.setId(1L);
            usuarioLogado.setNome("Administrador");
            model.addAttribute("usuarioLogado", usuarioLogado);
            
            List<Chamado> chamados = chamadoService.listarTodos();
            model.addAttribute("chamados", chamados);
        } catch (Exception e) {
            logger.error("Erro ao listar chamados: {}", e.getMessage());
            model.addAttribute("chamados", List.of());
        }
        
        return "suporte/chamados";
    }
    
    // Formulário para novo chamado - DEVE VIR ANTES da rota com {id}
    @GetMapping("/chamados/novo")
    public String novoFormulario(Model model) {
        logger.info("Acessando formulário de novo chamado");
        
        try {
            // Gera o próximo número do chamado
            String proximoNumero = chamadoService.gerarProximoNumero();
            model.addAttribute("proximoNumero", proximoNumero);
            model.addAttribute("prioridades", Prioridade.values());
            model.addAttribute("chamado", new Chamado());
            
            // Adicionar usuário logado (mock para teste)
            Usuario usuarioMock = new Usuario();
            usuarioMock.setId(1L);
            usuarioMock.setNome("Usuário Teste");
            model.addAttribute("usuarioLogado", usuarioMock);
            
            logger.info("Próximo número de chamado gerado: {}", proximoNumero);
            
            return "suporte/novo";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de novo chamado: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar formulário: " + e.getMessage());
            return "error";
        }
    }
    
    // Visualizar chamado específico
    @GetMapping("/chamados/{id}")
    public String visualizarChamado(@PathVariable Long id, Model model) {
        try {
            // Mock de usuário logado para o topbar
            Usuario usuarioLogado = new Usuario();
            usuarioLogado.setId(1L);
            usuarioLogado.setNome("Administrador");
            model.addAttribute("usuarioLogado", usuarioLogado);
            
            Chamado chamado = chamadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
            
            model.addAttribute("chamado", chamado);
            logger.info("Visualizando chamado {}: {}", id, chamado.getNumero());
        } catch (Exception e) {
            logger.error("Erro ao visualizar chamado {}: {}", id, e.getMessage());
            return "redirect:/suporte";
        }
        
        return "suporte/visualizar";
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
    
    @GetMapping("/novo-simples")
    public String novoSimples(Model model) {
        try {
            logger.info("Testando template simplificado");
            String proximoNumero = chamadoService.gerarProximoNumero();
            model.addAttribute("proximoNumero", proximoNumero);
            logger.info("Template simplificado - Próximo número: {}", proximoNumero);
            return "suporte/novo-simples";
        } catch (Exception e) {
            logger.error("Erro no template simplificado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // Criar novo chamado
    @PostMapping("/chamados/novo")
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
    
    // Endpoint de teste para debug do template
    @GetMapping("/chamados/{id}/debug")
    public String debugChamado(@PathVariable Long id, Model model) {
        try {
            logger.info("=== DEBUG CHAMADO {} ===", id);
            
            Chamado chamado = chamadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
            
            model.addAttribute("chamado", chamado);
            logger.info("Chamado carregado para debug: {}", chamado.getNumero());
            
            return "suporte/teste-debug";
        } catch (Exception e) {
            logger.error("Erro no debug do chamado {}: {}", id, e.getMessage(), e);
            model.addAttribute("chamado", null);
            return "suporte/teste-debug";
        }
    }
    
    // Página de debug para novo chamado
    @GetMapping("/debug-novo")
    public String debugNovo(Model model) {
        try {
            logger.info("=== DEBUG NOVO CHAMADO ===");
            
            // Teste 1: Gerar número
            String proximoNumero = chamadoService.gerarProximoNumero();
            logger.info("Próximo número gerado: {}", proximoNumero);
            model.addAttribute("proximoNumero", proximoNumero);
            
            // Teste 2: Verificar se o service está funcionando
            logger.info("ChamadoService injetado: {}", chamadoService != null);
            
            // Teste 3: Adicionar dados básicos
            model.addAttribute("teste", "Debug funcionando");
            
            logger.info("=== FIM DEBUG ===");
            return "suporte/debug-novo";
            
        } catch (Exception e) {
            logger.error("Erro no debug: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "suporte/debug-novo";
        }
    }
    
    // Página para avaliar chamado
    @GetMapping("/chamados/{id}/avaliar")
    public String avaliarChamado(@PathVariable Long id, Model model) {
        try {
            // Buscar chamado por ID
            Optional<Chamado> chamadoOpt = chamadoService.buscarPorId(id);
            if (!chamadoOpt.isPresent()) {
                logger.warn("Chamado não encontrado com ID: {}", id);
                return "redirect:/suporte/chamados?erro=nao_encontrado";
            }
            Chamado chamado = chamadoOpt.get();
            
            // Verificar se o chamado pode ser avaliado (deve estar resolvido ou fechado)
            if (chamado.getStatus() != StatusChamado.RESOLVIDO && chamado.getStatus() != StatusChamado.FECHADO) {
                logger.warn("Tentativa de avaliar chamado {} com status inválido: {}", id, chamado.getStatus());
                return "redirect:/suporte/chamados?erro=status_invalido";
            }
            
            // Verificar se já foi avaliado
            if (chamado.getAvaliacao() != null) {
                logger.warn("Tentativa de avaliar chamado {} que já foi avaliado", id);
                return "redirect:/suporte/chamados?erro=ja_avaliado";
            }
            
            model.addAttribute("chamado", chamado);
            logger.info("Carregando página de avaliação para chamado {}", id);
            return "suporte/avaliar";
            
        } catch (Exception e) {
            logger.error("Erro ao carregar página de avaliação para chamado {}: {}", id, e.getMessage());
            return "redirect:/suporte/chamados?erro=sistema";
        }
    }
    
    // Endpoint para dados de avaliações de atendimento
    @GetMapping("/api/avaliacoes-atendimento")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDadosAvaliacoes() {
        try {
            List<Chamado> chamadosAvaliados = chamadoService.buscarChamadosAvaliados();
            
            Map<String, Object> response = new HashMap<>();
            
            // Calcular métricas
            double notaMedia = chamadosAvaliados.stream()
                .mapToInt(Chamado::getAvaliacao)
                .average()
                .orElse(0.0);
            
            long totalAvaliacoes = chamadosAvaliados.size();
            long avaliacoesPositivas = chamadosAvaliados.stream()
                .mapToInt(Chamado::getAvaliacao)
                .filter(nota -> nota >= 4)
                .count();
            
            double taxaSatisfacao = totalAvaliacoes > 0 ? 
                (avaliacoesPositivas * 100.0) / totalAvaliacoes : 0.0;
            
            // Distribuição por nota
            Map<String, Long> distribuicao = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                final int nota = i;
                long count = chamadosAvaliados.stream()
                    .mapToInt(Chamado::getAvaliacao)
                    .filter(n -> n == nota)
                    .count();
                distribuicao.put(String.valueOf(i), count);
            }
            
            response.put("notaMedia", Math.round(notaMedia * 10.0) / 10.0);
            response.put("totalAvaliacoes", totalAvaliacoes);
            response.put("taxaSatisfacao", Math.round(taxaSatisfacao * 10.0) / 10.0);
            response.put("distribuicao", distribuicao);
            response.put("sucesso", true);
            
            logger.info("Dados de avaliações carregados: {} avaliações, nota média: {}", 
                totalAvaliacoes, notaMedia);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao carregar dados de avaliações: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    

    // Teste de busca de chamado sem template
    @GetMapping("/chamados/{id}/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarChamadoJson(@PathVariable Long id) {
        try {
            Optional<Chamado> chamadoOpt = chamadoService.buscarPorId(id);
            Map<String, Object> response = new HashMap<>();
            
            if (chamadoOpt.isPresent()) {
                Chamado chamado = chamadoOpt.get();
                response.put("id", chamado.getId());
                response.put("numero", chamado.getNumero());
                response.put("assunto", chamado.getAssunto());
                response.put("status", chamado.getStatus());
                response.put("prioridade", chamado.getPrioridade());
                response.put("slaRestante", chamado.getSlaRestante());
                response.put("sucesso", true);
            } else {
                response.put("sucesso", false);
                response.put("erro", "Chamado não encontrado");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao buscar chamado {}: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", false);
            response.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Endpoints específicos para Backlog de Chamados
    
    @GetMapping("/backlog")
    public String backlog(Model model) {
        try {
            logger.info("Carregando página do backlog de chamados");
            
            // Mock de usuário logado
            Usuario usuarioLogado = new Usuario();
            usuarioLogado.setId(1L);
            usuarioLogado.setNome("Administrador");
            model.addAttribute("usuarioLogado", usuarioLogado);
            
            // Dados do backlog
            Long totalBacklog = backlogChamadoService.contarTotalBacklog();
            List<com.jaasielsilva.portalceo.model.BacklogChamado> backlogOrdenado = 
                backlogChamadoService.listarBacklogOrdenado();
            Map<String, Object> estatisticas = backlogChamadoService.obterEstatisticasBacklog();
            Map<String, Object> metricas = backlogChamadoService.obterMetricasPerformance();
            
            model.addAttribute("totalBacklog", totalBacklog);
            model.addAttribute("backlogChamados", backlogOrdenado);
            model.addAttribute("estatisticasBacklog", estatisticas);
            model.addAttribute("metricasBacklog", metricas);
            
            return "suporte/backlog";
            
        } catch (Exception e) {
            logger.error("Erro ao carregar backlog: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar dados do backlog");
            return "suporte/backlog";
        }
    }
    
    @GetMapping("/api/backlog")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBacklogData() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            Long total = backlogChamadoService.contarTotalBacklog();
            Map<String, Object> estatisticas = backlogChamadoService.obterEstatisticasBacklog();
            Map<String, Object> metricas = backlogChamadoService.obterMetricasPerformance();
            
            response.put("total", total);
            response.put("estatisticas", estatisticas);
            response.put("metricas", metricas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao buscar dados do backlog: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/api/backlog/proximo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProximoChamado() {
        try {
            Optional<com.jaasielsilva.portalceo.model.BacklogChamado> proximo = 
                backlogChamadoService.obterProximoChamado();
            
            Map<String, Object> response = new HashMap<>();
            if (proximo.isPresent()) {
                response.put("chamado", proximo.get());
                response.put("encontrado", true);
            } else {
                response.put("encontrado", false);
                response.put("mensagem", "Nenhum chamado na fila");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao buscar próximo chamado: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/api/backlog/adicionar/{chamadoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adicionarAoBacklog(@PathVariable Long chamadoId) {
        try {
            Optional<Chamado> chamadoOpt = chamadoService.buscarPorId(chamadoId);
            
            if (chamadoOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("erro", "Chamado não encontrado");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            com.jaasielsilva.portalceo.model.BacklogChamado backlogItem = 
                backlogChamadoService.adicionarAoBacklog(chamadoOpt.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("backlogItem", backlogItem);
            response.put("mensagem", "Chamado adicionado ao backlog com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao adicionar chamado ao backlog: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @DeleteMapping("/api/backlog/remover/{chamadoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removerDoBacklog(@PathVariable Long chamadoId) {
        try {
            backlogChamadoService.removerDoBacklog(chamadoId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Chamado removido do backlog com sucesso");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao remover chamado do backlog: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // Teste com template simplificado
    @GetMapping("/chamados/{id}/simples")
    public String visualizarChamadoSimples(@PathVariable Long id, Model model) {
        try {
            Chamado chamado = chamadoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
            
            model.addAttribute("chamado", chamado);
            logger.info("Visualizando chamado simples {}: {}", id, chamado.getNumero());
        } catch (Exception e) {
            logger.error("Erro ao visualizar chamado simples {}: {}", id, e.getMessage());
            model.addAttribute("chamado", null);
        }
        
        return "suporte/visualizar-simples";
    }

}