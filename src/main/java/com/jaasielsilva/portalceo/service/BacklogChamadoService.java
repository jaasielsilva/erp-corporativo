package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.BacklogChamado;
import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.repository.BacklogChamadoRepository;
import com.jaasielsilva.portalceo.repository.ChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento inteligente do backlog de chamados
 * Implementa algoritmos de priorização e otimização da fila
 */
@Service
@Transactional
public class BacklogChamadoService {

    private static final Logger logger = LoggerFactory.getLogger(BacklogChamadoService.class);

    @Autowired
    private BacklogChamadoRepository backlogRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    /**
     * Adiciona um chamado ao backlog com priorização automática
     */
    public BacklogChamado adicionarAoBacklog(Chamado chamado) {
        try {
            // Verifica se já existe no backlog
            Optional<BacklogChamado> existente = backlogRepository.findByChamado(chamado);
            if (existente.isPresent()) {
                logger.warn("Chamado {} já existe no backlog", chamado.getId());
                return existente.get();
            }

            BacklogChamado backlogItem = new BacklogChamado();
            backlogItem.setChamado(chamado);
            backlogItem.setDataEntradaBacklog(LocalDateTime.now());
            
            // Calcula priorização automática
            calcularPriorizacao(backlogItem);
            
            // Define posição na fila
            definirPosicaoFila(backlogItem);
            
            // Sugere técnico baseado em expertise
            sugerirTecnico(backlogItem);
            
            // Estima tempo de atendimento
            estimarTempoAtendimento(backlogItem);

            BacklogChamado salvo = backlogRepository.save(backlogItem);
            logger.info("Chamado {} adicionado ao backlog com score {}", 
                       chamado.getId(), salvo.getScorePrioridade());
            
            return salvo;
            
        } catch (Exception e) {
            logger.error("Erro ao adicionar chamado {} ao backlog: {}", chamado.getId(), e.getMessage());
            throw new RuntimeException("Erro ao processar backlog", e);
        }
    }

    /**
     * Remove chamado do backlog (quando atendido ou cancelado)
     */
    public void removerDoBacklog(Long chamadoId) {
        try {
            Optional<BacklogChamado> backlogItem = backlogRepository.findByChamadoId(chamadoId);
            if (backlogItem.isPresent()) {
                backlogRepository.delete(backlogItem.get());
                logger.info("Chamado {} removido do backlog", chamadoId);
                
                // Reajusta posições da fila
                reajustarPosicoesFila();
            }
        } catch (Exception e) {
            logger.error("Erro ao remover chamado {} do backlog: {}", chamadoId, e.getMessage());
        }
    }

    /**
     * Obtém próximo chamado para atendimento
     */
    public Optional<BacklogChamado> obterProximoChamado() {
        try {
            return backlogRepository.findProximoChamadoParaAtendimento();
        } catch (Exception e) {
            logger.error("Erro ao obter próximo chamado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lista backlog completo ordenado por prioridade
     */
    public List<BacklogChamado> listarBacklogOrdenado() {
        try {
            return backlogRepository.findAllOrderedByPriority();
        } catch (Exception e) {
            logger.error("Erro ao listar backlog: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtém próximos N chamados da fila
     */
    public List<BacklogChamado> obterProximosChamados(int limite) {
        try {
            return backlogRepository.findProximosChamados(limite);
        } catch (Exception e) {
            logger.error("Erro ao obter próximos chamados: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Conta total de chamados aguardando atendimento
     */
    public Long contarTotalBacklog() {
        try {
            return backlogRepository.countTotalBacklog();
        } catch (Exception e) {
            logger.error("Erro ao contar backlog: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Obtém estatísticas detalhadas do backlog
     */
    public Map<String, Object> obterEstatisticasBacklog() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Estatísticas básicas
            Object[] estatisticas = backlogRepository.getEstatisticasBacklog();
            if (estatisticas != null && estatisticas.length >= 5) {
                stats.put("totalChamados", estatisticas[0]);
                stats.put("scoreMedio", estatisticas[1]);
                stats.put("scoreMaximo", estatisticas[2]);
                stats.put("scoreMinimo", estatisticas[3]);
                stats.put("tempoMedioEspera", estatisticas[4]);
            }
            
            // Distribuição por categoria de urgência
            List<Object[]> porUrgencia = backlogRepository.countByCategoriaUrgencia();
            Map<String, Long> urgenciaMap = porUrgencia.stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
            stats.put("porUrgencia", urgenciaMap);
            
            // Distribuição por complexidade
            List<Object[]> porComplexidade = backlogRepository.countByComplexidade();
            Map<String, Long> complexidadeMap = porComplexidade.stream()
                .collect(Collectors.toMap(
                    arr -> arr[0].toString(),
                    arr -> (Long) arr[1]
                ));
            stats.put("porComplexidade", complexidadeMap);
            
            // Chamados críticos
            stats.put("chamadosSlaCritico", backlogRepository.findChamadosSlaCritico().size());
            stats.put("chamadosClienteVip", backlogRepository.findChamadosClienteVip().size());
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas do backlog: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Filtra backlog por critérios específicos
     */
    public List<BacklogChamado> filtrarBacklog(String filtro, String valor) {
        try {
            switch (filtro.toLowerCase()) {
                case "urgencia":
                    BacklogChamado.CategoriaUrgencia urgencia = 
                        BacklogChamado.CategoriaUrgencia.valueOf(valor.toUpperCase());
                    return backlogRepository.findByCategoriaUrgencia(urgencia);
                    
                case "complexidade":
                    BacklogChamado.ComplexidadeEstimada complexidade = 
                        BacklogChamado.ComplexidadeEstimada.valueOf(valor.toUpperCase());
                    return backlogRepository.findByComplexidade(complexidade);
                    
                case "impacto":
                    BacklogChamado.ImpactoNegocio impacto = 
                        BacklogChamado.ImpactoNegocio.valueOf(valor.toUpperCase());
                    return backlogRepository.findByImpactoNegocio(impacto);
                    
                case "tecnico":
                    return backlogRepository.findByTecnicoSugerido(valor);
                    
                case "sla_critico":
                    return backlogRepository.findChamadosSlaCritico();
                    
                case "cliente_vip":
                    return backlogRepository.findChamadosClienteVip();
                    
                default:
                    return listarBacklogOrdenado();
            }
        } catch (Exception e) {
            logger.error("Erro ao filtrar backlog: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Recalcula prioridades do backlog (executado periodicamente)
     */
    @Scheduled(fixedRate = 300000) // A cada 5 minutos
    public void recalcularPrioridades() {
        try {
            List<BacklogChamado> backlog = backlogRepository.findAll();
            
            for (BacklogChamado item : backlog) {
                // Atualiza tempo de espera
                item.atualizarTempoEspera();
                
                // Recalcula score considerando novo tempo de espera
                calcularPriorizacao(item);
                
                backlogRepository.save(item);
            }
            
            // Reajusta posições da fila
            reajustarPosicoesFila();
            
            logger.info("Prioridades do backlog recalculadas para {} itens", backlog.size());
            
        } catch (Exception e) {
            logger.error("Erro ao recalcular prioridades: {}", e.getMessage());
        }
    }

    /**
     * Calcula priorização automática baseada em múltiplos fatores
     */
    private void calcularPriorizacao(BacklogChamado backlogItem) {
        Chamado chamado = backlogItem.getChamado();
        
        // Determina categoria de urgência
        backlogItem.determinarCategoriaUrgencia();
        
        // Estima complexidade
        backlogItem.estimarComplexidade();
        
        // Calcula impacto no negócio
        backlogItem.calcularImpactoNegocio();
        
        // Verifica se é SLA crítico
        verificarSlaCritico(backlogItem);
        
        // Verifica se é cliente VIP
        verificarClienteVip(backlogItem);
        
        // Calcula score final
        backlogItem.calcularScore();
    }

    /**
     * Define posição na fila baseada no score
     */
    private void definirPosicaoFila(BacklogChamado backlogItem) {
        List<BacklogChamado> backlogOrdenado = backlogRepository.findAllOrderedByPriority();
        
        int posicao = 1;
        for (BacklogChamado item : backlogOrdenado) {
            if (item.getScorePrioridade() > backlogItem.getScorePrioridade()) {
                posicao++;
            } else {
                break;
            }
        }
        
        backlogItem.setPosicaoFila(posicao);
    }

    /**
     * Sugere técnico baseado em expertise e disponibilidade
     */
    private void sugerirTecnico(BacklogChamado backlogItem) {
        Chamado chamado = backlogItem.getChamado();
        
        // Lógica simplificada - pode ser expandida com IA
        String categoria = chamado.getCategoria();
        String tecnicoSugerido = "Técnico Geral";
        
        if (categoria != null) {
            switch (categoria.toLowerCase()) {
                case "hardware":
                case "infraestrutura":
                    tecnicoSugerido = "Técnico Hardware";
                    break;
                case "software":
                case "sistema":
                    tecnicoSugerido = "Técnico Software";
                    break;
                case "rede":
                case "conectividade":
                    tecnicoSugerido = "Técnico Redes";
                    break;
                case "segurança":
                    tecnicoSugerido = "Especialista Segurança";
                    break;
            }
        }
        
        backlogItem.setTecnicoSugerido(tecnicoSugerido);
    }

    /**
     * Estima tempo de atendimento baseado na complexidade
     */
    private void estimarTempoAtendimento(BacklogChamado backlogItem) {
        LocalDateTime agora = LocalDateTime.now();
        int minutosEstimados = 30; // Padrão
        
        switch (backlogItem.getComplexidadeEstimada()) {
            case BAIXA:
                minutosEstimados = 15;
                break;
            case MEDIA:
                minutosEstimados = 45;
                break;
            case ALTA:
                minutosEstimados = 120;
                break;
            case CRITICA:
                minutosEstimados = 240;
                break;
        }
        
        // Considera fila atual
        Long posicaoFila = (long) backlogItem.getPosicaoFila();
        minutosEstimados += (int) (posicaoFila * 10); // 10 min por posição
        
        backlogItem.setEstimativaAtendimento(agora.plusMinutes(minutosEstimados));
    }

    /**
     * Verifica se o chamado tem SLA crítico
     */
    private void verificarSlaCritico(BacklogChamado backlogItem) {
        Chamado chamado = backlogItem.getChamado();
        
        // Lógica para determinar SLA crítico
        boolean slaCritico = false;
        
        if (chamado.getPrioridade() != null) {
            slaCritico = chamado.getPrioridade().equalsIgnoreCase("ALTA") ||
                        chamado.getPrioridade().equalsIgnoreCase("CRÍTICA");
        }
        
        // Verifica tempo desde criação
        if (chamado.getDataCriacao() != null) {
            long horasEspera = ChronoUnit.HOURS.between(chamado.getDataCriacao(), LocalDateTime.now());
            if (horasEspera > 4) { // Mais de 4 horas = crítico
                slaCritico = true;
            }
        }
        
        backlogItem.setSlaCritico(slaCritico);
    }

    /**
     * Verifica se é cliente VIP
     */
    private void verificarClienteVip(BacklogChamado backlogItem) {
        Chamado chamado = backlogItem.getChamado();
        
        // Lógica simplificada - pode integrar com sistema de CRM
        boolean clienteVip = false;
        
        if (chamado.getUsuario() != null) {
            String usuario = chamado.getUsuario().toLowerCase();
            // Lista de usuários VIP (pode vir de banco de dados)
            clienteVip = usuario.contains("diretor") || 
                        usuario.contains("gerente") ||
                        usuario.contains("vip");
        }
        
        backlogItem.setClienteVip(clienteVip);
    }

    /**
     * Reajusta posições da fila após mudanças
     */
    private void reajustarPosicoesFila() {
        List<BacklogChamado> backlogOrdenado = backlogRepository.findAllOrderedByPriority();
        
        for (int i = 0; i < backlogOrdenado.size(); i++) {
            BacklogChamado item = backlogOrdenado.get(i);
            item.setPosicaoFila(i + 1);
            backlogRepository.save(item);
        }
    }

    /**
     * Identifica chamados que precisam de reavaliação
     */
    public List<BacklogChamado> identificarChamadosParaReavaliacao() {
        LocalDateTime dataLimite = LocalDateTime.now().minusHours(2);
        return backlogRepository.findChamadosParaReavaliacao(dataLimite);
    }

    /**
     * Obtém métricas de performance do backlog
     */
    public Map<String, Object> obterMetricasPerformance() {
        Map<String, Object> metricas = new HashMap<>();
        
        try {
            // Tempo médio de espera
            Double tempoMedio = backlogRepository.calcularTempoMedioEspera();
            metricas.put("tempoMedioEspera", tempoMedio != null ? tempoMedio : 0.0);
            
            // Chamados com estimativa vencida
            List<BacklogChamado> estimativaVencida = 
                backlogRepository.findChamadosComEstimativaVencida(LocalDateTime.now());
            metricas.put("chamadosAtrasados", estimativaVencida.size());
            
            // Distribuição por faixas de score
            metricas.put("scoreAlto", backlogRepository.findByFaixaScore(80.0, 100.0).size());
            metricas.put("scoreMedio", backlogRepository.findByFaixaScore(50.0, 79.9).size());
            metricas.put("scoreBaixo", backlogRepository.findByFaixaScore(0.0, 49.9).size());
            
        } catch (Exception e) {
            logger.error("Erro ao obter métricas de performance: {}", e.getMessage());
        }
        
        return metricas;
    }
}