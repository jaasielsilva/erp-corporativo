package com.jaasielsilva.portalceo.service.ajuda;

import com.jaasielsilva.portalceo.model.ajuda.AjudaConteudo;
import com.jaasielsilva.portalceo.model.ajuda.AjudaEmbedding;
import com.jaasielsilva.portalceo.repository.ajuda.AjudaConteudoRepository;
import com.jaasielsilva.portalceo.repository.ajuda.AjudaEmbeddingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {
    @Autowired private AjudaConteudoRepository conteudoRepo;
    @Autowired private AjudaEmbeddingRepository embeddingRepo;
    private final WebClient webClient = WebClient.builder().build();

    @Value("${ollama.enabled:false}")
    private boolean ollamaEnabled;
    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    @Value("${ollama.embedModel:nomic-embed-text}")
    private String embedModel;
    @Value("${rag.allowedCategories:chamados}")
    private String allowedCategoriesCsv;

    private Set<String> allowedCategories() {
        String csv = allowedCategoriesCsv == null ? "" : allowedCategoriesCsv;
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<AjudaConteudo> searchSimilar(String query, int topN) {
        if (query == null || query.isBlank()) return topPublished(topN);
        float[] qv = embedText(query);
        if (qv == null) return topPublished(topN);
        Map<AjudaConteudo, Double> scores = new LinkedHashMap<>();
        Pageable pageable = PageRequest.of(0, 500);
        Page<AjudaConteudo> published = conteudoRepo.findByPublicadoTrue(pageable);
        Set<String> allow = allowedCategories();
        for (AjudaConteudo c : published) {
            String slug = c.getCategoria() != null ? c.getCategoria().getSlug() : null;
            if (!allow.isEmpty() && (slug == null || !allow.contains(slug))) continue;
            List<AjudaEmbedding> chunks = ensureIndexed(c);
            double best = -1.0;
            for (AjudaEmbedding e : chunks) {
                float[] ev = toFloatArray(e.getEmbedding());
                double s = cosine(qv, ev);
                if (s > best) best = s;
            }
            if (best >= 0) scores.put(c, best);
        }
        return scores.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(Math.max(topN, 1))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<AjudaConteudo> topPublished(int topN) {
        Pageable pageable = PageRequest.of(0, Math.max(topN, 1));
        return conteudoRepo.findByPublicadoTrue(pageable).getContent();
    }

    public List<AjudaEmbedding> ensureIndexed(AjudaConteudo conteudo) {
        List<AjudaEmbedding> existing = embeddingRepo.findByConteudoOrderByChunkIndexAsc(conteudo);
        if (!existing.isEmpty()) return existing;
        Set<String> allow = allowedCategories();
        String slug = conteudo.getCategoria() != null ? conteudo.getCategoria().getSlug() : null;
        if (!allow.isEmpty() && (slug == null || !allow.contains(slug))) return existing;
        if (!ollamaEnabled) return existing;
        String text = buildIndexText(conteudo);
        List<String> chunks = chunk(text, 800, 120);
        List<AjudaEmbedding> saved = new ArrayList<>();
        int idx = 0;
        for (String ch : chunks) {
            float[] v = embedText(ch);
            if (v == null) continue;
            AjudaEmbedding emb = new AjudaEmbedding();
            emb.setConteudo(conteudo);
            emb.setChunkIndex(idx++);
            emb.setEmbedding(toBytes(v));
            emb.setDim(v.length);
            emb.setArea(conteudo.getCategoria() != null ? conteudo.getCategoria().getSlug() : null);
            saved.add(embeddingRepo.save(emb));
        }
        return saved;
    }

    public Map<String,Object> reindexAllowed(boolean force) {
        Map<String,Object> stats = new LinkedHashMap<>();
        int processed = 0;
        int indexed = 0;
        int skipped = 0;
        Pageable pageable = PageRequest.of(0, 5000);
        Page<AjudaConteudo> published = conteudoRepo.findByPublicadoTrue(pageable);
        Set<String> allow = allowedCategories();
        for (AjudaConteudo c : published) {
            String slug = c.getCategoria() != null ? c.getCategoria().getSlug() : null;
            if (!allow.isEmpty() && (slug == null || !allow.contains(slug))) { skipped++; continue; }
            processed++;
            List<AjudaEmbedding> existing = embeddingRepo.findByConteudoOrderByChunkIndexAsc(c);
            if (force && !existing.isEmpty()) embeddingRepo.deleteAll(existing);
            List<AjudaEmbedding> after = ensureIndexed(c);
            indexed += after.size();
        }
        stats.put("processed", processed);
        stats.put("indexedVectors", indexed);
        stats.put("skipped", skipped);
        stats.put("allowedCategories", new ArrayList<>(allow));
        stats.put("embedModel", embedModel);
        stats.put("ollamaEnabled", ollamaEnabled);
        return stats;
    }

    private String buildIndexText(AjudaConteudo c) {
        StringBuilder sb = new StringBuilder();
        if (c.getTitulo() != null) sb.append(c.getTitulo()).append("\n\n");
        if (c.getCorpo() != null) sb.append(c.getCorpo());
        String t = sb.toString();
        return t.length() > 8000 ? t.substring(0, 8000) : t;
    }

    private List<String> chunk(String text, int size, int overlap) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) return out;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            out.add(text.substring(start, end));
            if (end >= text.length()) break;
            start = end - Math.min(overlap, size);
        }
        return out;
    }

    private float[] embedText(String text) {
        try {
            if (!ollamaEnabled) return null;
            Map<String,Object> req = new LinkedHashMap<>();
            req.put("model", embedModel);
            req.put("prompt", text);
            Map<?,?> resp = webClient.post()
                    .uri(ollamaUrl + "/api/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .blockOptional()
                    .orElse(null);
            if (resp == null) return null;
            Object vecObj = resp.get("embedding");
            if (!(vecObj instanceof List<?> list)) return null;
            float[] v = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                v[i] = (o instanceof Number n) ? n.floatValue() : 0f;
            }
            normalize(v);
            return v;
        } catch (Exception e) {
            return null;
        }
    }

    private void normalize(float[] v) {
        double sum = 0.0;
        for (float x : v) sum += x * x;
        double norm = Math.sqrt(sum);
        if (norm == 0) return;
        for (int i = 0; i < v.length; i++) v[i] = (float)(v[i] / norm);
    }

    private double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return -1.0;
        double dot = 0.0;
        for (int i = 0; i < a.length; i++) dot += a[i] * b[i];
        return dot;
    }

    private byte[] toBytes(float[] v) {
        ByteBuffer buf = ByteBuffer.allocate(v.length * 4);
        for (float x : v) buf.putFloat(x);
        return buf.array();
    }

    private float[] toFloatArray(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int n = bytes.length / 4;
        float[] v = new float[n];
        for (int i = 0; i < n; i++) v[i] = buf.getFloat();
        return v;
    }
}
