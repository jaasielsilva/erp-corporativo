package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.PageDoc;
import com.jaasielsilva.portalceo.model.FieldDoc;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocExportService {
    private final DocScannerService scanner;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public DocExportService(DocScannerService scanner) {
        this.scanner = scanner;
    }

    public void exportManual() throws IOException {
        List<PageDoc> docs = scanner.scan();
        writeCadastroCliente(docs);
        writeProcessoJuridico(docs);
        writeFinanceiroLiquidacao(docs);
        writeFinanceiroPagamentoCliente(docs);
    }

    private void writeCadastroCliente(List<PageDoc> docs) throws IOException {
        PageDoc cadastro = findByTemplate(docs, "clientes/geral/cadastro");
        Path file = path("manual/cadastro/template-cadastro-cliente.md");
        String md = "# Cadastro do Cliente\n"
                + "\n## Página\n"
                + sectionItem(cadastro)
                + sectionFields(cadastro);
        write(file, md);
    }

    private void writeProcessoJuridico(List<PageDoc> docs) throws IOException {
        PageDoc form = findByTemplate(docs, "juridico/processo-form");
        PageDoc detalhes = findByTemplate(docs, "juridico/processo-detalhes");
        PageDoc lista = findByTemplate(docs, "juridico/processos");
        Path file = path("manual/processo/template-criacao-processo.md");
        String md = "# Processo Jurídico\n"
                + "\n## Criar Processo\n"
                + sectionItem(form)
                + "\n## Detalhes do Processo\n"
                + sectionItem(detalhes)
                + "\n## Lista de Processos\n"
                + sectionItem(lista);
        write(file, md);
    }

    private void writeFinanceiroLiquidacao(List<PageDoc> docs) throws IOException {
        PageDoc lista = findByTemplate(docs, "financeiro/contas-receber/lista");
        PageDoc detalhes = findByTemplate(docs, "financeiro/contas-receber/detalhes");
        Path file = path("manual/financeiro/template-liquidacao.md");
        String md = "# Financeiro – Liquidação\n"
                + sectionItem(lista)
                + sectionItem(detalhes);
        write(file, md);
    }

    private void writeFinanceiroPagamentoCliente(List<PageDoc> docs) throws IOException {
        PageDoc transfer = findByTemplate(docs, "financeiro/transferencias");
        Path file = path("manual/financeiro/template-pagamento-cliente.md");
        String md = "# Financeiro – Pagamento a Cliente\n"
                + sectionItem(transfer);
        write(file, md);
    }

    private String sectionItem(PageDoc d) {
        if (d == null) return "\n- Página não mapeada.\n";
        String elements = Optional.ofNullable(d.getElements()).orElse(List.of()).stream().sorted().collect(Collectors.joining(", "));
        String components = Optional.ofNullable(d.getComponents()).orElse(List.of()).stream().sorted().collect(Collectors.joining(", "));
        String links = Optional.ofNullable(d.getNavigationLinks()).orElse(List.of()).stream().sorted(Comparator.naturalOrder()).map(l -> "[" + l + "](" + l + ")").collect(Collectors.joining(", "));
        String lastUpd = d.getLastUpdated() != null ? fmt.format(d.getLastUpdated()) : "-";
        StringBuilder sb = new StringBuilder();
        sb.append("\n- URL: ").append(Optional.ofNullable(d.getUrl()).orElse("-"));
        sb.append("\n- Método: ").append(Optional.ofNullable(d.getHttpMethod()).orElse("-"));
        sb.append("\n- Template: ").append(Optional.ofNullable(d.getTemplate()).orElse("-"));
        sb.append("\n- Título: ").append(Optional.ofNullable(d.getTitle()).orElse("-"));
        sb.append("\n- Permissões: ").append(Optional.ofNullable(d.getPermissions()).orElse("-"));
        sb.append("\n- Elementos: ").append(elements.isBlank() ? "-" : elements);
        sb.append("\n- Componentes: ").append(components.isBlank() ? "-" : components);
        sb.append("\n- Fluxos: ").append(links.isBlank() ? "-" : links);
        sb.append("\n- Última atualização: ").append(lastUpd).append("\n");
        return sb.toString();
    }

    private String sectionFields(PageDoc d) {
        List<FieldDoc> all = Optional.ofNullable(d.getFields()).orElse(List.of());
        if (all.isEmpty()) return "";
        List<FieldDoc> required = all.stream().filter(FieldDoc::isRequired).collect(Collectors.toList());
        List<FieldDoc> optional = all.stream().filter(f -> !f.isRequired()).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("\n## Campos\n");
        sb.append("\n### Obrigatórios\n");
        if (required.isEmpty()) {
            sb.append("- Nenhum\n");
        } else {
            for (FieldDoc f : required) sb.append(renderField(f));
        }
        sb.append("\n### Opcionais\n");
        if (optional.isEmpty()) {
            sb.append("- Nenhum\n");
        } else {
            for (FieldDoc f : optional) sb.append(renderField(f));
        }
        return sb.toString();
    }

    private String renderField(FieldDoc f) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(labelOrName(f)).append(" (").append(typeOrTag(f)).append(")");
        if (f.getPlaceholder() != null && !f.getPlaceholder().isBlank()) sb.append(" • placeholder: ").append(f.getPlaceholder());
        if (f.getMaxLength() != null) sb.append(" • maxlength: ").append(f.getMaxLength());
        if (f.getDisabledExpr() != null && !f.getDisabledExpr().isBlank()) sb.append(" • condicional: ").append(f.getDisabledExpr());
        sb.append("\n");
        return sb.toString();
    }

    private String labelOrName(FieldDoc f) {
        String l = f.getLabel();
        if (l != null && !l.isBlank()) return l;
        String n = f.getName();
        return n != null ? n : "-";
    }

    private String typeOrTag(FieldDoc f) {
        String t = f.getType();
        if (t != null && !t.isBlank()) return t;
        return "campo";
    }

    private PageDoc findByTemplate(List<PageDoc> docs, String template) {
        return docs.stream().filter(d -> template.equals(d.getTemplate())).findFirst().orElse(null);
    }

    private Path path(String rel) {
        return Paths.get(System.getProperty("user.dir")).resolve(rel.replace("\\", "/"));
    }

    private void write(Path file, String md) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, md.getBytes(StandardCharsets.UTF_8));
    }
}

