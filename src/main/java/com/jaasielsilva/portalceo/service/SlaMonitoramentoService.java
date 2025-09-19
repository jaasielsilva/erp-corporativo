package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.repository.ChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SlaMonitoramentoService {

    private static final Logger logger = LoggerFactory.getLogger(SlaMonitoramentoService.class);

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private NotificacaoSuporteService notificacaoSuporteService;

    // Configurações de alerta (em horas antes do vencimento)
    private static final int ALERTA_CRITICO = 1;  // 1 hora antes
    private static final int ALERTA_AVISO = 4;    // 4 horas antes

    /**
     * Executa verificação de SLA a cada 30 minutos
     */
    @Scheduled(fixedRate = 1800000) // 30 minutos em millisegundos
    public void verificarSlasChamados() {
        try {
            logger.info("Iniciando verificação de SLAs dos chamados...");
            
            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime limiteCritico = agora.plusHours(ALERTA_CRITICO);
            LocalDateTime limiteAviso = agora.plusHours(ALERTA_AVISO);

            // Buscar chamados próximos do vencimento
            List<Chamado> chamadosCriticos = chamadoRepository.findChamadosProximosVencimentoSla(limiteCritico);
            List<Chamado> chamadosAviso = chamadoRepository.findChamadosProximosVencimentoSla(limiteAviso);

            // Processar alertas críticos (1 hora ou menos)
            for (Chamado chamado : chamadosCriticos) {
                if (chamado.getColaboradorResponsavel() != null) {
                    long horasRestantes = ChronoUnit.HOURS.between(agora, chamado.getSlaVencimento());
                    if (horasRestantes <= ALERTA_CRITICO) {
                        enviarAlertaCritico(chamado, horasRestantes);
                    }
                }
            }

            // Processar alertas de aviso (4 horas ou menos, mas mais que 1 hora)
            for (Chamado chamado : chamadosAviso) {
                if (chamado.getColaboradorResponsavel() != null) {
                    long horasRestantes = ChronoUnit.HOURS.between(agora, chamado.getSlaVencimento());
                    if (horasRestantes <= ALERTA_AVISO && horasRestantes > ALERTA_CRITICO) {
                        enviarAlerteAviso(chamado, horasRestantes);
                    }
                }
            }

            // Verificar chamados com SLA vencido
            verificarSlasVencidos();

            logger.info("Verificação de SLAs concluída. Críticos: {}, Avisos: {}", 
                       chamadosCriticos.size(), chamadosAviso.size());

        } catch (Exception e) {
            logger.error("Erro durante verificação de SLAs: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia alerta crítico para chamados próximos do vencimento
     */
    private void enviarAlertaCritico(Chamado chamado, long horasRestantes) {
        try {
            // Verificar se já foi enviado alerta crítico recentemente (evitar spam)
            if (!jaEnviouAlerteCritico(chamado)) {
                notificacaoSuporteService.notificarSlaProximoVencimento(chamado);
                marcarAlerteCriticoEnviado(chamado);
                
                logger.warn("ALERTA CRÍTICO: Chamado {} vence em {} horas - Colaborador: {}", 
                           chamado.getNumero(), horasRestantes, 
                           chamado.getColaboradorResponsavel().getNome());
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar alerta crítico para chamado {}: {}", 
                        chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Envia alerta de aviso para chamados próximos do vencimento
     */
    private void enviarAlerteAviso(Chamado chamado, long horasRestantes) {
        try {
            // Verificar se já foi enviado alerta de aviso recentemente
            if (!jaEnviouAlerteAviso(chamado)) {
                // Criar notificação menos urgente
                criarNotificacaoAviso(chamado, horasRestantes);
                marcarAlerteAvisoEnviado(chamado);
                
                logger.info("ALERTA AVISO: Chamado {} vence em {} horas - Colaborador: {}", 
                           chamado.getNumero(), horasRestantes, 
                           chamado.getColaboradorResponsavel().getNome());
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar alerta de aviso para chamado {}: {}", 
                        chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Verifica chamados com SLA já vencido
     */
    private void verificarSlasVencidos() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            List<Chamado> chamadosVencidos = chamadoRepository.findChamadosProximosVencimentoSla(agora);
            
            for (Chamado chamado : chamadosVencidos) {
                if (chamado.getSlaVencimento().isBefore(agora)) {
                    processarSlaVencido(chamado);
                }
            }
            
            if (!chamadosVencidos.isEmpty()) {
                logger.warn("Encontrados {} chamados com SLA vencido", chamadosVencidos.size());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao verificar SLAs vencidos: {}", e.getMessage());
        }
    }

    /**
     * Processa chamado com SLA vencido
     */
    private void processarSlaVencido(Chamado chamado) {
        try {
            // Marcar como SLA vencido (se houver campo específico)
            // chamado.setSlaVencido(true);
            
            // Notificar gestores sobre violação de SLA
            notificarViolacaoSla(chamado);
            
            logger.error("SLA VENCIDO: Chamado {} - Vencimento: {} - Colaborador: {}", 
                        chamado.getNumero(), chamado.getSlaVencimento(), 
                        chamado.getColaboradorResponsavel() != null ? 
                        chamado.getColaboradorResponsavel().getNome() : "Não atribuído");
                        
        } catch (Exception e) {
            logger.error("Erro ao processar SLA vencido para chamado {}: {}", 
                        chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Cria notificação de aviso menos urgente
     */
    private void criarNotificacaoAviso(Chamado chamado, long horasRestantes) {
        // Implementar notificação de aviso (menos urgente que o alerta crítico)
        // Pode ser apenas notificação interna, sem email
        logger.info("Criando notificação de aviso para chamado {} - {} horas restantes", 
                   chamado.getNumero(), horasRestantes);
    }

    /**
     * Notifica gestores sobre violação de SLA
     */
    private void notificarViolacaoSla(Chamado chamado) {
        // Implementar notificação para gestores sobre violação de SLA
        // Pode incluir escalação automática
        logger.error("Notificando gestores sobre violação de SLA - Chamado: {}", chamado.getNumero());
    }

    /**
     * Verifica se já foi enviado alerta crítico recentemente
     */
    private boolean jaEnviouAlerteCritico(Chamado chamado) {
        // Implementar lógica para evitar spam de alertas
        // Pode usar cache ou campo no banco de dados
        return false; // Por enquanto sempre envia
    }

    /**
     * Marca que alerta crítico foi enviado
     */
    private void marcarAlerteCriticoEnviado(Chamado chamado) {
        // Implementar marcação de alerta enviado
        logger.debug("Marcando alerta crítico como enviado para chamado {}", chamado.getNumero());
    }

    /**
     * Verifica se já foi enviado alerta de aviso recentemente
     */
    private boolean jaEnviouAlerteAviso(Chamado chamado) {
        // Implementar lógica para evitar spam de alertas
        return false; // Por enquanto sempre envia
    }

    /**
     * Marca que alerta de aviso foi enviado
     */
    private void marcarAlerteAvisoEnviado(Chamado chamado) {
        // Implementar marcação de alerta enviado
        logger.debug("Marcando alerta de aviso como enviado para chamado {}", chamado.getNumero());
    }

    /**
     * Calcula estatísticas de SLA
     */
    public SlaEstatisticas calcularEstatisticasSla() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime proximaHora = agora.plusHours(1);
            LocalDateTime proximasDuasHoras = agora.plusHours(2);
            LocalDateTime proximasQuatroHoras = agora.plusHours(4);

            List<Chamado> chamadosProximaHora = chamadoRepository.findChamadosProximosVencimentoSla(proximaHora);
            List<Chamado> chamadosProximasDuasHoras = chamadoRepository.findChamadosProximosVencimentoSla(proximasDuasHoras);
            List<Chamado> chamadosProximasQuatroHoras = chamadoRepository.findChamadosProximosVencimentoSla(proximasQuatroHoras);

            // Chamados já vencidos
            List<Chamado> chamadosVencidos = chamadoRepository.findChamadosProximosVencimentoSla(agora);
            long vencidos = chamadosVencidos.stream()
                .filter(c -> c.getSlaVencimento().isBefore(agora))
                .count();

            return new SlaEstatisticas(
                (int) vencidos,
                chamadosProximaHora.size(),
                chamadosProximasDuasHoras.size() - chamadosProximaHora.size(),
                chamadosProximasQuatroHoras.size() - chamadosProximasDuasHoras.size()
            );

        } catch (Exception e) {
            logger.error("Erro ao calcular estatísticas de SLA: {}", e.getMessage());
            return new SlaEstatisticas(0, 0, 0, 0);
        }
    }

    /**
     * Classe para estatísticas de SLA
     */
    public static class SlaEstatisticas {
        private final int chamadosVencidos;
        private final int chamadosProximaHora;
        private final int chamadosProximasDuasHoras;
        private final int chamadosProximasQuatroHoras;

        public SlaEstatisticas(int vencidos, int proximaHora, int proximasDuasHoras, int proximasQuatroHoras) {
            this.chamadosVencidos = vencidos;
            this.chamadosProximaHora = proximaHora;
            this.chamadosProximasDuasHoras = proximasDuasHoras;
            this.chamadosProximasQuatroHoras = proximasQuatroHoras;
        }

        // Getters
        public int getChamadosVencidos() { return chamadosVencidos; }
        public int getChamadosProximaHora() { return chamadosProximaHora; }
        public int getChamadosProximasDuasHoras() { return chamadosProximasDuasHoras; }
        public int getChamadosProximasQuatroHoras() { return chamadosProximasQuatroHoras; }
    }
}