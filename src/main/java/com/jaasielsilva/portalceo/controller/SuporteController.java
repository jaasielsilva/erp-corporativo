package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ChamadoService;
import com.jaasielsilva.portalceo.service.BacklogChamadoService;
import com.jaasielsilva.portalceo.service.SlaMonitoramentoService;
import com.jaasielsilva.portalceo.dto.CategoriaChamadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Autowired
    private SlaMonitoramentoService slaMonitoramentoService;

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
                new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_HARDWARE", "Hardware", "Problemas com computadores, impressoras, equipamentos de rede, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_SOFTWARE", "Software", "Problemas com programas, sistemas, aplicativos, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_REDE", "Rede", "Problemas com internet, wi-fi, conexões de rede, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_EMAIL", "E-mail", "Problemas com contas de e-mail, configurações, acesso, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("TECNICO_SITE", "Site/E-commerce", "Problemas com o site da empresa, loja virtual, etc.")
            );
            categorias.add(new CategoriaChamadoDTO("TECNICO", "Técnico", subcategoriasTecnico));
            
            // Categoria Financeiro
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasFinanceiro = Arrays.asList(
                new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_CONTAS_PAGAR", "Contas a Pagar", "Dúvidas sobre contas, pagamentos, vencimentos, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_CONTAS_RECEBER", "Contas a Receber", "Dúvidas sobre recebimentos, clientes, inadimplência, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_FLUXO_CAIXA", "Fluxo de Caixa", "Relatórios, análises, projeções de caixa, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_ORCAMENTO", "Orçamento", "Elaboração, análise e controle de orçamentos"),
                new CategoriaChamadoDTO.SubcategoriaDTO("FINANCEIRO_IMPOSTOS", "Impostos", "Dúvidas sobre tributação, guias, declarações, etc.")
            );
            categorias.add(new CategoriaChamadoDTO("FINANCEIRO", "Financeiro", subcategoriasFinanceiro));
            
            // Categoria RH
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasRH = Arrays.asList(
                new CategoriaChamadoDTO.SubcategoriaDTO("RH_FOLHA_PAGAMENTO", "Folha de Pagamento", "Dúvidas sobre salários, descontos, benefícios, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("RH_ADMISSAO", "Admissão", "Processos de contratação, documentação, integração, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("RH_DEMISSAO", "Demissão", "Processos de desligamento, rescisões, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("RH_FERIAS", "Férias", "Solicitações, agendamentos, cálculos de férias, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("RH_BENEFICIOS", "Benefícios", "Plano de saúde, odontológico, vale transporte, etc.")
            );
            categorias.add(new CategoriaChamadoDTO("RH", "Recursos Humanos", subcategoriasRH));
            
            // Categoria Vendas
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasVendas = Arrays.asList(
                new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_PEDIDOS", "Pedidos", "Dúvidas sobre pedidos, alterações, cancelamentos, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_CLIENTES", "Clientes", "Cadastro, atualização, dúvidas sobre clientes, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_COMISSOES", "Comissões", "Cálculos, pagamentos, relatórios de comissões, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_PROMOCOES", "Promoções", "Criação, acompanhamento de promoções e campanhas"),
                new CategoriaChamadoDTO.SubcategoriaDTO("VENDAS_RELATORIOS", "Relatórios", "Relatórios de vendas, performance, etc.")
            );
            categorias.add(new CategoriaChamadoDTO("VENDAS", "Vendas", subcategoriasVendas));
            
            // Categoria Estoque
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasEstoque = Arrays.asList(
                new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_MOVIMENTACAO", "Movimentação", "Entradas, saídas, transferências de estoque"),
                new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_PRODUTOS", "Produtos", "Cadastro, atualização, informações de produtos"),
                new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_INVENTARIO", "Inventário", "Contagens, ajustes, reconciliações de estoque"),
                new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_FORNECEDORES", "Fornecedores", "Cadastro, atualização, dúvidas sobre fornecedores"),
                new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_COMPRAS", "Compras", "Solicitações, cotações, compras de produtos"),
                new CategoriaChamadoDTO.SubcategoriaDTO("ESTOQUE_RELATORIOS", "Relatórios", "Relatórios de estoque, movimentações, etc.")
            );
            categorias.add(new CategoriaChamadoDTO("ESTOQUE", "Estoque", subcategoriasEstoque));
            
            // Categoria Geral
            List<CategoriaChamadoDTO.SubcategoriaDTO> subcategoriasGeral = Arrays.asList(
                new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_ADMINISTRATIVO", "Administrativo", "Questões administrativas gerais da empresa"),
                new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_COMUNICACAO", "Comunicação", "Dúvidas sobre comunicação interna, avisos, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_SEGURANCA", "Segurança", "Questões de segurança física e digital"),
                new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_MANUTENCAO", "Manutenção", "Solicitações de manutenção predial, equipamentos, etc."),
                new CategoriaChamadoDTO.SubcategoriaDTO("GERAL_OUTROS", "Outros", "Outras questões que não se enquadram nas categorias acima")
            );
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
                case "reabrir":
                    chamado = chamadoService.reabrirChamado(id);
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
                    "dataResolucao", c.getDataResolucao() != null ? c.getDataResolucao().toString() : "null"
                ))
                .collect(java.util.stream.Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro ao obter dados de debug: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}