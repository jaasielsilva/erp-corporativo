package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Holerite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class HoleriteEmailService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HoleriteService holeriteService;

    @Value("${rh.folha.email.destinatariosExtras:}")
    private String destinatariosExtras;

    public void enviar(Holerite h, String emailOverride) throws java.io.IOException {
        List<String> destinatarios = new ArrayList<>();
        String base = h.getColaborador() != null ? h.getColaborador().getEmail() : null;
        if (emailOverride != null && !emailOverride.isBlank()) destinatarios.add(emailOverride);
        else if (base != null && !base.isBlank()) destinatarios.add(base);
        if (destinatariosExtras != null && !destinatariosExtras.isBlank()) {
            String[] extras = destinatariosExtras.split(",");
            for (String e : extras) {
                String t = e.trim();
                if (!t.isEmpty()) destinatarios.add(t);
            }
        }
        if (destinatarios.isEmpty()) throw new IllegalArgumentException("destinatario_indisponivel");

        byte[] pdf = gerarPdf(h);
        String assunto = "Holerite - " + holeriteService.gerarDescricaoPeriodo(h);
        String corpoHtml = "<html><body><p>Segue seu holerite em anexo.</p><p>Competência: "
                + holeriteService.gerarDescricaoPeriodo(h) + "</p></body></html>";
        String nomeArquivo = "holerite_" + h.getId() + ".pdf";
        for (String d : destinatarios) {
            emailService.enviarEmailComAnexo(d, assunto, corpoHtml, nomeArquivo, pdf);
        }
    }

    private byte[] gerarPdf(Holerite h) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Context ctx = new Context(new Locale("pt", "BR"));
        ctx.setVariable("holerite", h);
        ctx.setVariable("numeroHolerite", holeriteService.gerarNumeroHolerite(h));
        ctx.setVariable("periodoReferencia", holeriteService.gerarDescricaoPeriodo(h));
        ctx.setVariable("dataReferencia", holeriteService.gerarDataReferencia(h));
        ctx.setVariable("pdf", true);
        String html = templateEngine.process("rh/folha-pagamento/holerite-pdf", ctx);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        try {
            builder.run();
        } catch (Exception e) {
            throw new java.io.IOException("erro_pdf", e);
        }
        return baos.toByteArray();
    }
}
