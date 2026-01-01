package com.jaasielsilva.portalceo.controller.docs;

import com.jaasielsilva.portalceo.model.PageDoc;
import com.jaasielsilva.portalceo.service.DocScannerService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/documentacao")
public class DocsController {
    private final DocScannerService scanner;

    public DocsController(DocScannerService scanner) {
        this.scanner = scanner;
    }

    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "q", required = false) String q) {
        List<PageDoc> docs = scanner.scan();
        if (q != null && !q.isBlank()) {
            String ql = q.toLowerCase();
            docs = docs.stream().filter(d ->
                    (Optional.ofNullable(d.getUrl()).orElse("").toLowerCase().contains(ql)) ||
                    (Optional.ofNullable(d.getTemplate()).orElse("").toLowerCase().contains(ql)) ||
                    (Optional.ofNullable(d.getTitle()).orElse("").toLowerCase().contains(ql)) ||
                    (Optional.ofNullable(d.getPurpose()).orElse("").toLowerCase().contains(ql))
            ).collect(Collectors.toList());
        }
        List<Group> groups = toGroups(docs);
        model.addAttribute("docs", docs);
        model.addAttribute("groups", groups);
        model.addAttribute("lastUpdated", Instant.now());
        return "docs/index";
    }

    @GetMapping("/pagina")
    public String pagina(Model model,
                         @RequestParam("template") String template,
                         @RequestParam(value = "url", required = false) String url) {
        List<PageDoc> docs = scanner.scan();
        PageDoc doc = docs.stream()
                .filter(d -> template.equals(d.getTemplate()) && (url == null || url.equals(d.getUrl())))
                .findFirst()
                .orElseGet(() -> {
                    PageDoc p = new PageDoc();
                    p.setTemplate(template);
                    p.setUrl(url != null ? url : "(não mapeado)");
                    p.setTitle(template);
                    p.setPurpose(template);
                    return p;
                });
        model.addAttribute("doc", doc);
        model.addAttribute("lastUpdated", Instant.now());
        return "docs/pagina";
    }

    @PostMapping(value = "/upload-screenshot", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String uploadScreenshot(@RequestParam("template") String template,
                                   @RequestParam("imageBase64") String imageBase64) throws IOException {
        byte[] png = Base64.getDecoder().decode(imageBase64.replaceFirst("^data:image/[^;]+;base64,", ""));
        Path dir = Paths.get(System.getProperty("user.dir"))
                .resolve("src/main/resources/static/docs/screenshots");
        Files.createDirectories(dir);
        Path file = dir.resolve(template.toLowerCase().replace("/", "_") + ".png");
        Files.write(file, png);
        return "redirect:/documentacao/pagina?template=" + template;
    }

    private List<Group> toGroups(List<PageDoc> docs) {
        Map<String, List<PageDoc>> byLabel = docs.stream()
                .collect(Collectors.groupingBy(d -> {
                    String t = Optional.ofNullable(d.getTemplate()).orElse("");
                    int i = t.indexOf('/');
                    return i > 0 ? t.substring(0, i) : t;
                }));
        List<Group> out = new ArrayList<>();
        for (Map.Entry<String, List<PageDoc>> e : byLabel.entrySet()) {
            Group g = new Group();
            g.label = e.getKey();
            g.id = "group-" + slug(e.getKey());
            g.docs = e.getValue();
            out.add(g);
        }
        out.sort((a,b) -> a.label.compareToIgnoreCase(b.label));
        return out;
    }

    private String slug(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase();
        lower = lower.replace("/", "-").replace(" ", "-");
        return lower.replaceAll("[^a-z0-9_-]", "");
    }

    public static class Group {
        public String label;
        public String id;
        public List<PageDoc> docs;
        public String getLabel() { return label; }
        public String getId() { return id; }
        public List<PageDoc> getDocs() { return docs; }
    }
}

