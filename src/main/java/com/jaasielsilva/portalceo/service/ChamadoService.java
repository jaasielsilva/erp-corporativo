package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import com.jaasielsilva.portalceo.model.Chamado.Prioridade;
import com.jaasielsilva.portalceo.repository.ChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChamadoService {

    private static final Logger logger = LoggerFactory.getLogger(ChamadoService.class);

    @Autowired
    private ChamadoRepository chamadoRepository;

    // Criar novo chamado
    public Chamado criarChamado(Chamado chamado) {
        // Gerar número único se não foi definido
        if (chamado.getNumero() == null || chamado.getNumero().isEmpty()) {
            chamado.setNumero(gerarProximoNumero());
        }
        
        // Definir data de abertura se não foi definida
        if (chamado.getDataAbertura() == null) {
            chamado.setDataAbertura(LocalDateTime.now());
        }
        
        // Definir status inicial
        if (chamado.getStatus() == null) {
            chamado.setStatus(StatusChamado.ABERTO);
        }
        
        // Calcular e definir SLA de vencimento
        chamado.setSlaVencimento(calcularSlaVencimento(chamado));
        
        logger.info("Criando novo chamado: {} - SLA: {}", chamado.getAssunto(), chamado.getSlaVencimento());
        return chamadoRepository.save(chamado);
    }

    // Buscar chamado por ID
    @Transactional(readOnly = true)
    public Optional<Chamado> buscarPorId(Long id) {
        Optional<Chamado> chamado = chamadoRepository.findById(id);
        if (chamado.isPresent()) {
            Chamado c = chamado.get();
            c.setSlaRestante(calcularSlaRestante(c));
        }
        return chamado;
    }

    // Buscar chamado por número
    @Transactional(readOnly = true)
    public Optional<Chamado> buscarPorNumero(String numero) {
        Optional<Chamado> chamado = chamadoRepository.findByNumero(numero);
        if (chamado.isPresent()) {
            Chamado c = chamado.get();
            c.setSlaRestante(calcularSlaRestante(c));
        }
        return chamado;
    }

    // Listar todos os chamados
    @Transactional(readOnly = true)
    public List<Chamado> listarTodos() {
        List<Chamado> chamados = chamadoRepository.findAll();
        // Calcular SLA restante para cada chamado
        chamados.forEach(chamado -> chamado.setSlaRestante(calcularSlaRestante(chamado)));
        return chamados;
    }

    // Listar chamados abertos
    @Transactional(readOnly = true)
    public List<Chamado> listarAbertos() {
        List<Chamado> chamados = chamadoRepository.findChamadosAbertos();
        chamados.forEach(chamado -> chamado.setSlaRestante(calcularSlaRestante(chamado)));
        return chamados;
    }

    // Listar chamados em andamento
    @Transactional(readOnly = true)
    public List<Chamado> listarEmAndamento() {
        List<Chamado> chamados = chamadoRepository.findChamadosEmAndamento();
        chamados.forEach(chamado -> chamado.setSlaRestante(calcularSlaRestante(chamado)));
        return chamados;
    }

    // Listar chamados resolvidos
    @Transactional(readOnly = true)
    public List<Chamado> listarResolvidos() {
        return chamadoRepository.findChamadosResolvidos();
    }
    
    // Buscar chamados que foram avaliados
    @Transactional(readOnly = true)
    public List<Chamado> buscarChamadosAvaliados() {
        return chamadoRepository.findChamadosAvaliados();
    }

    // Listar últimos chamados para dashboard
    @Transactional(readOnly = true)
    public List<Chamado> listarUltimosChamados(int limite) {
        Pageable pageable = PageRequest.of(0, limite);
        List<Chamado> chamados = chamadoRepository.findUltimosChamados();
        
        // Limitar resultados e calcular SLA
        List<Chamado> chamadosLimitados = chamados.stream()
                .limit(limite)
                .peek(chamado -> chamado.setSlaRestante(calcularSlaRestante(chamado)))
                .toList();
                
        return chamadosLimitados;
    }

    // Atualizar chamado
    public Chamado atualizarChamado(Chamado chamado) {
        logger.info("Atualizando chamado: {}", chamado.getNumero());
        
        Chamado chamadoAtualizado = chamadoRepository.save(chamado);
        chamadoAtualizado.setSlaRestante(calcularSlaRestante(chamadoAtualizado));
        
        return chamadoAtualizado;
    }

    // Iniciar atendimento
    public Chamado iniciarAtendimento(Long id, String tecnicoResponsavel) {
        Optional<Chamado> chamadoOpt = chamadoRepository.findById(id);
        
        if (chamadoOpt.isPresent()) {
            Chamado chamado = chamadoOpt.get();
            chamado.iniciarAtendimento(tecnicoResponsavel);
            
            logger.info("Atendimento iniciado para chamado {} por {}", chamado.getNumero(), tecnicoResponsavel);
            return atualizarChamado(chamado);
        }
        
        throw new RuntimeException("Chamado não encontrado com ID: " + id);
    }

    // Resolver chamado
    public Chamado resolverChamado(Long id) {
        Optional<Chamado> chamadoOpt = chamadoRepository.findById(id);
        
        if (chamadoOpt.isPresent()) {
            Chamado chamado = chamadoOpt.get();
            chamado.resolver();
            
            logger.info("Chamado {} resolvido", chamado.getNumero());
            return atualizarChamado(chamado);
        }
        
        throw new RuntimeException("Chamado não encontrado com ID: " + id);
    }

    // Fechar chamado
    public Chamado fecharChamado(Long id) {
        Optional<Chamado> chamadoOpt = chamadoRepository.findById(id);
        
        if (chamadoOpt.isPresent()) {
            Chamado chamado = chamadoOpt.get();
            chamado.fechar();
            
            logger.info("Chamado {} fechado", chamado.getNumero());
            return atualizarChamado(chamado);
        }
        
        throw new RuntimeException("Chamado não encontrado com ID: " + id);
    }

    // Calcular SLA de vencimento baseado na prioridade
    public LocalDateTime calcularSlaVencimento(Chamado chamado) {
        if (chamado.getDataAbertura() == null || chamado.getPrioridade() == null) {
            return null;
        }
        
        int horasUteis = chamado.getPrioridade().getHorasUteis();
        return chamado.getDataAbertura().plusHours(horasUteis);
    }
    
    // MÉTODO PRINCIPAL: Calcular SLA restante em horas
    @Transactional(readOnly = true)
    public Long calcularSlaRestante(Chamado chamado) {
        if (chamado == null || chamado.getSlaVencimento() == null) {
            return 0L;
        }
        
        // Se o chamado já foi resolvido ou fechado, SLA não se aplica
        if (chamado.isResolvido() || chamado.isFechado()) {
            return 0L;
        }
        
        try {
            LocalDateTime agora = LocalDateTime.now();
            
            // Calcular minutos restantes até o vencimento e converter para horas
            long minutosRestantes = ChronoUnit.MINUTES.between(agora, chamado.getSlaVencimento());
            long horasRestantes = minutosRestantes / 60;
            
            logger.debug("Chamado {}: {} minutos ({} horas) restantes até vencimento", 
                        chamado.getNumero(), minutosRestantes, horasRestantes);
            
            return horasRestantes;
            
        } catch (Exception e) {
            logger.error("Erro ao calcular SLA restante para chamado {}: {}", chamado.getNumero(), e.getMessage());
            return 0L;
        }
    }

    // MÉTODO PRINCIPAL: Calcular SLA médio de todos os chamados resolvidos
    @Transactional(readOnly = true)
    public Double calcularSlaMedio() {
        try {
            Double tempoMedio = chamadoRepository.calcularTempoMedioResolucaoEmHoras();
            
            if (tempoMedio == null) {
                logger.info("Nenhum chamado resolvido encontrado para calcular SLA médio");
                return 0.0;
            }
            
            logger.info("SLA médio calculado: {} horas", tempoMedio);
            return Math.round(tempoMedio * 100.0) / 100.0; // Arredondar para 2 casas decimais
            
        } catch (Exception e) {
            logger.error("Erro ao calcular SLA médio: {}", e.getMessage());
            return 0.0;
        }
    }
    
    public Double calcularAvaliacaoMedia() {
        return chamadoRepository.calcularAvaliacaoMedia();
    }

    // Método auxiliar para calcular horas úteis entre duas datas
    private long calcularHorasUteisDecorridas(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio.isAfter(fim)) {
            return 0L;
        }
        
        long horasUteis = 0L;
        LocalDateTime atual = inicio;
        
        while (atual.isBefore(fim)) {
            // Verificar se é dia útil (segunda a sexta)
            if (atual.getDayOfWeek() != DayOfWeek.SATURDAY && atual.getDayOfWeek() != DayOfWeek.SUNDAY) {
                // Verificar se está no horário comercial (8h às 18h)
                int hora = atual.getHour();
                if (hora >= 8 && hora < 18) {
                    horasUteis++;
                }
            }
            atual = atual.plusHours(1);
        }
        
        return horasUteis;
    }

    // Obter estatísticas para dashboard
    @Transactional(readOnly = true)
    public Object[] getEstatisticasDashboard() {
        return chamadoRepository.getEstatisticasDashboard();
    }

    // Contar chamados por status
    @Transactional(readOnly = true)
    public long contarPorStatus(StatusChamado status) {
        return chamadoRepository.countByStatus(status);
    }

    // Contar chamados por prioridade
    @Transactional(readOnly = true)
    public long contarPorPrioridade(Prioridade prioridade) {
        return chamadoRepository.countByPrioridade(prioridade);
    }

    // Buscar chamados com SLA próximo do vencimento
    @Transactional(readOnly = true)
    public List<Chamado> buscarChamadosComSlaProximoVencimento() {
        List<Chamado> chamados = chamadoRepository.findChamadosComSlaProximoVencimento();
        chamados.forEach(chamado -> chamado.setSlaRestante(calcularSlaRestante(chamado)));
        return chamados;
    }

    // Buscar chamados com SLA vencido
    @Transactional(readOnly = true)
    public List<Chamado> buscarChamadosComSlaVencido() {
        List<Chamado> chamados = chamadoRepository.findChamadosComSlaVencido();
        chamados.forEach(chamado -> chamado.setSlaRestante(calcularSlaRestante(chamado)));
        return chamados;
    }

    // Obter estatísticas por prioridade
    @Transactional(readOnly = true)
    public List<Object[]> getEstatisticasPorPrioridade() {
        return chamadoRepository.countByPrioridadeGrouped();
    }

    // Obter estatísticas por status
    @Transactional(readOnly = true)
    public List<Object[]> getEstatisticasPorStatus() {
        return chamadoRepository.countByStatusGrouped();
    }

    // Obter estatísticas por categoria
    @Transactional(readOnly = true)
    public List<Object[]> getEstatisticasPorCategoria() {
        return chamadoRepository.countByCategoriaGrouped();
    }

    // Deletar chamado
    public void deletarChamado(Long id) {
        Optional<Chamado> chamadoOpt = chamadoRepository.findById(id);
        
        if (chamadoOpt.isPresent()) {
            chamadoRepository.deleteById(id);
            logger.info("Chamado {} deletado", chamadoOpt.get().getNumero());
        } else {
            throw new RuntimeException("Chamado não encontrado com ID: " + id);
        }
    }
    
    // Gerar próximo número de chamado
    public String gerarProximoNumero() {
        return "CH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    
    // Obter dados de evolução de chamados dos últimos 12 meses
    @Transactional(readOnly = true)
    public List<Long> obterEvolucaoChamadosUltimosMeses(int meses) {
        List<Long> dadosMeses = new ArrayList<>();
        LocalDate agora = LocalDate.now();
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate mesReferencia = agora.minusMonths(i);
            LocalDateTime inicioMes = mesReferencia.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimMes = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth()).atTime(23, 59, 59);
            
            try {
                Long count = chamadoRepository.countByDataAberturaBetween(inicioMes, fimMes);
                dadosMeses.add(count != null ? count : 0L);
                logger.debug("Chamados em {}/{}: {}", mesReferencia.getMonthValue(), mesReferencia.getYear(), count);
            } catch (Exception e) {
                logger.error("Erro ao obter dados do mês {}/{}: {}", mesReferencia.getMonthValue(), mesReferencia.getYear(), e.getMessage());
                dadosMeses.add(0L);
            }
        }
        
        return dadosMeses;
    }
    
    // Obter labels dos últimos 12 meses
    @Transactional(readOnly = true)
    public List<String> obterLabelsUltimosMeses(int meses) {
        List<String> labels = new ArrayList<>();
        LocalDate agora = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", java.util.Locale.forLanguageTag("pt-BR"));
        
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate mesReferencia = agora.minusMonths(i);
            String label = mesReferencia.format(formatter);
            // Capitalizar primeira letra
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            labels.add(label);
        }
        
        return labels;
    }
    
    // Calcular meta mensal de chamados baseada em histórico e crescimento
    @Transactional(readOnly = true)
    public List<Long> calcularMetaMensalChamados(int meses) {
        List<Long> metas = new ArrayList<>();
        List<Long> dadosHistoricos = obterEvolucaoChamadosUltimosMeses(meses);
        
        // Calcular média dos últimos 3 meses para base da meta
        long somaUltimos3Meses = 0;
        int countUltimos3 = 0;
        for (int i = Math.max(0, dadosHistoricos.size() - 3); i < dadosHistoricos.size(); i++) {
            somaUltimos3Meses += dadosHistoricos.get(i);
            countUltimos3++;
        }
        
        long mediaBase = countUltimos3 > 0 ? somaUltimos3Meses / countUltimos3 : 50;
        
        // Definir meta com crescimento gradual de 5% ao mês
        for (int i = 0; i < meses; i++) {
            // Meta inicial baseada na média + 20% de margem
            long metaBase = Math.round(mediaBase * 1.2);
            
            // Crescimento de 2% por mês para acompanhar expansão do negócio
            double fatorCrescimento = Math.pow(1.02, i);
            long meta = Math.round(metaBase * fatorCrescimento);
            
            // Garantir meta mínima de 30 chamados
            meta = Math.max(meta, 30);
            
            metas.add(meta);
        }
        
        return metas;
    }
    
    // ===== MÉTODOS DE TEMPO DE RESOLUÇÃO =====
    
    /**
     * Calcula o tempo médio de resolução geral em horas
     */
    @Transactional(readOnly = true)
    public Double calcularTempoMedioResolucaoGeral() {
        try {
            Double tempoMedio = chamadoRepository.calcularTempoMedioResolucaoEmHoras();
            return tempoMedio != null ? tempoMedio : 0.0;
        } catch (Exception e) {
            logger.error("Erro ao calcular tempo médio de resolução geral: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calcula tempo médio de resolução por prioridade
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Double> calcularTempoMedioResolucaoPorPrioridade() {
        java.util.Map<String, Double> temposPorPrioridade = new java.util.HashMap<>();
        
        for (Prioridade prioridade : Prioridade.values()) {
            try {
                List<Chamado> chamadosResolvidos = chamadoRepository.findByPrioridade(prioridade)
                    .stream()
                    .filter(c -> c.getStatus() == StatusChamado.RESOLVIDO || c.getStatus() == StatusChamado.FECHADO)
                    .filter(c -> c.getDataResolucao() != null)
                    .collect(java.util.stream.Collectors.toList());
                
                if (!chamadosResolvidos.isEmpty()) {
                    double somaHoras = chamadosResolvidos.stream()
                        .mapToDouble(c -> java.time.Duration.between(c.getDataAbertura(), c.getDataResolucao()).toHours())
                        .sum();
                    
                    double media = somaHoras / chamadosResolvidos.size();
                    temposPorPrioridade.put(prioridade.getDescricao(), media);
                } else {
                    temposPorPrioridade.put(prioridade.getDescricao(), 0.0);
                }
            } catch (Exception e) {
                logger.error("Erro ao calcular tempo médio para prioridade {}: {}", prioridade, e.getMessage());
                temposPorPrioridade.put(prioridade.getDescricao(), 0.0);
            }
        }
        
        return temposPorPrioridade;
    }
    
    /**
     * Calcula tempo médio de resolução por categoria
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Double> calcularTempoMedioResolucaoPorCategoria() {
        java.util.Map<String, Double> temposPorCategoria = new java.util.HashMap<>();
        
        try {
            List<Object[]> categorias = chamadoRepository.countByCategoriaGrouped();
            
            for (Object[] categoria : categorias) {
                String nomeCategoria = (String) categoria[0];
                
                List<Chamado> chamadosCategoria = chamadoRepository.findByCategoria(nomeCategoria)
                    .stream()
                    .filter(c -> c.getStatus() == StatusChamado.RESOLVIDO || c.getStatus() == StatusChamado.FECHADO)
                    .filter(c -> c.getDataResolucao() != null)
                    .collect(java.util.stream.Collectors.toList());
                
                if (!chamadosCategoria.isEmpty()) {
                    double somaHoras = chamadosCategoria.stream()
                        .mapToDouble(c -> java.time.Duration.between(c.getDataAbertura(), c.getDataResolucao()).toHours())
                        .sum();
                    
                    double media = somaHoras / chamadosCategoria.size();
                    temposPorCategoria.put(nomeCategoria, media);
                } else {
                    temposPorCategoria.put(nomeCategoria, 0.0);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao calcular tempo médio por categoria: {}", e.getMessage());
        }
        
        return temposPorCategoria;
    }
    
    /**
     * Calcula métricas de SLA (percentual de cumprimento) - apenas chamados fechados
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> calcularMetricasSLA() {
        return calcularMetricasSLAPorPeriodo(null, null);
    }
    
    /**
     * Calcula métricas de SLA por período específico - apenas chamados fechados
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> calcularMetricasSLAPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        java.util.Map<String, Object> metricasSLA = new java.util.HashMap<>();
        
        try {
            // Buscar apenas chamados FECHADOS (não apenas resolvidos)
            List<Chamado> chamadosFechados;
            
            if (dataInicio != null && dataFim != null) {
                chamadosFechados = chamadoRepository.findByStatusAndDataFechamentoBetween(
                    StatusChamado.FECHADO, dataInicio, dataFim);
            } else {
                chamadosFechados = chamadoRepository.findByStatus(StatusChamado.FECHADO);
            }
            
            if (chamadosFechados.isEmpty()) {
                metricasSLA.put("percentualSLACumprido", 0.0);
                metricasSLA.put("totalChamados", 0);
                metricasSLA.put("chamadosNoPrazo", 0);
                metricasSLA.put("chamadosForaPrazo", 0);
                metricasSLA.put("slaMedioHoras", 0.0);
                return metricasSLA;
            }
            
            int chamadosNoPrazo = 0;
            int totalChamados = chamadosFechados.size();
            double somaTempoResolucao = 0.0;
            
            for (Chamado chamado : chamadosFechados) {
                if (chamado.getDataFechamento() != null && chamado.getDataAbertura() != null) {
                    LocalDateTime slaVencimento = calcularSlaVencimento(chamado);
                    
                    // Verificar se foi resolvido dentro do SLA
                    if (slaVencimento != null && chamado.getDataFechamento().isBefore(slaVencimento)) {
                        chamadosNoPrazo++;
                    }
                    
                    // Calcular tempo de resolução em horas
                    long minutosResolucao = ChronoUnit.MINUTES.between(
                        chamado.getDataAbertura(), chamado.getDataFechamento());
                    somaTempoResolucao += minutosResolucao / 60.0;
                }
            }
            
            double percentualSLA = totalChamados > 0 ? (double) chamadosNoPrazo / totalChamados * 100 : 0.0;
            double slaMedioHoras = totalChamados > 0 ? somaTempoResolucao / totalChamados : 0.0;
            
            metricasSLA.put("percentualSLACumprido", Math.round(percentualSLA * 100.0) / 100.0);
            metricasSLA.put("totalChamados", totalChamados);
            metricasSLA.put("chamadosNoPrazo", chamadosNoPrazo);
            metricasSLA.put("chamadosForaPrazo", totalChamados - chamadosNoPrazo);
            metricasSLA.put("slaMedioHoras", Math.round(slaMedioHoras * 100.0) / 100.0);
            
        } catch (Exception e) {
            logger.error("Erro ao calcular métricas de SLA: {}", e.getMessage());
            metricasSLA.put("percentualSLACumprido", 0.0);
            metricasSLA.put("totalChamados", 0);
            metricasSLA.put("chamadosNoPrazo", 0);
            metricasSLA.put("chamadosForaPrazo", 0);
            metricasSLA.put("slaMedioHoras", 0.0);
        }
        
        return metricasSLA;
    }
    
    /**
     * Calcula métricas de SLA para os últimos N dias
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> calcularMetricasSLAUltimosDias(int dias) {
        LocalDateTime dataFim = LocalDateTime.now();
        LocalDateTime dataInicio = dataFim.minusDays(dias);
        return calcularMetricasSLAPorPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Obtém dados de tempo de resolução dos últimos 12 meses para gráfico
     */
    @Transactional(readOnly = true)
    public List<Double> obterTempoMedioResolucaoUltimosMeses() {
        List<Double> temposResolucao = new ArrayList<>();
        
        // Configuração de período - últimos 12 meses (padrão ERP)
        final int PERIODO_MESES = 12;
        LocalDate dataReferencia = LocalDate.now();
        
        logger.info("=== INICIANDO ANÁLISE DE TEMPO MÉDIO DE RESOLUÇÃO ===");
        logger.info("Data de referência: {}", dataReferencia);
        
        try {
            // Buscar todos os chamados resolvidos (otimização: uma única consulta)
            List<Chamado> todosResolvidos = chamadoRepository.findChamadosResolvidos();
            logger.info("Total de chamados resolvidos encontrados: {}", todosResolvidos.size());
            
            // Log dos chamados para debug
            todosResolvidos.forEach(c -> 
                logger.debug("Chamado {}: Abertura={}, Resolução={}, Status={}", 
                    c.getNumero(), c.getDataAbertura(), c.getDataResolucao(), c.getStatus())
            );
            
            // Processar cada mês dos últimos 12 meses (do mais antigo para o mais recente)
            for (int i = PERIODO_MESES - 1; i >= 0; i--) {
                LocalDate mesReferencia = dataReferencia.minusMonths(i);
                
                // Definir período do mês com precisão (padrão ERP corporativo)
                LocalDateTime inicioMes = mesReferencia
                    .withDayOfMonth(1)
                    .atStartOfDay();
                    
                LocalDateTime fimMes = mesReferencia
                    .withDayOfMonth(mesReferencia.lengthOfMonth())
                    .atTime(23, 59, 59, 999_999_999); // Máxima precisão
                
                logger.info("Processando período: {}/{} - {} até {}", 
                    mesReferencia.getMonthValue(), mesReferencia.getYear(), 
                    inicioMes, fimMes);
                
                // Filtrar chamados do período usando lógica robusta de intervalo
                List<Chamado> chamadosDoMes = todosResolvidos.stream()
                    .filter(chamado -> chamado.getDataResolucao() != null)
                    .filter(chamado -> {
                        LocalDateTime dataResolucao = chamado.getDataResolucao();
                        // Lógica de intervalo inclusivo (padrão ERP)
                        boolean dentroDoIntervalo = 
                            (dataResolucao.isEqual(inicioMes) || dataResolucao.isAfter(inicioMes)) &&
                            (dataResolucao.isEqual(fimMes) || dataResolucao.isBefore(fimMes));
                        
                        if (dentroDoIntervalo) {
                            logger.debug("✓ Chamado {} incluído no período {}/{}", 
                                chamado.getNumero(), mesReferencia.getMonthValue(), mesReferencia.getYear());
                        }
                        
                        return dentroDoIntervalo;
                    })
                    .filter(chamado -> 
                        chamado.getStatus() == StatusChamado.RESOLVIDO || 
                        chamado.getStatus() == StatusChamado.FECHADO
                    )
                    .collect(Collectors.toList());
                
                // Calcular tempo médio de resolução do período
                double tempoMedioMes = calcularTempoMedioResolucao(chamadosDoMes, mesReferencia);
                temposResolucao.add(tempoMedioMes);
                
                logger.info("Resultado mês {}/{}: {} chamados, tempo médio: {} horas", 
                    mesReferencia.getMonthValue(), mesReferencia.getYear(), 
                    chamadosDoMes.size(), tempoMedioMes);
            }
            
        } catch (Exception e) {
            logger.error("Erro crítico no cálculo de tempo médio de resolução", e);
            // Em caso de erro, retornar array com zeros (comportamento seguro)
            for (int i = 0; i < PERIODO_MESES; i++) {
                temposResolucao.add(0.0);
            }
        }
        
        logger.info("=== ANÁLISE CONCLUÍDA ===");
        logger.info("Tempos de resolução por mês: {}", temposResolucao);
        
        return temposResolucao;
    }
    
    /**
     * Calcula o tempo médio de resolução para uma lista de chamados
     * Implementação robusta com tratamento de edge cases (padrão ERP)
     */
    private double calcularTempoMedioResolucao(List<Chamado> chamados, LocalDate mesReferencia) {
        if (chamados == null || chamados.isEmpty()) {
            logger.debug("Nenhum chamado encontrado para o mês {}/{}", 
                mesReferencia.getMonthValue(), mesReferencia.getYear());
            return 0.0;
        }
        
        try {
            // Calcular tempo de resolução para cada chamado
            List<Double> temposResolucao = chamados.stream()
                .filter(chamado -> chamado.getDataAbertura() != null && chamado.getDataResolucao() != null)
                .map(chamado -> {
                    Duration duracao = Duration.between(chamado.getDataAbertura(), chamado.getDataResolucao());
                    double horas = duracao.toMinutes() / 60.0; // Precisão em minutos convertida para horas
                    
                    logger.debug("Chamado {}: {} horas de resolução", 
                        chamado.getNumero(), String.format("%.2f", horas));
                    
                    return horas;
                })
                .filter(horas -> horas >= 0) // Filtrar valores inválidos
                .collect(Collectors.toList());
            
            if (temposResolucao.isEmpty()) {
                return 0.0;
            }
            
            // Calcular média com precisão decimal (padrão financeiro ERP)
            double soma = temposResolucao.stream().mapToDouble(Double::doubleValue).sum();
            double media = soma / temposResolucao.size();
            
            // Arredondar para 2 casas decimais (padrão corporativo)
            return Math.round(media * 100.0) / 100.0;
            
        } catch (Exception e) {
            logger.error("Erro ao calcular tempo médio para o mês {}/{}: {}", 
                mesReferencia.getMonthValue(), mesReferencia.getYear(), e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calcula meta de tempo de resolução baseada no SLA por prioridade
     */
    @Transactional(readOnly = true)
    public List<Double> calcularMetaTempoResolucao() {
        List<Double> metasResolucao = new ArrayList<>();
        
        // Meta baseada na média ponderada dos SLAs por prioridade
        // Considerando distribuição típica: 20% Urgente, 30% Alta, 35% Média, 15% Baixa
        double metaMedia = (Prioridade.URGENTE.getHorasUteis() * 0.2 +
                           Prioridade.ALTA.getHorasUteis() * 0.3 +
                           Prioridade.MEDIA.getHorasUteis() * 0.35 +
                           Prioridade.BAIXA.getHorasUteis() * 0.15) * 0.8; // 80% do SLA como meta
        
        // Aplicar a mesma meta para todos os meses (pode ser refinado posteriormente)
        for (int i = 0; i < 12; i++) {
            metasResolucao.add(Math.round(metaMedia * 100.0) / 100.0);
        }
        
        return metasResolucao;
    }
    
    /**
     * Calcula tempo médio de primeira resposta em horas
     */
    @Transactional(readOnly = true)
    public Double calcularTempoMedioPrimeiraResposta() {
        try {
            List<Chamado> chamadosComAtendimento = chamadoRepository.findAll()
                .stream()
                .filter(c -> c.getDataInicioAtendimento() != null)
                .collect(java.util.stream.Collectors.toList());
            
            if (chamadosComAtendimento.isEmpty()) {
                return 0.0;
            }
            
            double somaHoras = chamadosComAtendimento.stream()
                .mapToDouble(c -> java.time.Duration.between(c.getDataAbertura(), c.getDataInicioAtendimento()).toHours())
                .sum();
            
            double media = somaHoras / chamadosComAtendimento.size();
            return Math.round(media * 100.0) / 100.0;
            
        } catch (Exception e) {
            logger.error("Erro ao calcular tempo médio de primeira resposta: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Conta o número de chamados que foram reabertos após resolução
     */
    @Transactional(readOnly = true)
    public Long contarChamadosReabertos() {
        try {
            long count = chamadoRepository.findAll()
                .stream()
                .filter(c -> c.getFoiReaberto() != null && c.getFoiReaberto())
                .count();
            
            logger.info("Total de chamados reabertos: {}", count);
            return count;
            
        } catch (Exception e) {
            logger.error("Erro ao contar chamados reabertos: {}", e.getMessage());
            return 0L;
        }
    }
}