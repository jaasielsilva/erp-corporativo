package com.jaasielsilva.portalceo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Envia email simples
     */
    public void enviarEmail(String destinatario, String assunto, String corpo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpo, true); // true para HTML
            helper.setFrom("noreply@portalceo.com");
            
            mailSender.send(message);
            logger.info("Email enviado com sucesso para: {}", destinatario);
            
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            // Em produção, você pode querer lançar uma exceção customizada
            // throw new EmailException("Falha ao enviar email", e);
        }
    }
    
    /**
     * Envia email com template HTML
     */
    public void enviarEmailComTemplate(String destinatario, String assunto, String template, Object... parametros) {
        try {
            String corpoFormatado = String.format(template, parametros);
            enviarEmail(destinatario, assunto, corpoFormatado);
        } catch (Exception e) {
            logger.error("Erro ao enviar email com template para {}: {}", destinatario, e.getMessage());
        }
    }
    
    /**
     * Template para nova solicitação
     */
    public void enviarNotificacaoNovaSolicitacao(String destinatario, String protocolo, String solicitante, String colaborador) {
        String template = """
            <html>
            <body>
                <h2>Nova Solicitação de Acesso - Portal CEO</h2>
                <p><strong>Protocolo:</strong> %s</p>
                <p><strong>Solicitante:</strong> %s</p>
                <p><strong>Colaborador:</strong> %s</p>
                <p>Uma nova solicitação de acesso foi criada e aguarda aprovação.</p>
                <p>Acesse o sistema para revisar e processar a solicitação.</p>
                <br>
                <p><em>Este é um email automático. Não responda.</em></p>
            </body>
            </html>
            """;
        
        enviarEmailComTemplate(destinatario, 
            "Nova Solicitação de Acesso - " + protocolo, 
            template, protocolo, solicitante, colaborador);
    }
    
    /**
     * Template para aprovação
     */
    public void enviarNotificacaoAprovacao(String destinatario, String protocolo, String colaborador, String aprovador) {
        String template = """
            <html>
            <body>
                <h2>Solicitação Aprovada - Portal CEO</h2>
                <p><strong>Protocolo:</strong> %s</p>
                <p><strong>Colaborador:</strong> %s</p>
                <p><strong>Aprovado por:</strong> %s</p>
                <p>Sua solicitação de acesso foi aprovada!</p>
                <p>O usuário será criado em breve e as credenciais serão enviadas.</p>
                <br>
                <p><em>Este é um email automático. Não responda.</em></p>
            </body>
            </html>
            """;
        
        enviarEmailComTemplate(destinatario, 
            "Solicitação Aprovada - " + protocolo, 
            template, protocolo, colaborador, aprovador);
    }
    
    /**
     * Template para rejeição
     */
    public void enviarNotificacaoRejeicao(String destinatario, String protocolo, String colaborador, String motivo) {
        String template = """
            <html>
            <body>
                <h2>Solicitação Rejeitada - Portal CEO</h2>
                <p><strong>Protocolo:</strong> %s</p>
                <p><strong>Colaborador:</strong> %s</p>
                <p><strong>Motivo:</strong> %s</p>
                <p>Infelizmente sua solicitação de acesso foi rejeitada.</p>
                <p>Entre em contato com o departamento responsável para mais informações.</p>
                <br>
                <p><em>Este é um email automático. Não responda.</em></p>
            </body>
            </html>
            """;
        
        enviarEmailComTemplate(destinatario, 
            "Solicitação Rejeitada - " + protocolo, 
            template, protocolo, colaborador, motivo);
    }
    
    /**
     * Template para envio de credenciais
     */
    public void enviarCredenciais(String destinatario, String nomeUsuario, String email, String senhaTemporaria) {
        String template = """
            <html>
            <body>
                <h2>Credenciais de Acesso - Portal CEO</h2>
                <p>Olá <strong>%s</strong>,</p>
                <p>Suas credenciais de acesso ao Portal CEO foram criadas:</p>
                <ul>
                    <li><strong>Email:</strong> %s</li>
                    <li><strong>Senha Temporária:</strong> %s</li>
                </ul>
                <p><strong>IMPORTANTE:</strong> Por segurança, altere sua senha no primeiro acesso.</p>
                <p>Acesse: <a href=\"http://localhost:8080\">Portal CEO</a></p>
                <br>
                <p><em>Este é um email automático. Não responda.</em></p>
            </body>
            </html>
            """;
        
        enviarEmailComTemplate(destinatario, 
            "Credenciais de Acesso - Portal CEO", 
            template, nomeUsuario, email, senhaTemporaria);
    }

    public void enviarEmailMarketing(String destinatario, String assunto, String corpo, Long campanhaId) {
    String template = """
        <html>
        <body>
            <h2>Email Marketing - Campanha %s</h2>
            <p>%s</p>
            <br>
            <p><em>Este é um email automático da campanha %s.</em></p>
        </body>
        </html>
        """;

    enviarEmailComTemplate(destinatario, assunto, template, campanhaId, corpo, campanhaId);
}

}