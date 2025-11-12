package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.TermoAuditoria;
import com.jaasielsilva.portalceo.repository.TermoAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Serviço simples de auditoria para eventos de Termo/Política
 * Registra logs estruturados para rastreabilidade.
 */
@Service
public class TermoAuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(TermoAuditoriaService.class);

    @Autowired
    private TermoAuditoriaRepository termoAuditoriaRepository;

    public void registrarCriacao(Termo termo, String usuarioEmail) {
        logger.info("AUDIT_TERMO | operacao=CRIAR | id={} | tipo={} | versao={} | usuario={}",
                termo.getId(), termo.getTipo(), termo.getVersao(), usuarioEmail);
    }

    public void registrarMudancaStatus(Termo termo, Termo.StatusTermo statusAnterior,
                                       Termo.StatusTermo statusNovo, String usuarioEmail, String motivo) {
        logger.info("AUDIT_TERMO | operacao=STATUS | id={} | de={} | para={} | usuario={} | motivo={}",
                termo.getId(), statusAnterior, statusNovo, usuarioEmail, motivo != null ? motivo : "");
    }

    public void registrarMudancaStatus(Termo termo, Termo.StatusTermo statusAnterior,
                                       Termo.StatusTermo statusNovo, String usuarioEmail, String motivo,
                                       String ip, String operacao) {
        logger.info("AUDIT_TERMO | operacao={} | id={} | de={} | para={} | usuario={} | ip={} | motivo={}",
                operacao, termo.getId(), statusAnterior, statusNovo, usuarioEmail, ip, motivo != null ? motivo : "");

        TermoAuditoria audit = new TermoAuditoria();
        audit.setTermo(termo);
        audit.setStatusAnterior(statusAnterior);
        audit.setStatusNovo(statusNovo);
        audit.setUsuarioEmail(usuarioEmail);
        audit.setIp(ip);
        audit.setMotivo(motivo);
        audit.setOperacao(operacao);
        termoAuditoriaRepository.save(audit);
    }

    public void registrarAcao(Termo termo, String usuarioEmail, String motivo, String ip, String operacao) {
        logger.info("AUDIT_TERMO | operacao={} | id={} | status={} | usuario={} | ip={} | motivo={}",
                operacao, termo.getId(), termo.getStatus(), usuarioEmail, ip, motivo != null ? motivo : "");

        TermoAuditoria audit = new TermoAuditoria();
        audit.setTermo(termo);
        audit.setStatusAnterior(termo.getStatus());
        audit.setStatusNovo(termo.getStatus());
        audit.setUsuarioEmail(usuarioEmail);
        audit.setIp(ip);
        audit.setMotivo(motivo);
        audit.setOperacao(operacao);
        termoAuditoriaRepository.save(audit);
    }

    public void registrarTentativaOperacaoNaoAutorizada(String operacao, Long termoId, String usuarioEmail, String motivo) {
        logger.warn("AUDIT_TERMO | operacao={} | id={} | usuario={} | nao_autorizado=true | motivo={}",
                operacao, termoId, usuarioEmail, motivo);
    }

    public void registrarErroOperacao(String operacao, Long termoId, String usuarioEmail, String erro) {
        logger.error("AUDIT_TERMO | operacao={} | id={} | usuario={} | erro={}",
                operacao, termoId, usuarioEmail, erro);
    }
}