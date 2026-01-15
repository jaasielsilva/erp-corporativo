package com.jaasielsilva.portalceo.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DocumentTemplateService {

    public byte[] personalizeDocx(Path templatePath, Map<String, String> placeholders) throws Exception {
        try (InputStream is = Files.newInputStream(templatePath);
             XWPFDocument doc = new XWPFDocument(is);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs == null) continue;
                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun run = runs.get(i);
                    String text = run.getText(0);
                    if (text == null) continue;
                    String replaced = replacePlaceholders(text, placeholders);
                    if (!replaced.equals(text)) {
                        run.setText(replaced, 0);
                    }
                }
            }
            doc.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] personalizeDocx(byte[] templateBytes, Map<String, String> placeholders) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(templateBytes);
             XWPFDocument doc = new XWPFDocument(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs == null) continue;
                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun run = runs.get(i);
                    String text = run.getText(0);
                    if (text == null) continue;
                    String replaced = replacePlaceholders(text, placeholders);
                    if (!replaced.equals(text)) {
                        run.setText(replaced, 0);
                    }
                }
            }
            doc.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generatePdfFromHtml(String html, Map<String, String> placeholders) throws Exception {
        Map<String, String> escaped = new java.util.HashMap<>();
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                escaped.put(e.getKey(), escapeXml(e.getValue()));
            }
        }
        String processed = replacePlaceholders(html, escaped);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
        builder.withHtmlContent(processed, "file:./");
        builder.toStream(baos);
        builder.run();
        return baos.toByteArray();
    }

    private String escapeXml(String input) {
        if (input == null || input.isEmpty()) return "";
        String out = input;
        out = out.replace("&", "&amp;");
        out = out.replace("<", "&lt;");
        out = out.replace(">", "&gt;");
        out = out.replace("\"", "&quot;");
        out = out.replace("'", "&apos;");
        return out;
    }

    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        String out = text;
        for (Map.Entry<String,String> e : placeholders.entrySet()) {
            String token = "{{" + e.getKey() + "}}";
            out = out.replace(token, e.getValue() != null ? e.getValue() : "");
        }
        return out;
    }
}
