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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChamadoService {

    private static final Logger logger = LoggerFactory.getLogger(ChamadoService.class);

    @Autowired
    private ChamadoRepository chamadoRepository;

    // Criar novo chamado
    public Chamado criarChamado(Chamado chamado) {
        logger.info("Criando novo chamado: {}", chamado.getAssunto());
        
        // Definir valores padrão se não informados
        if (chamado.getStatus() == null) {
            chamado.setStatus(StatusChamado.ABERTO);
        }
        
        if (chamado.getDataAbertura() == null) {
            chamado.setDataAbertura(LocalDateTime.now());
        }
        
        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        
        // Calcular SLA restante após salvar
        chamadoSalvo.setSlaRestante(calcularSlaRestante(chamadoSalvo));
        
        logger.info("Chamado criado com sucesso. Número: {}", chamadoSalvo.getNumero());
        return chamadoSalvo;
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

    // MÉTODO PRINCIPAL: Calcular SLA restante em horas
    @Transactional(readOnly = true)
    public Long calcularSlaRestante(Chamado chamado) {
        if (chamado == null || chamado.getDataAbertura() == null || chamado.getPrioridade() == null) {
            return 0L;
        }
        
        // Se o chamado já foi resolvido ou fechado, SLA não se aplica
        if (chamado.isResolvido() || chamado.isFechado()) {
            return 0L;
        }
        
        try {
            // Obter horas úteis do SLA baseado na prioridade
            int horasUteisSla = chamado.getPrioridade().getHorasUteis();
            
            // Calcular horas úteis decorridas desde a abertura
            long horasUteisDecorridas = calcularHorasUteisDecorridas(chamado.getDataAbertura(), LocalDateTime.now());
            
            // Calcular SLA restante
            long slaRestante = horasUteisSla - horasUteisDecorridas;
            
            logger.debug("Chamado {}: SLA {} horas, Decorridas {} horas, Restante {} horas", 
                        chamado.getNumero(), horasUteisSla, horasUteisDecorridas, slaRestante);
            
            return slaRestante;
            
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
}