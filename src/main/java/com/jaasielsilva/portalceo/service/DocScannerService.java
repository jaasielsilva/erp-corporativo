package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.PageDoc;
import com.jaasielsilva.portalceo.model.FieldDoc;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DocScannerService {
    private static final Pattern CLASS_REQUEST = Pattern.compile("@RequestMapping\\(\"([^\"]+)\"\\)");
    private static final Pattern METHOD_GET = Pattern.compile("@GetMapping\\(\"?([^\")}]*)\"?\\)");
    private static final Pattern METHOD_POST = Pattern.compile("@PostMapping\\(\"?([^\")}]*)\"?\\)");
    private static final Pattern RETURN_VIEW = Pattern.compile("return\\s+\"([^\"]+)\"\\s*;");
    private static final Pattern PREAUTHORIZE = Pattern.compile("@PreAuthorize\\(\"([^\"]+)\"\\)");

    public List<PageDoc> scan() {
        Path root = Paths.get(System.getProperty("user.dir"));
        Path javaRoot = root.resolve("src/main/java");
        Path templatesRoot = root.resolve("src/main/resources/templates");
        List<PageDoc> docs = new ArrayList<>();

        Map<String, TemplateInfo> templates = scanTemplates(templatesRoot);
        List<ControllerInfo> controllers = scanControllers(javaRoot);

        for (ControllerInfo ci : controllers) {
            for (ControllerMethod cm : ci.methods) {
                String view = cm.view;
                TemplateInfo ti = view != null ? templates.get(view) : null;
                if (ti == null) {
                    // Ignora endpoints que não retornam um template Thymeleaf válido
                    continue;
                }
                String url = normalizeUrl(ci.basePath, cm.path);
                String httpMethod = cm.httpMethod;
                PageDoc pd = new PageDoc();
                pd.setUrl(url);
                pd.setHttpMethod(httpMethod);
                pd.setTemplate(view);
                pd.setPermissions(cm.permissions);
                pd.setTitle(ti.title);
                pd.setPurpose(ti.title);
                pd.setComponents(new ArrayList<>(ti.components));
                pd.setElements(new ArrayList<>(ti.elements));
                pd.setNavigationLinks(new ArrayList<>(ti.navigationLinks));
                pd.setLastUpdated(ti.lastUpdated);
                pd.setScreenshotPath(ti.screenshotPath);
                pd.setFields(new ArrayList<>(ti.fields));
                docs.add(pd);
            }
        }

        // Add orphan templates without explicit controller mapping
        Set<String> mappedViews = docs.stream().map(PageDoc::getTemplate).filter(Objects::nonNull).collect(Collectors.toSet());
        templates.forEach((view, ti) -> {
            if (!mappedViews.contains(view)) {
                PageDoc pd = new PageDoc();
                pd.setUrl("(não mapeado)");
                pd.setHttpMethod("GET");
                pd.setTemplate(view);
                pd.setTitle(ti.title);
                pd.setPurpose(ti.title);
                pd.setComponents(new ArrayList<>(ti.components));
                pd.setElements(new ArrayList<>(ti.elements));
                pd.setNavigationLinks(new ArrayList<>(ti.navigationLinks));
                pd.setLastUpdated(ti.lastUpdated);
                pd.setScreenshotPath(ti.screenshotPath);
                pd.setPermissions("isAuthenticated()");
                docs.add(pd);
            }
        });

        docs.sort(Comparator.comparing(d -> Optional.ofNullable(d.getUrl()).orElse("")));
        return docs;
    }

    private Map<String, TemplateInfo> scanTemplates(Path templatesRoot) {
        Map<String, TemplateInfo> map = new HashMap<>();
        try {
            Files.walk(templatesRoot)
                    .filter(p -> p.toString().endsWith(".html"))
                    .forEach(p -> {
                        String rel = templatesRoot.relativize(p).toString().replace("\\", "/");
                        String viewName = rel.replace(".html", "");
                        String html = readFileSafe(p);
                        TemplateInfo ti = new TemplateInfo();
                        ti.viewName = viewName;
                        ti.title = extractTitle(html);
                        ti.components = extractComponents(html);
                        ti.elements = extractElements(html);
                        ti.navigationLinks = extractLinks(html);
                        ti.fields = extractFields(html);
                        try {
                            ti.lastUpdated = Files.getLastModifiedTime(p).toInstant();
                        } catch (IOException e) {
                            ti.lastUpdated = Instant.now();
                        }
                        ti.screenshotPath = "/docs/screenshots/" + slug(viewName) + ".png";
                        map.put(viewName, ti);
                    });
        } catch (IOException ignored) {}
        return map;
    }

    private List<ControllerInfo> scanControllers(Path javaRoot) {
        List<ControllerInfo> list = new ArrayList<>();
        try {
            Files.walk(javaRoot)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        String src = readFileSafe(p);
                        if (src.contains("@Controller")) {
                            ControllerInfo ci = new ControllerInfo();
                            ci.path = p;
                            ci.basePath = matchOne(CLASS_REQUEST, src, "/");
                            ci.methods = new ArrayList<>();
                            Matcher mReturn = RETURN_VIEW.matcher(src);
                            List<Integer> returnIdx = new ArrayList<>();
                            while (mReturn.find()) returnIdx.add(mReturn.start());

                            Matcher mGet = METHOD_GET.matcher(src);
                            while (mGet.find()) {
                                ControllerMethod cm = new ControllerMethod();
                                cm.httpMethod = "GET";
                                cm.path = sanitizePath(mGet.group(1));
                                cm.view = nearestReturnView(mGet.start(), src);
                                cm.permissions = nearestPreAuthorize(mGet.start(), src);
                                ci.methods.add(cm);
                            }
                            Matcher mPost = METHOD_POST.matcher(src);
                            while (mPost.find()) {
                                ControllerMethod cm = new ControllerMethod();
                                cm.httpMethod = "POST";
                                cm.path = sanitizePath(mPost.group(1));
                                cm.view = nearestReturnView(mPost.start(), src);
                                cm.permissions = nearestPreAuthorize(mPost.start(), src);
                                ci.methods.add(cm);
                            }
                            list.add(ci);
                        }
                    });
        } catch (IOException ignored) {}
        return list;
    }

    private String nearestReturnView(int fromIndex, String src) {
        Matcher m = RETURN_VIEW.matcher(src);
        int bestDist = Integer.MAX_VALUE;
        String bestView = null;
        while (m.find()) {
            int d = Math.abs(m.start() - fromIndex);
            if (d < bestDist) {
                bestDist = d;
                bestView = m.group(1);
            }
        }
        return bestView;
    }

    private String nearestPreAuthorize(int fromIndex, String src) {
        Matcher m = PREAUTHORIZE.matcher(src);
        int bestDist = Integer.MAX_VALUE;
        String best = null;
        while (m.find()) {
            int d = Math.abs(m.start() - fromIndex);
            if (d < bestDist) {
                bestDist = d;
                best = m.group(1);
            }
        }
        return best != null ? best : "isAuthenticated()";
    }

    private static String matchOne(Pattern p, String src, String def) {
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1) : def;
    }

    private static String sanitizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String normalizeUrl(String base, String path) {
        if (base == null) base = "/";
        if (!base.startsWith("/")) base = "/" + base;
        if (path == null) path = "/";
        if (!path.startsWith("/")) path = "/" + path;
        if ("/".equals(path)) return base;
        if ("/".equals(base)) return path;
        return base + path;
    }

    private String readFileSafe(Path p) {
        try {
            return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private String extractTitle(String html) {
        Matcher m = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        if (m.find()) return stripTags(m.group(1)).trim();
        m = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        if (m.find()) return stripTags(m.group(1)).trim();
        return null;
    }

    private List<String> extractComponents(String html) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("th:(include|replace)\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(html);
        while (m.find()) out.add(m.group(2));
        if (html.contains("components/topbar")) out.add("components/topbar");
        if (html.contains("components/sidebar")) out.add("components/sidebar");
        return dedup(out);
    }

    private List<String> extractElements(String html) {
        List<String> out = new ArrayList<>();
        if (html.contains("<form")) out.add("form");
        if (html.contains("<table")) out.add("table");
        if (html.contains("modal")) out.add("modal");
        if (html.contains("<input")) out.add("input");
        if (html.contains("<select")) out.add("select");
        if (html.contains("<button")) out.add("button");
        return dedup(out);
    }

    private List<String> extractLinks(String html) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(html);
        while (m.find()) {
            String href = m.group(1);
            if (href.startsWith("/")) out.add(href);
        }
        return dedup(out);
    }

    private List<String> dedup(List<String> in) {
        return in.stream().filter(Objects::nonNull).map(String::trim).distinct().collect(Collectors.toList());
    }

    private String stripTags(String s) {
        return s.replaceAll("<[^>]+>", "");
    }

    private String slug(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replace("/", "_").replaceAll("[^a-z0-9_\\-]", "");
    }

    static class TemplateInfo {
        String viewName;
        String title;
        List<String> components;
        List<String> elements;
        List<String> navigationLinks;
        Instant lastUpdated;
        String screenshotPath;
        List<FieldDoc> fields;
    }

    static class ControllerInfo {
        Path path;
        String basePath;
        List<ControllerMethod> methods;
    }

    static class ControllerMethod {
        String httpMethod;
        String path;
        String view;
        String permissions;
    }

    private List<FieldDoc> extractFields(String html) {
        List<FieldDoc> out = new ArrayList<>();
        Pattern groupPattern = Pattern.compile("<div\\s+class=\\\"form-group[^\"]*\\\">([\\s\\S]*?)</div>", Pattern.CASE_INSENSITIVE);
        Matcher gm = groupPattern.matcher(html);
        while (gm.find()) {
            String block = gm.group(1);
            String label = null;
            Matcher lm = Pattern.compile("<label[^>]*>([\\s\\S]*?)</label>", Pattern.CASE_INSENSITIVE).matcher(block);
            if (lm.find()) {
                label = stripTags(lm.group(1)).trim();
            }
            Matcher im = Pattern.compile("<(input|select|textarea)([^>]*)>", Pattern.CASE_INSENSITIVE).matcher(block);
            if (im.find()) {
                String tag = im.group(1).toLowerCase(Locale.ROOT);
                String attrs = im.group(2);
                FieldDoc fd = new FieldDoc();
                fd.setLabel(label);
                fd.setType(extractAttr(attrs, "type"));
                fd.setId(extractAttr(attrs, "id"));
                String thField = extractAttr(attrs, "th:field");
                if (thField != null) {
                    fd.setName(thField.replace("*{", "").replace("}", "").trim());
                } else {
                    fd.setName(extractAttr(attrs, "name"));
                }
                fd.setPlaceholder(extractAttr(attrs, "placeholder"));
                String ml = extractAttr(attrs, "maxlength");
                fd.setMaxLength(ml != null ? parseInt(ml) : null);
                fd.setRequired(attrs != null && attrs.toLowerCase(Locale.ROOT).contains("required"));
                String disabled = extractAttr(attrs, "th:disabled");
                fd.setDisabledExpr(disabled);
                out.add(fd);
            }
        }
        return out;
    }

    private String extractAttr(String attrs, String name) {
        if (attrs == null) return null;
        Matcher m = Pattern.compile(name + "\\s*=\\s*\\\"([^\\\"]*)\\\"", Pattern.CASE_INSENSITIVE).matcher(attrs);
        if (m.find()) return m.group(1);
        return null;
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
}
