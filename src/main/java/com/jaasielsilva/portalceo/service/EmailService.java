package com.jaasielsilva.portalceo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.Colaborador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender defaultMailSender;

    @Autowired
    private ConfiguracaoService configuracaoService;

    @Value("${spring.mail.username}")
    private String defaultMailFrom;

    private JavaMailSender getMailSender() {
        String dbHost = configuracaoService.getValor("smtp_host", "");
        
        if (dbHost != null && !dbHost.isEmpty()) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(dbHost);
            sender.setPort(Integer.parseInt(configuracaoService.getValor("smtp_port", "587")));
            sender.setUsername(configuracaoService.getValor("smtp_username", ""));
            sender.setPassword(configuracaoService.getValor("smtp_password", ""));

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");

            return sender;
        }
        return defaultMailSender;
    }

    private String getMailFrom() {
        String dbFrom = configuracaoService.getValor("smtp_from", "");
        return (dbFrom != null && !dbFrom.isEmpty()) ? dbFrom : defaultMailFrom;
    }

    /**
     * Envia email simples
     */
    @Async
    public void enviarEmail(String destinatario, String assunto, String corpo) {
        try {
            enviarEmailTeste(destinatario, assunto, corpo);
            logger.info("Email enviado com sucesso para: {}", destinatario);
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * Envia email de forma síncrona (para testes e depuração)
     * Lança exceção em caso de erro para ser capturado pelo chamador
     */
    public void enviarEmailTeste(String destinatario, String assunto, String corpo) throws Exception {
        JavaMailSender sender = getMailSender();
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(assunto);
        helper.setText(corpo, true); // true para HTML
        helper.setFrom(getMailFrom());

        sender.send(message);
    }

    /**
     * Envia email com template HTML
     */
    @Async
    public void enviarEmailComTemplate(String destinatario, String assunto, String template, Object... parametros) {
        try {
            String corpoFormatado = String.format(template, parametros);
            enviarEmail(destinatario, assunto, corpoFormatado);
        } catch (Exception e) {
            logger.error("Erro ao enviar email com template para {}: {}", destinatario, e.getMessage());
        }
    }

    @Async
    public void enviarEmailComAnexo(String destinatario, String assunto, String corpoHtml, String nomeArquivo, byte[] anexo) {
        try {
            JavaMailSender sender = getMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            helper.setFrom(getMailFrom());
            helper.addAttachment(nomeArquivo, new ByteArrayDataSource(anexo, "application/pdf"));

            sender.send(message);
            logger.info("Email com anexo enviado para: {}", destinatario);
        } catch (Exception e) {
            logger.error("Erro ao enviar email com anexo para {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * Template para nova solicitação
     */
    @Async
    public void enviarNotificacaoNovaSolicitacao(String destinatario, String protocolo, String solicitante,
            String colaborador) {
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
    @Async
    public void enviarNotificacaoAprovacao(String destinatario, String protocolo, String colaborador,
            String aprovador) {
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
    @Async
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
    @Async
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

    @Async
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

    /**
     * Envia email de boas-vindas para um novo colaborador
     */
    @Async
    public void enviarEmailBoasVindas(Colaborador colaborador) {
        if (colaborador == null || colaborador.getEmail() == null) {
            logger.warn("Colaborador ou email inválido para envio de boas-vindas");
            return;
        }

        String template = """
                <html>
                <body>
                    <h2>Bem-vindo(a) ao Portal CEO, %s!</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Seu cadastro foi realizado com sucesso e você já pode acessar o sistema.</p>
                    <p>Seu usuário é: <strong>%s</strong></p>
                    <p>Por segurança, altere sua senha no primeiro acesso.</p>
                    <p>Acesse: <a href="http://localhost:8080">Portal CEO</a></p>
                    <br>
                    <p><em>Este é um email automático. Não responda.</em></p>
                </body>
                </html>
                """;

        String usuario = colaborador.getNome();
        String email = colaborador.getEmail();
         // ou outro campo que representa o usuário

        enviarEmailComTemplate(email,
                "Bem-vindo(a) ao Portal CEO",
                template,
                usuario, email);

        logger.info("Email de boas-vindas enviado para {}", email);
    }

    /**
     * Envia email de notificação para novo processo de adesão
     */
    @Async
    public void enviarNotificacaoNovoProcessoAdesao(String destinatario, String nomeColaborador, String processoId) {
        String template = """
                <html>
                <body>
                    <h2>Novo Processo de Adesão - Portal CEO</h2>
                    <p><strong>Colaborador:</strong> %s</p>
                    <p><strong>Processo ID:</strong> %s</p>
                    <p>Um novo processo de adesão foi criado e aguarda aprovação.</p>
                    <p>Acesse o sistema para revisar e processar o processo.</p>
                    <p><a href="http://localhost:8080/rh/workflow/processo/%s">Visualizar Processo</a></p>
                    <br>
                    <p><em>Este é um email automático. Não responda.</em></p>
                </body>
                </html>
                """;

        enviarEmailComTemplate(destinatario,
                "Novo Processo de Adesão - " + nomeColaborador,
                template, nomeColaborador, processoId, processoId);

        logger.info("Email de novo processo de adesão enviado para {} - Colaborador: {}", destinatario, nomeColaborador);
    }

    /**
     * Envia email de notificação de processo aprovado
     */
    @Async
    public void enviarNotificacaoProcessoAprovado(String destinatario, String nomeColaborador, String aprovadoPor, String sessionId) {
        String template = """
                <html>
                <body>
                    <h2>Processo de Adesão Aprovado - Portal CEO</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Seu processo de adesão foi <strong>aprovado</strong> com sucesso!</p>
                    <p><strong>Aprovado por:</strong> %s</p>
                    <p>Seus benefícios serão ativados em breve e você receberá mais informações sobre os próximos passos.</p>
                    <p><a href="http://localhost:8080/rh/colaboradores/adesao/status/%s">Acompanhar Status</a></p>
                    <br>
                    <p>Parabéns e bem-vindo(a) aos nossos benefícios!</p>
                    <p><em>Este é um email automático. Não responda.</em></p>
                </body>
                </html>
                """;

        enviarEmailComTemplate(destinatario,
                "Processo de Adesão Aprovado",
                template, nomeColaborador, aprovadoPor, sessionId);

        logger.info("Email de processo aprovado enviado para {} - Colaborador: {}", destinatario, nomeColaborador);
    }

    /**
     * Envia email de notificação de processo rejeitado
     */
    @Async
    public void enviarNotificacaoProcessoRejeitado(String destinatario, String nomeColaborador, String motivoRejeicao) {
        String template = """
                <html>
                <body>
                    <h2>Processo de Adesão Rejeitado - Portal CEO</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Infelizmente, seu processo de adesão foi <strong>rejeitado</strong>.</p>
                    <p><strong>Motivo:</strong> %s</p>
                    <p>Entre em contato com o departamento de RH para mais informações ou para iniciar um novo processo.</p>
                    <p><a href="http://localhost:8080/rh/colaboradores/adesao/inicio">Iniciar Novo Processo</a></p>
                    <br>
                    <p>Atenciosamente,<br>Equipe de RH</p>
                    <p><em>Este é um email automático. Não responda.</em></p>
                </body>
                </html>
                """;

        enviarEmailComTemplate(destinatario,
                "Processo de Adesão Rejeitado",
                template, nomeColaborador, motivoRejeicao);

        logger.info("Email de processo rejeitado enviado para {} - Colaborador: {}", destinatario, nomeColaborador);
    }

}
