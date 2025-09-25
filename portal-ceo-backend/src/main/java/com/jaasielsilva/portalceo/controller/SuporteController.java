package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.service.ChamadoService;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.BacklogChamadoService;
import com.jaasielsilva.portalceo.service.SlaMonitoramentoService;
import com.jaasielsilva.portalceo.dto.CategoriaChamadoDTO;
import com.jaasielsilva.portalceo.dto.AtualizarStatusRequest;
import com.jaasielsilva.portalceo.dto.ChamadoStatusResponse;
import com.jaasielsilva.portalceo.service.ChamadoStateMachine;
import com.jaasielsilva.portalceo.service.ChamadoAuditoriaService;
import com.jaasielsilva.portalceo.security.PermissaoBusinessService;
import com.jaasielsilva.portalceo.security.PerfilUsuario;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/suporte")
public class SuporteController {

    private static final Logger logger = LoggerFactory.getLogger(SuporteController.class);

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private BacklogChamadoService backlogChamadoService;

    @Autowired
    private SlaMonitoramentoService slaMonitoramentoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ChamadoStateMachine stateMachine;

    @Autowired
    private ChamadoAuditoriaService auditoriaService;

    @Autowired
    private PermissaoBusinessService permissaoService;

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
            model.addAttribute("avaliacaoMedia",
                    avaliacaoMedia != null ? Math.round(avaliacaoMedia * 100.0) / 100.0 : 0.0);
            model.addAttribute("ultimosChamados", ultimosChamados);
            model.addAttribute("estatisticasPrioridade", estatisticasPrioridade);
            model.addAttribute("estatisticasStatus", estatisticasStatus);
            model.addAttribute("estatisticasCategoria", estatisticasCategoria);
            model.addAttribute("chamadosSlaVencido", chamadosSlaVencido.size());
            model.addAttribute("chamadosSlaProximo", chamadosSlaProximo.size());
            model.addAttribute("taxaResolucaoPrazo", taxaResolucaoPrazo != null ? taxaResolucaoPrazo : 0.0);
            model.addAttribute("tempoMedioPrimeiraResposta",
                    tempoMedioPrimeiraResposta != null ? tempoMedioPrimeiraResposta : 0.0);
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

    /**
     * API para tempo médio de resolução dos últimos N dias
     */
    @GetMapping("/api/tempo-medio-ultimos-dias")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTempoMedioUltimosDias(@RequestParam(defaultValue = "30") int dias) {
        try {
            Map<String, Object> dados = chamadoService.obterTempoMedioResolucaoUltimosDias(dias);
            return ResponseEntity.ok(dados);
        } catch (Exception e) {
            logger.error("Erro ao obter tempo médio dos últimos {} dias: {}", dias, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Endpoint para estatísticas de SLA
    @GetMapping("/api/sla-estatisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obterEstatisticasSla() {
        try {
            SlaMonitoramentoService.SlaEstatisticas stats = slaMonitoramentoService.calcularEstatisticasSla();

            Map<String, Object> response = new HashMap<>();
            response.put("chamadosVencidos", stats.getChamadosVencidos());
            response.put("chamadosProximaHora", stats.getChamadosProximaHora());
            response.put("chamadosProximasDuasHoras", stats.getChamadosProximasDuasHoras());
            response.put("chamadosProximasQuatroHoras", stats.getChamadosProximasQuatroHoras());
            response.put("sucesso", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas de SLA: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            errorResponse.put("sucesso", false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API para listar chamados - usado pela página de status
     */
    @GetMapping("/api/chamados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarChamados() {
        try {
            List<Chamado> chamados = chamadoService.listarTodos();
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("dados", chamados);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao listar chamados: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API para tempo médio de primeira resposta
     */
    @GetMapping("/api/tempo-medio-primeira-resposta")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTempoMedioPrimeiraResposta() {
        try {
            Double tempoMedio = chamadoService.calcularTempoMedioPrimeiraResposta();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tempoMedio", tempoMedio);
            response.put("unidade", "horas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao obter tempo médio de primeira resposta: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API para métricas de SLA dos últimos N dias
     */
    @GetMapping("/api/metricas-sla-periodo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricasSLAPeriodo(@RequestParam(defaultValue = "30") int dias) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> metricasSLA = chamadoService.calcularMetricasSLAUltimosDias(dias);

            response.put("success", true);
            response.put("periodo", dias + " dias");
            response.putAll(metricasSLA);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao obter métricas de SLA por período: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API para comparar métricas de SLA entre diferentes períodos
     */
    @GetMapping("/api/metricas-sla-comparativo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricasSLAComparativo() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Métricas dos últimos 7 dias
            Map<String, Object> ultimos7Dias = chamadoService.calcularMetricasSLAUltimosDias(7);

            // Métricas dos últimos 30 dias
            Map<String, Object> ultimos30Dias = chamadoService.calcularMetricasSLAUltimosDias(30);

            // Métricas gerais (todos os chamados)
            Map<String, Object> metricasGerais = chamadoService.calcularMetricasSLA();

            response.put("success", true);
            response.put("ultimos7Dias", ultimos7Dias);
            response.put("ultimos30Dias", ultimos30Dias);
            response.put("geral", metricasGerais);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao obter métricas comparativas de SLA: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API para tendência de cumprimento SLA dos últimos N dias
     */
    @GetMapping("/api/tendencia-sla")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTendenciaSLA(@RequestParam(defaultValue = "30") int dias) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> tendenciaSLA = chamadoService.calcularTendenciaSLA(dias);

            response.put("success", true);
            response.put("periodo", dias + " dias");
            response.putAll(tendenciaSLA);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao obter tendência de SLA: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(response);
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

    // Página para gerenciar status dos chamados
    @GetMapping("/status")
    public String gerenciarStatus(Model model) {
        try {
            logger.info("Carregando página de gerenciar status");

            // Buscar todos os chamados para exibir na página
            List<Chamado> todosChamados = chamadoService.listarTodos();
            
            // Verificar se há chamados
            if (todosChamados == null) {
                todosChamados = new ArrayList<>();
            }
            
            logger.info("Carregados {} chamados para a página de status", todosChamados.size());
            
            // Log dos chamados para debug
            for (Chamado chamado : todosChamados) {
                logger.info("Chamado: {} - {} - {}", chamado.getId(), chamado.getNumero(), chamado.getAssunto());
            }

            model.addAttribute("todosChamados", todosChamados);

            return "suporte/status";
        } catch (Exception e) {
            logger.error("Erro ao carregar página de status: {}", e.getMessage(), e);
            model.addAttribute("todosChamados", new ArrayList<>());
            return "suporte/status";
        }
    }

    // Página para atribuir chamados
    @GetMapping("/atribuir")
    public String atribuirChamados(Model model) {
        try {
            logger.info("Carregando página de atribuir chamados");

            // Obter usuário logado
            Usuario usuarioLogado = obterUsuarioLogado();

            // Buscar chamados não atribuídos
            List<Chamado> chamadosNaoAtribuidos = chamadoService.listarChamadosNaoAtribuidos();

            // Buscar técnicos disponíveis
            List<Colaborador> tecnicosDisponiveis = chamadoService.buscarTecnicosDisponiveis();

            // Verificar se o usuário logado está disponível para atribuição automática
            boolean usuarioDisponivelParaAtribuicao = false;
            if (usuarioLogado != null && usuarioLogado.getColaborador() != null) {
                usuarioDisponivelParaAtribuicao = chamadoService
                        .isColaboradorDisponivel(usuarioLogado.getColaborador());
            }

            model.addAttribute("chamadosNaoAtribuidos", chamadosNaoAtribuidos);
            model.addAttribute("tecnicosDisponiveis", tecnicosDisponiveis);
            model.addAttribute("usuarioDisponivelParaAtribuicao", usuarioDisponivelParaAtribuicao);
            model.addAttribute("usuarioLogado", usuarioLogado);

            return "suporte/atribuir";
        } catch (Exception e) {
            logger.error("Erro ao carregar página de atribuição: {}", e.getMessage());
            model.addAttribute("chamadosNaoAtribuidos", List.of());
            model.addAttribute("tecnicosDisponiveis", List.of());
            model.addAttribute("usuarioDisponivelParaAtribuicao", false);
            return "suporte/atribuir";
        }
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

    // API para obter categorias e subcategorias
    @GetMapping("/api/categorias")
    @ResponseBody
    public ResponseEntity<List<CategoriaChamadoDTO>> obterCategorias() {
        try {
            List<CategoriaChamadoDTO> categorias = new ArrayList<>();

            // Categoria Técnico
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasTecnico = Arrays.asList(
                    new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_HARDWARE", "Hardware",
                            "Problemas com computadores, impressoras, equipamentos de rede, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_SOFTWARE", "Software",
                            "Problemas com programas, sistemas, aplicativos, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_REDE", "Rede",
                            "Problemas com internet, wi-fi, conexões de rede, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_EMAIL", "E-mail",
                            "Problemas com contas de e-mail, configurações, acesso, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_SITE", "Site/E-commerce",
                            "Problemas com o site da empresa, loja virtual, etc."));
            categorias.add(new CategoriaChamadoDTO("TECNICO", "Técnico", subcategoriasTecnico));

            // Categoria Financeiro
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasFinanceiro = Arrays.asList(
                    new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_CONTAS_PAGAR", "Contas a Pagar",
                            "Dúvidas sobre contas, pagamentos, vencimentos, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_CONTAS_RECEBER", "Contas a Receber",
                            "Dúvidas sobre recebimentos, clientes, inadimplência, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_FLUXO_CAIXA", "Fluxo de Caixa",
                            "Relatórios, análises, projeções de caixa, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_ORCAMENTO", "Orçamento",
                            "Elaboração, análise e controle de orçamentos"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_IMPOSTOS", "Impostos",
                            "Dúvidas sobre tributação, guias, declarações, etc."));
            categorias.add(new CategoriaChamadoDTO("FINANCEIRO", "Financeiro", subcategoriasFinanceiro));

            // Categoria RH
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasRH = Arrays.asList(
                    new CategoriaChamadoDTO.SubcategoriaDTO("RH_FOLHA_PAGAMENTO", "Folha de Pagamento",
                            "Dúvidas sobre salários, descontos, benefícios, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("RH_ADMISSAO", "Admissão",
                            "Processos de contratação, documentação, integração, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("RH_DEMISSAO", "Demissão",
                            "Processos de desligamento, rescisões, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("RH_FERIAS", "Férias",
                            "Solicitações, agendamentos, cálculos de férias, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("RH_BENEFICIOS", "Benefícios",
                            "Plano de saúde, odontológico, vale transporte, etc."));
            categorias.add(new CategoriaChamadoDTO("RH", "Recursos Humanos", subcategoriasRH));

            // Categoria Vendas
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasVendas = Arrays.asList(
                    new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_PEDIDOS", "Pedidos",
                            "Dúvidas sobre pedidos, alterações, cancelamentos, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_CLIENTES", "Clientes",
                            "Cadastro, atualização, dúvidas sobre clientes, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_COMISSOES", "Comissões",
                            "Cálculos, pagamentos, relatórios de comissões, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_PROMOCOES", "Promoções",
                            "Criação, acompanhamento de promoções e campanhas"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_RELATORIOS", "Relatórios",
                            "Relatórios de vendas, performance, etc."));
            categorias.add(new CategoriaChamadoDTO("VENDAS", "Vendas", subcategoriasVendas));

            // Categoria Estoque
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasEstoque = Arrays.asList(
                    new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_MOVIMENTACAO", "Movimentação",
                            "Entradas, saídas, transferências de estoque"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_PRODUTOS", "Produtos",
                            "Cadastro, atualização, informações de produtos"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_INVENTARIO", "Inventário",
                            "Contagens, ajustes, reconciliações de estoque"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_FORNECEDORES", "Fornecedores",
                            "Cadastro, atualização, dúvidas sobre fornecedores"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_COMPRAS", "Compras",
                            "Solicitações, cotações, compras de produtos"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_RELATORIOS", "Relatórios",
                            "Relatórios de estoque, movimentações, etc."));
            categorias.add(new CategoriaChamadoDTO("ESTOQUE", "Estoque", subcategoriasEstoque));

            // Categoria Geral
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasGeral = Arrays.asList(
                    new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_ADMINISTRATIVO", "Administrativo",
                            "Questões administrativas gerais da empresa"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_COMUNICACAO", "Comunicação",
                            "Dúvidas sobre comunicação interna, avisos, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_SEGURANCA", "Segurança",
                            "Questões de segurança física e digital"),
                    new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_MANUTENCAO", "Manutenção",
                            "Solicitações de manutenção predial, equipamentos, etc."),
                    new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_OUTROS", "Outros",
                            "Outras questões que não se enquadram nas categorias acima"));
            categorias.add(new CategoriaChamadoDTO("GERAL", "Geral", subcategoriasGeral));

            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            logger.error("Erro ao obter categorias: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
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

    // Endpoint DEPRECIADO - mantido para compatibilidade
    @PostMapping("/chamados/{id}/status")
    @ResponseBody
    @Deprecated
    public ResponseEntity<Map<String, Object>> atualizarStatusLegacy(
            @PathVariable Long id,
            @RequestParam String acao,
            @RequestParam(required = false) String tecnico) {
        
        logger.warn("Uso de endpoint depreciado: /suporte/chamados/{}/status. Use /api/suporte/chamados/{}/status", id, id);
        
        // Redireciona para o novo endpoint
        AtualizarStatusRequest request = new AtualizarStatusRequest(acao, tecnico);
        ResponseEntity<ChamadoStatusResponse> response = atualizarStatusPadronizado(id, request);
        
        // Converte resposta para formato legacy
        Map<String, Object> legacyResponse = new HashMap<>();
        ChamadoStatusResponse statusResponse = response.getBody();
        
        if (statusResponse != null) {
            legacyResponse.put("sucesso", statusResponse.isSucesso());
            if (statusResponse.isSucesso()) {
                legacyResponse.put("status", statusResponse.getStatusDescricao());
                legacyResponse.put("slaRestante", statusResponse.getSlaRestante());
            } else {
                legacyResponse.put("erro", statusResponse.getMensagem());
            }
        }
        
        return ResponseEntity.status(response.getStatusCode()).body(legacyResponse);
    }

    // Endpoint PUT para atualizar status (usado pelo JavaScript)
    @PutMapping("/api/chamados/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atualizarStatusViaPut(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String status = (String) requestBody.get("status");
            String tecnicoResponsavel = (String) requestBody.get("tecnicoResponsavel");
            String observacoes = (String) requestBody.get("observacoes");
            
            // Log para debug
            logger.info("Recebendo requisição PUT para chamado {}: status={}, tecnico={}", id, status, tecnicoResponsavel);
            
            // Mapear status para ação
            String acao;
            switch (status) {
                case "EM_ANDAMENTO":
                    acao = "iniciar";
                    break;
                case "RESOLVIDO":
                    acao = "resolver";
                    break;
                case "FECHADO":
                    acao = "fechar";
                    break;
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("sucesso", false);
                    errorResponse.put("erro", "Status inválido: " + status);
                    return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Criar request padronizado
            AtualizarStatusRequest request = new AtualizarStatusRequest(acao, tecnicoResponsavel);
            if (observacoes != null) {
                request.setObservacoes(observacoes);
            }
            ResponseEntity<ChamadoStatusResponse> response = atualizarStatusPadronizado(id, request);
            
            // Converter resposta para formato esperado pelo JavaScript
            Map<String, Object> jsResponse = new HashMap<>();
            ChamadoStatusResponse statusResponse = response.getBody();
            
            if (statusResponse != null) {
                jsResponse.put("sucesso", statusResponse.isSucesso());
                if (statusResponse.isSucesso()) {
                    jsResponse.put("status", statusResponse.getStatusDescricao());
                    jsResponse.put("slaRestante", statusResponse.getSlaRestante());
                } else {
                    jsResponse.put("erro", statusResponse.getMensagem());
                }
            }
            
            return ResponseEntity.status(response.getStatusCode()).body(jsResponse);
            
        } catch (Exception e) {
            logger.error("Erro ao atualizar status via PUT para chamado {}: {}", id, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", "Erro interno do servidor");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Novo endpoint padronizado com validações
    @PostMapping("/api/chamados/{id}/status-padronizado")
    @ResponseBody
    public ResponseEntity<ChamadoStatusResponse> atualizarStatusPadronizado(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusRequest request) {

        try {
            // Verificar permissão para atualizar status
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Assumindo perfil TECNICO por padrão - em um sistema real, isso viria do banco de dados
            PerfilUsuario perfil = PerfilUsuario.TECNICO;
            
            if (!permissaoService.podeAtualizarStatus(perfil, request.getAcao())) {
                auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS", id, username, 
                    "Usuário não possui permissão para executar a ação: " + request.getAcao());
                return ResponseEntity.status(403)
                    .body(ChamadoStatusResponse.erro("Acesso negado: você não possui permissão para executar esta ação"));
            }
            // Buscar chamado
            Optional<Chamado> chamadoOpt = chamadoService.buscarPorId(id);
            if (chamadoOpt.isEmpty()) {
                auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS", id, null, "Chamado não encontrado");
                return ResponseEntity.badRequest()
                    .body(ChamadoStatusResponse.erro("Chamado não encontrado"));
            }

            Chamado chamado = chamadoOpt.get();
            StatusChamado statusAtual = chamado.getStatus();
            StatusChamado novoStatus = request.getStatusDestino();

            // Validar transição de status
            if (!stateMachine.isTransicaoValida(statusAtual, novoStatus)) {
                String erro = String.format("Transição inválida de %s para %s", 
                    statusAtual.getDescricao(), novoStatus.getDescricao());
                auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS", id, null, erro);
                return ResponseEntity.badRequest()
                    .body(ChamadoStatusResponse.erro(erro));
            }

            // Executar ação baseada no tipo
            Chamado chamadoAtualizado;
            String acao = request.getAcao().toLowerCase();
            
            switch (acao) {
                case "iniciar":
                    if (request.getTecnicoResponsavel() == null || request.getTecnicoResponsavel().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                            .body(ChamadoStatusResponse.erro("Técnico responsável é obrigatório para iniciar atendimento"));
                    }
                    chamadoAtualizado = chamadoService.iniciarAtendimento(id, request.getTecnicoResponsavel());
                    auditoriaService.registrarMudancaStatus(chamadoAtualizado, statusAtual, novoStatus, request.getTecnicoResponsavel(), request.getObservacoes());
                    break;
                    
                case "resolver":
                    chamadoAtualizado = chamadoService.resolverChamado(id);
                    auditoriaService.registrarMudancaStatus(chamadoAtualizado, statusAtual, novoStatus, chamado.getTecnicoResponsavel(), request.getObservacoes());
                    break;
                    
                case "fechar":
                    chamadoAtualizado = chamadoService.fecharChamado(id);
                    auditoriaService.registrarMudancaStatus(chamadoAtualizado, statusAtual, novoStatus, chamado.getTecnicoResponsavel(), request.getObservacoes());
                    break;
                    
                case "reabrir":
                    if (request.getMotivoReabertura() == null || request.getMotivoReabertura().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                            .body(ChamadoStatusResponse.erro("Motivo da reabertura é obrigatório"));
                    }
                    chamadoAtualizado = chamadoService.reabrirChamado(id);
                    auditoriaService.registrarReaberturaChamado(chamadoAtualizado, null, request.getMotivoReabertura());
                    break;
                    
                default:
                    auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS", id, null, "Ação inválida: " + acao);
                    return ResponseEntity.badRequest()
                        .body(ChamadoStatusResponse.erro("Ação inválida: " + acao));
            }

            return ResponseEntity.ok(ChamadoStatusResponse.sucesso(chamadoAtualizado, "Status atualizado com sucesso"));

        } catch (Exception e) {
            logger.error("Erro ao atualizar status do chamado {}: {}", id, e.getMessage(), e);
            auditoriaService.registrarErroOperacao("ATUALIZAR_STATUS", id, null, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ChamadoStatusResponse.erro("Erro interno: " + e.getMessage()));
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

            double taxaSatisfacao = totalAvaliacoes > 0 ? (avaliacoesPositivas * 100.0) / totalAvaliacoes : 0.0;

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

            // Dados do backlog
            Long totalBacklog = backlogChamadoService.contarTotalBacklog();
            List<com.jaasielsilva.portalceo.model.BacklogChamado> backlogOrdenado = backlogChamadoService
                    .listarBacklogOrdenado();
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
            Optional<com.jaasielsilva.portalceo.model.BacklogChamado> proximo = backlogChamadoService
                    .obterProximoChamado();

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

            com.jaasielsilva.portalceo.model.BacklogChamado backlogItem = backlogChamadoService
                    .adicionarAoBacklog(chamadoOpt.get());

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

    @GetMapping("/api/public/debug-dados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugDados() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Contar total de chamados
            List<Chamado> todosChamados = chamadoService.listarTodos();
            long totalChamados = todosChamados.size();

            // Contar chamados resolvidos
            List<Chamado> chamadosResolvidos = chamadoService.listarResolvidos();
            long totalResolvidos = chamadosResolvidos.size();

            // Calcular tempo médio
            Double tempoMedio = chamadoService.calcularTempoMedioResolucaoGeral();

            // Calcular SLA médio
            Double slaMedio = chamadoService.calcularSlaMedio();

            // Métricas de SLA
            Map<String, Object> metricasSLA = chamadoService.calcularMetricasSLA();

            response.put("success", true);
            response.put("totalChamados", totalChamados);
            response.put("totalResolvidos", totalResolvidos);
            response.put("tempoMedioResolucao", tempoMedio);
            response.put("slaMedio", slaMedio);
            response.put("metricasSLA", metricasSLA);
            response.put("chamadosDetalhes", todosChamados.stream()
                    .limit(5)
                    .map(c -> Map.of(
                            "id", c.getId(),
                            "numero", c.getNumero(),
                            "status", c.getStatus().toString(),
                            "dataAbertura", c.getDataAbertura().toString(),
                            "dataResolucao", c.getDataResolucao() != null ? c.getDataResolucao().toString() : "null"))
                    .collect(java.util.stream.Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao obter dados de debug: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Endpoint para atribuição automática de chamado
    @PostMapping("/atribuir-automatico/{chamadoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> atribuirAutomatico(@PathVariable Long chamadoId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuarioLogado = obterUsuarioLogado();

            if (usuarioLogado == null || usuarioLogado.getColaborador() == null) {
                response.put("success", false);
                response.put("message", "Usuário não encontrado ou não é um colaborador");
                return ResponseEntity.badRequest().body(response);
            }

            Chamado chamadoAtribuido = chamadoService.atribuirChamadoAutomaticamente(chamadoId,
                    usuarioLogado.getColaborador());

            if (chamadoAtribuido != null) {
                response.put("success", true);
                response.put("message", "Chamado atribuído com sucesso!");
                response.put("chamado", chamadoAtribuido);
            } else {
                response.put("success", false);
                response.put("message", "Não foi possível atribuir o chamado. Verifique se você está disponível.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao atribuir chamado automaticamente: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Erro interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Método auxiliar para obter usuário logado
    private Usuario obterUsuarioLogado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser")) {

                String email = authentication.getName(); // Spring Security normalmente coloca email ou login aqui
                return usuarioRepository.findByEmail(email)
                        .orElse(null);
            }
        } catch (Exception e) {
            logger.error("Erro ao obter usuário logado: {}", e.getMessage());
        }
        return null;
    }

    // ENDPOINTS DE EXPORTAÇÃO
    
    /**
     * Exporta lista de chamados em formato Excel
     */
    @GetMapping("/api/chamados/export/excel")
    @ResponseBody
    public ResponseEntity<String> exportarChamadosExcel(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @RequestParam(required = false) String tecnico) {
        
        try {
            logger.info("Exportando chamados para Excel - Status: {}, Prioridade: {}, Técnico: {}", 
                       status, prioridade, tecnico);
            
            List<Chamado> chamados = chamadoService.listarTodos();
            
            // Aplicar filtros se fornecidos
            if (status != null && !status.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getStatus().name().equals(status))
                    .toList();
            }
            
            if (prioridade != null && !prioridade.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getPrioridade().name().equals(prioridade))
                    .toList();
            }
            
            if (tecnico != null && !tecnico.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getTecnicoResponsavel() != null && 
                               c.getTecnicoResponsavel().toLowerCase().contains(tecnico.toLowerCase()))
                    .toList();
            }
            
            String csvContent = gerarCSVChamados(chamados);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDispositionFormData("attachment", 
                "chamados_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
                
        } catch (Exception e) {
            logger.error("Erro ao exportar chamados para Excel", e);
            return ResponseEntity.internalServerError()
                .body("Erro ao gerar relatório Excel");
        }
    }
    
    /**
     * Exporta lista de chamados em formato PDF
     */
    @GetMapping("/api/chamados/export/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> exportarChamadosPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @RequestParam(required = false) String tecnico,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String solicitante,
            @RequestParam(required = false, defaultValue = "false") boolean incluirDetalhes,
            @RequestParam(required = false, defaultValue = "false") boolean incluirEstatisticas) {
        
        try {
            logger.info("Exportando chamados para PDF - Período: {} a {}, Status: {}, Prioridade: {}, Técnico: {}, Categoria: {}, Solicitante: {}", 
                       dataInicio, dataFim, status, prioridade, tecnico, categoria, solicitante);
            
            List<Chamado> chamados = chamadoService.listarTodos();
            
            // Aplicar filtros se fornecidos
            if (dataInicio != null) {
                chamados = chamados.stream()
                    .filter(c -> c.getDataAbertura().toLocalDate().isAfter(dataInicio.minusDays(1)))
                    .toList();
            }
            
            if (dataFim != null) {
                chamados = chamados.stream()
                    .filter(c -> c.getDataAbertura().toLocalDate().isBefore(dataFim.plusDays(1)))
                    .toList();
            }
            
            if (status != null && !status.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getStatus().name().equals(status))
                    .toList();
            }
            
            if (prioridade != null && !prioridade.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getPrioridade().name().equals(prioridade))
                    .toList();
            }
            
            if (tecnico != null && !tecnico.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getTecnicoResponsavel() != null && 
                               c.getTecnicoResponsavel().toLowerCase().contains(tecnico.toLowerCase()))
                    .toList();
            }
            
            if (categoria != null && !categoria.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getCategoria() != null && 
                               c.getCategoria().toLowerCase().contains(categoria.toLowerCase()))
                    .toList();
            }
            
            if (solicitante != null && !solicitante.isEmpty()) {
                chamados = chamados.stream()
                    .filter(c -> c.getSolicitanteNome() != null && 
                               c.getSolicitanteNome().toLowerCase().contains(solicitante.toLowerCase()))
                    .toList();
            }
            
            byte[] pdfContent = gerarPDFChamados(chamados, incluirDetalhes, incluirEstatisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "relatorio_chamados_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
                
        } catch (Exception e) {
            logger.error("Erro ao exportar chamados para PDF", e);
            return ResponseEntity.internalServerError()
                .body("Erro ao gerar relatório PDF".getBytes());
        }
    }
    
    /**
     * Gera conteúdo CSV dos chamados
     */
    private String gerarCSVChamados(List<Chamado> chamados) {
        StringBuilder csv = new StringBuilder();
        
        // Cabeçalho
        csv.append("Número,Assunto,Status,Prioridade,Categoria,Solicitante,Email,Técnico,Data Abertura,Data Resolução,SLA Restante\n");
        
        // Dados
        for (Chamado chamado : chamados) {
            csv.append(String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                chamado.getNumero(),
                chamado.getAssunto().replace("\"", "\"\""), // Escape aspas duplas
                chamado.getStatus().getDescricao(),
                chamado.getPrioridade().getDescricao(),
                chamado.getCategoria() != null ? chamado.getCategoria() : "N/A",
                chamado.getSolicitanteNome() != null ? chamado.getSolicitanteNome() : "N/A",
                chamado.getSolicitanteEmail() != null ? chamado.getSolicitanteEmail() : "N/A",
                chamado.getTecnicoResponsavel() != null ? chamado.getTecnicoResponsavel() : "Não atribuído",
                chamado.getDataAbertura() != null ? chamado.getDataAbertura().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                chamado.getDataResolucao() != null ? chamado.getDataResolucao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                chamado.getSlaRestante() != null ? chamado.getSlaRestante() + "h" : "N/A"
            ));
        }
        
        return csv.toString();
    }
    
    /**
     * Gera conteúdo PDF dos chamados usando iText
     */
    private byte[] gerarPDFChamados(List<Chamado> chamados, boolean incluirDetalhes, boolean incluirEstatisticas) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Título do relatório
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("RELATÓRIO DE CHAMADOS DE SUPORTE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Informações do cabeçalho
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Paragraph header = new Paragraph();
        header.add(new Chunk("Data de Geração: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n", headerFont));
        header.add(new Chunk("Total de Chamados: " + chamados.size() + "\n\n", headerFont));
        document.add(header);
        
        // Tabela de chamados
        PdfPTable table = new PdfPTable(7); // 7 colunas
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Definir larguras das colunas
        float[] columnWidths = {1f, 2f, 1.5f, 1f, 1.5f, 2f, 1.5f};
        table.setWidths(columnWidths);
        
        // Cabeçalhos da tabela
        Font headerCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        BaseColor headerColor = new BaseColor(52, 73, 94);
        
        String[] headers = {"Número", "Assunto", "Status", "Prioridade", "Categoria", "Solicitante", "Técnico"};
        for (String headerText : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(headerText, headerCellFont));
            cell.setBackgroundColor(headerColor);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        
        // Dados dos chamados
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        for (Chamado chamado : chamados) {
            // Número
            PdfPCell cell1 = new PdfPCell(new Phrase(chamado.getNumero() != null ? chamado.getNumero().toString() : "N/A", cellFont));
            cell1.setPadding(5);
            table.addCell(cell1);
            
            // Assunto
            String assunto = chamado.getAssunto() != null ? chamado.getAssunto() : "N/A";
            if (assunto.length() > 30) {
                assunto = assunto.substring(0, 27) + "...";
            }
            PdfPCell cell2 = new PdfPCell(new Phrase(assunto, cellFont));
            cell2.setPadding(5);
            table.addCell(cell2);
            
            // Status
            PdfPCell cell3 = new PdfPCell(new Phrase(chamado.getStatus() != null ? chamado.getStatus().getDescricao() : "N/A", cellFont));
            cell3.setPadding(5);
            table.addCell(cell3);
            
            // Prioridade
            PdfPCell cell4 = new PdfPCell(new Phrase(chamado.getPrioridade() != null ? chamado.getPrioridade().getDescricao() : "N/A", cellFont));
            cell4.setPadding(5);
            table.addCell(cell4);
            
            // Categoria
            PdfPCell cell5 = new PdfPCell(new Phrase(chamado.getCategoria() != null ? chamado.getCategoria() : "N/A", cellFont));
            cell5.setPadding(5);
            table.addCell(cell5);
            
            // Solicitante
            String solicitante = chamado.getSolicitanteNome() != null ? chamado.getSolicitanteNome() : "N/A";
            if (solicitante.length() > 25) {
                solicitante = solicitante.substring(0, 22) + "...";
            }
            PdfPCell cell6 = new PdfPCell(new Phrase(solicitante, cellFont));
            cell6.setPadding(5);
            table.addCell(cell6);
            
            // Técnico
            String tecnico = chamado.getTecnicoResponsavel() != null ? chamado.getTecnicoResponsavel() : "Não atribuído";
            if (tecnico.length() > 20) {
                tecnico = tecnico.substring(0, 17) + "...";
            }
            PdfPCell cell7 = new PdfPCell(new Phrase(tecnico, cellFont));
            cell7.setPadding(5);
            table.addCell(cell7);
        }
        
        document.add(table);
        
        // Seção de Estatísticas (se solicitada)
        if (incluirEstatisticas) {
            document.newPage();
            
            Font statsTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph statsHeader = new Paragraph("ESTATÍSTICAS DO RELATÓRIO", statsTitle);
            statsHeader.setAlignment(Element.ALIGN_CENTER);
            statsHeader.setSpacingAfter(20);
            document.add(statsHeader);
            
            // Calcular estatísticas
            Map<String, Long> statusCount = new HashMap<>();
            Map<String, Long> prioridadeCount = new HashMap<>();
            Map<String, Long> categoriaCount = new HashMap<>();
            
            for (Chamado chamado : chamados) {
                // Contagem por status
                String status = chamado.getStatus() != null ? chamado.getStatus().getDescricao() : "Não definido";
                statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
                
                // Contagem por prioridade
                String prioridade = chamado.getPrioridade() != null ? chamado.getPrioridade().getDescricao() : "Não definida";
                prioridadeCount.put(prioridade, prioridadeCount.getOrDefault(prioridade, 0L) + 1);
                
                // Contagem por categoria
                String categoria = chamado.getCategoria() != null ? chamado.getCategoria() : "Não definida";
                categoriaCount.put(categoria, categoriaCount.getOrDefault(categoria, 0L) + 1);
            }
            
            Font statsFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font statsBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            
            // Estatísticas por Status
            Paragraph statusStats = new Paragraph();
            statusStats.add(new Chunk("Distribuição por Status:\n", statsBoldFont));
            for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / chamados.size();
                statusStats.add(new Chunk(String.format("• %s: %d chamados (%.1f%%)\n", 
                    entry.getKey(), entry.getValue(), percentage), statsFont));
            }
            statusStats.setSpacingAfter(15);
            document.add(statusStats);
            
            // Estatísticas por Prioridade
            Paragraph prioridadeStats = new Paragraph();
            prioridadeStats.add(new Chunk("Distribuição por Prioridade:\n", statsBoldFont));
            for (Map.Entry<String, Long> entry : prioridadeCount.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / chamados.size();
                prioridadeStats.add(new Chunk(String.format("• %s: %d chamados (%.1f%%)\n", 
                    entry.getKey(), entry.getValue(), percentage), statsFont));
            }
            prioridadeStats.setSpacingAfter(15);
            document.add(prioridadeStats);
            
            // Estatísticas por Categoria
            Paragraph categoriaStats = new Paragraph();
            categoriaStats.add(new Chunk("Distribuição por Categoria:\n", statsBoldFont));
            for (Map.Entry<String, Long> entry : categoriaCount.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / chamados.size();
                categoriaStats.add(new Chunk(String.format("• %s: %d chamados (%.1f%%)\n", 
                    entry.getKey(), entry.getValue(), percentage), statsFont));
            }
            document.add(categoriaStats);
        }
        
        // Seção de Detalhes (se solicitada)
        if (incluirDetalhes) {
            document.newPage();
            
            Font detailsTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph detailsHeader = new Paragraph("DETALHES DOS CHAMADOS", detailsTitle);
            detailsHeader.setAlignment(Element.ALIGN_CENTER);
            detailsHeader.setSpacingAfter(20);
            document.add(detailsHeader);
            
            Font detailFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font detailBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            
            for (Chamado chamado : chamados) {
                // Criar uma tabela para cada chamado
                PdfPTable detailTable = new PdfPTable(2);
                detailTable.setWidthPercentage(100);
                detailTable.setSpacingBefore(10f);
                detailTable.setSpacingAfter(10f);
                
                float[] detailWidths = {1f, 3f};
                detailTable.setWidths(detailWidths);
                
                // Cabeçalho do chamado
                PdfPCell headerCell = new PdfPCell(new Phrase("Chamado #" + 
                    (chamado.getNumero() != null ? chamado.getNumero() : "N/A"), detailBoldFont));
                headerCell.setColspan(2);
                headerCell.setBackgroundColor(new BaseColor(230, 230, 230));
                headerCell.setPadding(8);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                detailTable.addCell(headerCell);
                
                // Detalhes do chamado
                addDetailRow(detailTable, "Assunto:", chamado.getAssunto(), detailBoldFont, detailFont);
                addDetailRow(detailTable, "Descrição:", chamado.getDescricao(), detailBoldFont, detailFont);
                addDetailRow(detailTable, "Status:", 
                    chamado.getStatus() != null ? chamado.getStatus().getDescricao() : "N/A", 
                    detailBoldFont, detailFont);
                addDetailRow(detailTable, "Prioridade:", 
                    chamado.getPrioridade() != null ? chamado.getPrioridade().getDescricao() : "N/A", 
                    detailBoldFont, detailFont);
                addDetailRow(detailTable, "Categoria:", chamado.getCategoria(), detailBoldFont, detailFont);
                addDetailRow(detailTable, "Solicitante:", chamado.getSolicitanteNome(), detailBoldFont, detailFont);
                addDetailRow(detailTable, "Técnico:", chamado.getTecnicoResponsavel(), detailBoldFont, detailFont);
                addDetailRow(detailTable, "Data Abertura:", 
                    chamado.getDataAbertura() != null ? 
                        chamado.getDataAbertura().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A", 
                    detailBoldFont, detailFont);
                
                document.add(detailTable);
            }
        }
        
        // Rodapé
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        Paragraph footer = new Paragraph("\nRelatório gerado automaticamente pelo Sistema de Suporte", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
        
        document.close();
        
        return baos.toByteArray();
    }
    
    /**
     * Método auxiliar para adicionar linhas de detalhes na tabela do PDF
     */
    private void addDetailRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(new BaseColor(245, 245, 245));
        table.addCell(labelCell);
        
        String displayValue = (value != null && !value.trim().isEmpty()) ? value : "N/A";
        PdfPCell valueCell = new PdfPCell(new Phrase(displayValue, valueFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    // ENDPOINT PARA OBTER USUÁRIO ATUAL
    @GetMapping("/api/usuario/atual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obterUsuarioAtual() {
        try {
            Usuario usuarioLogado = obterUsuarioLogado();
            
            if (usuarioLogado == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Verificar se usuário pode atender chamados
            boolean podeAtenderChamados = permissaoService.podeGerenciarChamados(usuarioLogado);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", usuarioLogado.getId());
            response.put("nome", usuarioLogado.getNome());
            response.put("email", usuarioLogado.getEmail());
            response.put("podeAtenderChamados", podeAtenderChamados);
            
            if (usuarioLogado.getColaborador() != null) {
                response.put("colaboradorId", usuarioLogado.getColaborador().getId());
                response.put("cargo", usuarioLogado.getColaborador().getCargo());
                response.put("departamento", usuarioLogado.getColaborador().getDepartamento());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter usuário atual: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("erro", "Erro interno do servidor"));
        }
    }

}