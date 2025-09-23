package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Chamado.StatusChamado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço de auditoria para operações críticas do módulo de suporte
 * Registra todas as ações importantes para rastreabilidade e compliance
 */
@Service
public class ChamadoAuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(ChamadoAuditoriaService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Registra mudança de status de chamado
     */
    public void registrarMudancaStatus(Chamado chamado, StatusChamado statusAnterior, 
                                     StatusChamado novoStatus, String usuario, String observacoes) {
        
        String logMessage = String.format(
            "MUDANCA_STATUS | Chamado: %s | Status: %s -> %s | Usuario: %s | Data: %s | Observacoes: %s",
            chamado.getNumero(),
            statusAnterior != null ? statusAnterior.getDescricao() : "N/A",
            novoStatus.getDescricao(),
            usuario != null ? usuario : "Sistema",
            LocalDateTime.now().format(FORMATTER),
            observacoes != null ? observacoes : "Sem observações"
        );
        
        auditLogger.info(logMessage);
        logger.info("Auditoria registrada: mudança de status do chamado {} de {} para {}", 
                   chamado.getNumero(), statusAnterior, novoStatus);
    }

    /**
     * Registra criação de novo chamado
     */
    public void registrarCriacaoChamado(Chamado chamado, String usuario) {
        String logMessage = String.format(
            "CRIACAO_CHAMADO | Chamado: %s | Assunto: %s | Prioridade: %s | Usuario: %s | Data: %s",
            chamado.getNumero(),
            chamado.getAssunto(),
            chamado.getPrioridade().getDescricao(),
            usuario != null ? usuario : "Sistema",
            LocalDateTime.now().format(FORMATTER)
        );
        
        auditLogger.info(logMessage);
        logger.info("Auditoria registrada: criação do chamado {}", chamado.getNumero());
    }

    /**
     * Registra atribuição de técnico
     */
    public void registrarAtribuicaoTecnico(Chamado chamado, String tecnicoAnterior, 
                                         String novoTecnico, String usuario) {
        
        String logMessage = String.format(
            "ATRIBUICAO_TECNICO | Chamado: %s | Tecnico: %s -> %s | Usuario: %s | Data: %s",
            chamado.getNumero(),
            tecnicoAnterior != null ? tecnicoAnterior : "Não atribuído",
            novoTecnico != null ? novoTecnico : "Não atribuído",
            usuario != null ? usuario : "Sistema",
            LocalDateTime.now().format(FORMATTER)
        );
        
        auditLogger.info(logMessage);
        logger.info("Auditoria registrada: atribuição de técnico do chamado {}", chamado.getNumero());
    }

    /**
     * Registra resolução de chamado
     */
    public void registrarResolucaoChamado(Chamado chamado, String usuario, String solucao) {
        String logMessage = String.format(
            "RESOLUCAO_CHAMADO | Chamado: %s | Usuario: %s | Data: %s | Solucao: %s",
            chamado.getNumero(),
            usuario != null ? usuario : "Sistema",
            LocalDateTime.now().format(FORMATTER),
            solucao != null ? solucao.substring(0, Math.min(solucao.length(), 100)) + "..." : "Sem descrição"
        );
        
        auditLogger.info(logMessage);
        logger.info("Auditoria registrada: resolução do chamado {}", chamado.getNumero());
    }

    /**
     * Registra reabertura de chamado
     */
    public void registrarReaberturaChamado(Chamado chamado, String usuario, String motivo) {
        String logMessage = String.format(
            "REABERTURA_CHAMADO | Chamado: %s | Usuario: %s | Data: %s | Motivo: %s",
            chamado.getNumero(),
            usuario != null ? usuario : "Sistema",
            LocalDateTime.now().format(FORMATTER),
            motivo != null ? motivo : "Motivo não informado"
        );
        
        auditLogger.info(logMessage);
        logger.info("Auditoria registrada: reabertura do chamado {}", chamado.getNumero());
    }

    /**
     * Registra avaliação de chamado
     */
    public void registrarAvaliacaoChamado(Chamado chamado, Integer avaliacao, String comentario, String usuario) {
        String logMessage = String.format(
            "AVALIACAO_CHAMADO | Chamado: %s | Avaliacao: %d/5 | Usuario: %s | Data: %s | Comentario: %s",
            chamado.getNumero(),
            avaliacao != null ? avaliacao : 0,
            usuario != null ? usuario : "Sistema",
            LocalDateTime.now().format(FORMATTER),
            comentario != null ? comentario.substring(0, Math.min(comentario.length(), 100)) + "..." : "Sem comentário"
        );
        
        auditLogger.info(logMessage);
        logger.info("Auditoria registrada: avaliação do chamado {}", chamado.getNumero());
    }

    /**
     * Registra violação de SLA
     */
    public void registrarViolacaoSLA(Chamado chamado, double slaVencido) {
        String logMessage = String.format(
            "VIOLACAO_SLA | Chamado: %s | SLA_Vencido: %.2f horas | Prioridade: %s | Data: %s",
            chamado.getNumero(),
            slaVencido,
            chamado.getPrioridade().getDescricao(),
            LocalDateTime.now().format(FORMATTER)
        );
        
        auditLogger.warn(logMessage);
        logger.warn("Auditoria registrada: violação de SLA do chamado {}", chamado.getNumero());
    }

    /**
     * Registra tentativa de operação não autorizada
     */
    public void registrarTentativaOperacaoNaoAutorizada(String operacao, Long chamadoId, String usuario, String motivo) {
        String logMessage = String.format(
            "OPERACAO_NAO_AUTORIZADA | Operacao: %s | Chamado_ID: %d | Usuario: %s | Motivo: %s | Data: %s",
            operacao,
            chamadoId,
            usuario != null ? usuario : "Desconhecido",
            motivo,
            LocalDateTime.now().format(FORMATTER)
        );
        
        auditLogger.warn(logMessage);
        logger.warn("Auditoria registrada: tentativa de operação não autorizada - {}", operacao);
    }

    /**
     * Registra erro durante operação
     */
    public void registrarErroOperacao(String operacao, Long chamadoId, String usuario, String erro) {
        String logMessage = String.format(
            "ERRO_OPERACAO | Operacao: %s | Chamado_ID: %d | Usuario: %s | Erro: %s | Data: %s",
            operacao,
            chamadoId,
            usuario != null ? usuario : "Sistema",
            erro.substring(0, Math.min(erro.length(), 200)) + "...",
            LocalDateTime.now().format(FORMATTER)
        );
        
        auditLogger.error(logMessage);
        logger.error("Auditoria registrada: erro durante operação - {}", operacao);
    }
}