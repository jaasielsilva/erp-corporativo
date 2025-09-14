package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.model.Usuario;
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
import java.util.Optional;

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
            
            // Chamados com SLA crítico
            List<Chamado> chamadosSlaVencido = chamadoService.buscarChamadosComSlaVencido();
            List<Chamado> chamadosSlaProximo = chamadoService.buscarChamadosComSlaProximoVencimento();
            
            // Adicionar dados ao model
            model.addAttribute("chamadosAbertos", chamadosAbertos);
            model.addAttribute("chamadosEmAndamento", chamadosEmAndamento);
            model.addAttribute("chamadosResolvidos", chamadosResolvidos);
            model.addAttribute("slaMedio", slaMedio != null ? Math.round(slaMedio * 100.0) / 100.0 : 0.0);
            model.addAttribute("avaliacaoMedia", avaliacaoMedia != null ? Math.round(avaliacaoMedia * 100.0) / 100.0 : 0.0);
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
        
        return "suporte/teste-debug";
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
    
    // Endpoint de teste simples para debug
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        return "Servidor funcionando corretamente - " + new java.util.Date();
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