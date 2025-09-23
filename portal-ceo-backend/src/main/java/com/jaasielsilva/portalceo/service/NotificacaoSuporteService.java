package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class NotificacaoSuporteService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoSuporteService.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Notifica colaborador sobre novo chamado atribuído
     */
    public void notificarNovoColaborador(Chamado chamado, Colaborador colaborador) {
        try {
            // Notificação por email
            enviarEmailNovoColaborador(chamado, colaborador);
            
            // Notificação interna
            criarNotificacaoInterna(chamado, colaborador);
            
            logger.info("Notificações enviadas para colaborador {} - Chamado {}", 
                       colaborador.getNome(), chamado.getNumero());
                       
        } catch (Exception e) {
            logger.error("Erro ao enviar notificações para colaborador {} - Chamado {}: {}", 
                        colaborador.getNome(), chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Notifica solicitante sobre resolução do chamado
     */
    public void notificarResolucao(Chamado chamado) {
        try {
            if (chamado.getSolicitanteEmail() != null && !chamado.getSolicitanteEmail().trim().isEmpty()) {
                // Email de resolução
                enviarEmailResolucao(chamado);
                
                // Notificação interna (se solicitante tiver conta no sistema)
                criarNotificacaoResolucao(chamado);
                
                logger.info("Notificação de resolução enviada para {} - Chamado {}", 
                           chamado.getSolicitanteEmail(), chamado.getNumero());
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de resolução - Chamado {}: {}", 
                        chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Notifica sobre chamado próximo do vencimento do SLA
     */
    public void notificarSlaProximoVencimento(Chamado chamado) {
        try {
            if (chamado.getColaboradorResponsavel() != null) {
                // Email de alerta SLA
                enviarEmailAlertaSla(chamado);
                
                // Notificação interna urgente
                criarNotificacaoAlertaSla(chamado);
                
                logger.info("Alerta de SLA enviado para {} - Chamado {}", 
                           chamado.getColaboradorResponsavel().getNome(), chamado.getNumero());
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar alerta de SLA - Chamado {}: {}", 
                        chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Notifica sobre reabertura de chamado
     */
    public void notificarReabertura(Chamado chamado) {
        try {
            if (chamado.getColaboradorResponsavel() != null) {
                // Email de reabertura
                enviarEmailReabertura(chamado);
                
                // Notificação interna
                criarNotificacaoReabertura(chamado);
                
                logger.info("Notificação de reabertura enviada para {} - Chamado {}", 
                           chamado.getColaboradorResponsavel().getNome(), chamado.getNumero());
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação de reabertura - Chamado {}: {}", 
                        chamado.getNumero(), e.getMessage());
        }
    }

    /**
     * Envia email para colaborador sobre novo chamado
     */
    private void enviarEmailNovoColaborador(Chamado chamado, Colaborador colaborador) {
        String assunto = String.format("Novo Chamado Atribuído - %s", chamado.getNumero());
        
        String template = """
                <html>
                <body>
                    <h2>Novo Chamado Atribuído - Portal CEO</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Um novo chamado foi atribuído para você:</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0;">
                        <p><strong>Número:</strong> %s</p>
                        <p><strong>Assunto:</strong> %s</p>
                        <p><strong>Prioridade:</strong> %s</p>
                        <p><strong>SLA:</strong> %s</p>
                        <p><strong>Solicitante:</strong> %s</p>
                        <p><strong>Categoria:</strong> %s</p>
                    </div>
                    
                    <p><strong>Descrição:</strong></p>
                    <p style="background-color: #f8f9fa; padding: 10px; border-radius: 3px;">%s</p>
                    
                    <p><a href="http://localhost:8080/suporte/chamado/%s" 
                         style="background-color: #007bff; color: white; padding: 10px 20px; 
                                text-decoration: none; border-radius: 5px;">
                        Visualizar Chamado
                    </a></p>
                    
                    <br>
                    <p>Atenciosamente,<br>Sistema de Suporte</p>
                    <p><em>Este é um email automático. Não responda.</em></p>
                </body>
                </html>
                """;

        String slaFormatado = chamado.getSlaVencimento() != null ? 
            chamado.getSlaVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Não definido";

        emailService.enviarEmailComTemplate(
            colaborador.getEmail(),
            assunto,
            template,
            colaborador.getNome(),
            chamado.getNumero(),
            chamado.getAssunto(),
            chamado.getPrioridade().getDescricao(),
            slaFormatado,
            chamado.getSolicitanteNome() != null ? chamado.getSolicitanteNome() : "Não informado",
            chamado.getCategoria() != null ? chamado.getCategoria() : "Geral",
            chamado.getDescricao(),
            chamado.getId()
        );
    }

    /**
     * Cria notificação interna para colaborador
     */
    private void criarNotificacaoInterna(Chamado chamado, Colaborador colaborador) {
        String titulo = String.format("Novo Chamado: %s", chamado.getNumero());
        String mensagem = String.format("Chamado '%s' (Prioridade: %s) foi atribuído para você", 
                                       chamado.getAssunto(), chamado.getPrioridade().getDescricao());
        String url = String.format("/suporte/chamado/%s", chamado.getId());

        notificacaoService.adicionarNotificacao(
            colaborador.getEmail(),
            titulo,
            mensagem,
            "warning", // Tipo warning para chamados atribuídos
            url
        );
    }

    /**
     * Envia email de resolução para solicitante
     */
    private void enviarEmailResolucao(Chamado chamado) {
        String assunto = String.format("Chamado Resolvido - %s", chamado.getNumero());
        
        String template = """
                <html>
                <body>
                    <h2>Chamado Resolvido - Portal CEO</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Seu chamado foi resolvido:</p>
                    
                    <div style="background-color: #d4edda; padding: 15px; border-radius: 5px; margin: 15px 0;">
                        <p><strong>Número:</strong> %s</p>
                        <p><strong>Assunto:</strong> %s</p>
                        <p><strong>Resolvido por:</strong> %s</p>
                        <p><strong>Data de Resolução:</strong> %s</p>
                    </div>
                    
                    <p>Por favor, verifique se a solução atende às suas necessidades.</p>
                    <p>Se o problema não foi resolvido adequadamente, você pode reabrir o chamado.</p>
                    
                    <p><a href="http://localhost:8080/suporte/chamado/%s" 
                         style="background-color: #28a745; color: white; padding: 10px 20px; 
                                text-decoration: none; border-radius: 5px;">
                        Visualizar Chamado
                    </a></p>
                    
                    <br>
                    <p>Atenciosamente,<br>Equipe de Suporte</p>
                    <p><em>Este é um email automático. Não responda.</em></p>
                </body>
                </html>
                """;

        String dataResolucao = chamado.getDataResolucao() != null ? 
            chamado.getDataResolucao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Agora";

        emailService.enviarEmailComTemplate(
            chamado.getSolicitanteEmail(),
            assunto,
            template,
            chamado.getSolicitanteNome() != null ? chamado.getSolicitanteNome() : "Cliente",
            chamado.getNumero(),
            chamado.getAssunto(),
            chamado.getColaboradorResponsavel() != null ? chamado.getColaboradorResponsavel().getNome() : "Equipe de Suporte",
            dataResolucao,
            chamado.getId()
        );
    }

    /**
     * Cria notificação interna de resolução
     */
    private void criarNotificacaoResolucao(Chamado chamado) {
        if (chamado.getSolicitanteEmail() != null) {
            String titulo = String.format("Chamado Resolvido: %s", chamado.getNumero());
            String mensagem = String.format("Seu chamado '%s' foi resolvido", chamado.getAssunto());
            String url = String.format("/suporte/chamado/%s", chamado.getId());

            notificacaoService.adicionarNotificacao(
                chamado.getSolicitanteEmail(),
                titulo,
                mensagem,
                "success",
                url
            );
        }
    }

    /**
     * Envia email de alerta de SLA
     */
    private void enviarEmailAlertaSla(Chamado chamado) {
        String assunto = String.format("URGENTE: SLA Próximo do Vencimento - %s", chamado.getNumero());
        
        String template = """
                <html>
                <body>
                    <h2 style="color: #dc3545;">ALERTA: SLA Próximo do Vencimento</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p style="color: #dc3545;"><strong>ATENÇÃO:</strong> O SLA do chamado está próximo do vencimento!</p>
                    
                    <div style="background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #dc3545;">
                        <p><strong>Número:</strong> %s</p>
                        <p><strong>Assunto:</strong> %s</p>
                        <p><strong>Prioridade:</strong> %s</p>
                        <p><strong>SLA Vence em:</strong> %s</p>
                    </div>
                    
                    <p><strong>Ação necessária:</strong> Priorize este chamado para evitar violação do SLA.</p>
                    
                    <p><a href="http://localhost:8080/suporte/chamado/%s" 
                         style="background-color: #dc3545; color: white; padding: 10px 20px; 
                                text-decoration: none; border-radius: 5px;">
                        Atender Chamado URGENTE
                    </a></p>
                    
                    <br>
                    <p>Sistema de Suporte</p>
                </body>
                </html>
                """;

        String slaFormatado = chamado.getSlaVencimento() != null ? 
            chamado.getSlaVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Não definido";

        emailService.enviarEmailComTemplate(
            chamado.getColaboradorResponsavel().getEmail(),
            assunto,
            template,
            chamado.getColaboradorResponsavel().getNome(),
            chamado.getNumero(),
            chamado.getAssunto(),
            chamado.getPrioridade().getDescricao(),
            slaFormatado,
            chamado.getId()
        );
    }

    /**
     * Cria notificação interna de alerta SLA
     */
    private void criarNotificacaoAlertaSla(Chamado chamado) {
        String titulo = String.format("URGENTE: SLA Vencendo - %s", chamado.getNumero());
        String mensagem = String.format("SLA do chamado '%s' está próximo do vencimento!", chamado.getAssunto());
        String url = String.format("/suporte/chamado/%s", chamado.getId());

        notificacaoService.adicionarNotificacao(
            chamado.getColaboradorResponsavel().getEmail(),
            titulo,
            mensagem,
            "error", // Tipo error para alertas urgentes
            url
        );
    }

    /**
     * Envia email de reabertura
     */
    private void enviarEmailReabertura(Chamado chamado) {
        String assunto = String.format("Chamado Reaberto - %s", chamado.getNumero());
        
        String template = """
                <html>
                <body>
                    <h2 style="color: #ffc107;">Chamado Reaberto - Portal CEO</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>O chamado foi reaberto pelo solicitante:</p>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 15px 0;">
                        <p><strong>Número:</strong> %s</p>
                        <p><strong>Assunto:</strong> %s</p>
                        <p><strong>Solicitante:</strong> %s</p>
                        <p><strong>Data de Reabertura:</strong> %s</p>
                    </div>
                    
                    <p>Por favor, entre em contato com o solicitante para entender os motivos da reabertura.</p>
                    
                    <p><a href="http://localhost:8080/suporte/chamado/%s" 
                         style="background-color: #ffc107; color: black; padding: 10px 20px; 
                                text-decoration: none; border-radius: 5px;">
                        Visualizar Chamado
                    </a></p>
                    
                    <br>
                    <p>Sistema de Suporte</p>
                </body>
                </html>
                """;

        String dataReabertura = chamado.getDataReabertura() != null ? 
            chamado.getDataReabertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Agora";

        emailService.enviarEmailComTemplate(
            chamado.getColaboradorResponsavel().getEmail(),
            assunto,
            template,
            chamado.getColaboradorResponsavel().getNome(),
            chamado.getNumero(),
            chamado.getAssunto(),
            chamado.getSolicitanteNome() != null ? chamado.getSolicitanteNome() : "Cliente",
            dataReabertura,
            chamado.getId()
        );
    }

    /**
     * Cria notificação interna de reabertura
     */
    private void criarNotificacaoReabertura(Chamado chamado) {
        String titulo = String.format("Chamado Reaberto: %s", chamado.getNumero());
        String mensagem = String.format("O chamado '%s' foi reaberto pelo solicitante", chamado.getAssunto());
        String url = String.format("/suporte/chamado/%s", chamado.getId());

        notificacaoService.adicionarNotificacao(
            chamado.getColaboradorResponsavel().getEmail(),
            titulo,
            mensagem,
            "warning",
            url
        );
    }
}