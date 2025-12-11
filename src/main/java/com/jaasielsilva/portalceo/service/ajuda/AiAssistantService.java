package com.jaasielsilva.portalceo.service.ajuda;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.ajuda.AjudaConversaIa;
import com.jaasielsilva.portalceo.model.ajuda.AjudaConteudo;
import com.jaasielsilva.portalceo.repository.ajuda.AjudaConversaIaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.*;

@Service
public class AiAssistantService {
    @Autowired private HelpService helpService;
    @Autowired private RagService ragService;
    @Autowired private AjudaConversaIaRepository conversaRepo;
    private final WebClient webClient = WebClient.builder().build();

    @Value("${ollama.enabled:false}")
    private boolean ollamaEnabled;
    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    public Map<String,Object> answer(String query, Usuario usuario) {
        List<AjudaConteudo> top = ragService.searchSimilar(query, 5);
        List<Map<String,Object>> sugestoes = new ArrayList<>();
        for (AjudaConteudo c : top) {
            Map<String,Object> s = new LinkedHashMap<>();
            s.put("id", c.getId());
            s.put("titulo", c.getTitulo());
            s.put("categoria", c.getCategoria().getSlug());
            sugestoes.add(s);
        }
        String resposta = sugestoes.isEmpty() ? "Não encontrei informações suficientes na base de conhecimento interna. Deseja registrar essa dúvida para melhoria futura?" : "Conteúdos relacionados encontrados.";
        Double confianca = sugestoes.isEmpty() ? 0.2 : 0.7;

        Map<String,Object> sections = buildSections(query, top, resposta, sugestoes);

        AjudaConversaIa conv = new AjudaConversaIa();
        conv.setUsuario(usuario);
        conv.setPergunta(query);
        conv.setResposta(resposta);
        conv.setConfianca(confianca);
        conv.setEscalonado(false);
        conversaRepo.save(conv);

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("resposta", resposta);
        out.put("confianca", confianca);
        out.put("sugestoes", sugestoes);
        out.put("conversaId", conv.getId());
        out.put("abrirChamadoSugerido", sugestoes.isEmpty());
        out.put("sections", sections);
        return out;
    }

    public Map<String,Object> generateWithOllama(String query, Usuario usuario) {
        List<AjudaConteudo> top = ragService.searchSimilar(query, 5);
        StringBuilder contexto = new StringBuilder();
        int i = 1;
        for (AjudaConteudo c : top) {
            contexto.append(i++).append(") ").append(c.getTitulo()).append(" [").append(c.getCategoria().getSlug()).append("]\n");
            String corpo = c.getCorpo();
            if (corpo != null) {
                String excerpt = corpo.length() > 600 ? corpo.substring(0, 600) + "..." : corpo;
                contexto.append(excerpt).append("\n\n");
            }
        }

        String prompt = "Você é o Assistente Inteligente do Portal CEO. Responda SEMPRE no formato abaixo, usando apenas o contexto fornecido. Se o contexto não for suficiente, responda: 'Não encontrei informações suficientes na base de conhecimento interna. Deseja registrar essa dúvida para melhoria futura?' e sugira abrir um chamado. Formato:\n\n### 🔹 Resumo rápido\n<uma frase>\n\n### 🔹 Caminho de navegação\nMenu → Módulo → Submódulo → Tela\n\n### 🔹 Passo a passo\n1) ...\n2) ...\n3) ...\n\n### 🔹 Observações úteis\n- ...\n- ...\n\n### 🔹 Pergunta final\n<pergunta>\n\nPergunta: " + query + "\n\nContexto:\n" + contexto.toString();

        String respostaGerada = null;
        Double confianca = 0.6;
        List<Map<String,Object>> sugestoes = new ArrayList<>();
        for (AjudaConteudo c : top) {
            Map<String,Object> s = new LinkedHashMap<>();
            s.put("id", c.getId());
            s.put("titulo", c.getTitulo());
            s.put("categoria", c.getCategoria().getSlug());
            sugestoes.add(s);
        }

        boolean abrirChamadoSugerido = false;
        try {
            if (!ollamaEnabled) throw new IllegalStateException("Ollama desativado");
            Map<String, Object> req = new LinkedHashMap<>();
            req.put("model", ollamaModel);
            req.put("prompt", prompt);
            req.put("stream", Boolean.FALSE);

            Map<?,?> resp = webClient.post()
                    .uri(ollamaUrl + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .blockOptional()
                    .orElse(null);
            if (resp != null && resp.get("response") instanceof String s) {
                respostaGerada = s.trim();
                abrirChamadoSugerido = respostaGerada.isBlank();
            }
        } catch (Exception e) {
            respostaGerada = (sugestoes.isEmpty() ? "Não encontrei informações suficientes na base de conhecimento interna. Deseja registrar essa dúvida para melhoria futura?" : "Conteúdos relacionados encontrados.");
            abrirChamadoSugerido = sugestoes.isEmpty();
            confianca = sugestoes.isEmpty() ? 0.2 : 0.7;
        }

        AjudaConversaIa conv = new AjudaConversaIa();
        conv.setUsuario(usuario);
        conv.setPergunta(query);
        conv.setResposta(respostaGerada);
        conv.setConfianca(confianca);
        conv.setEscalonado(false);
        conversaRepo.save(conv);

        Map<String,Object> sections = buildSections(query, top, respostaGerada, sugestoes);

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("resposta", respostaGerada);
        out.put("confianca", confianca);
        out.put("sugestoes", sugestoes);
        out.put("conversaId", conv.getId());
        out.put("abrirChamadoSugerido", abrirChamadoSugerido);
        out.put("usouOllama", ollamaEnabled);
        out.put("sections", sections);
        return out;
    }

    public AjudaConversaIa marcarEscalonado(Long conversaId) {
        AjudaConversaIa conv = conversaRepo.findById(convId(conversaId)).orElseThrow();
        conv.setEscalonado(true);
        return conversaRepo.save(conv);
    }

    private Long convId(Long id) { return id; }

    private Map<String,Object> buildSections(String query, List<AjudaConteudo> context, String respostaBase, List<Map<String,Object>> sugestoes) {
        Map<String,Object> sections = new LinkedHashMap<>();
        String resumo = respostaBase;
        String caminho = "";
        List<String> passos = new ArrayList<>();
        List<String> observacoes = new ArrayList<>();
        String perguntaFinal = "Deseja ajuda em outro processo?";

        if (!context.isEmpty()) {
            AjudaConteudo top = context.get(0);
            caminho = inferPathFromCategory(top.getCategoria() != null ? top.getCategoria().getSlug() : null, top.getTitulo());
            String corpo = top.getCorpo();
            if (corpo != null) {
                String[] lines = corpo.split("\n");
                for (String line : lines) {
                    String t = line.trim();
                    if (t.matches("^\\d+\\.\\s+.*")) {
                        String step = t.replaceFirst("^\\d+\\.\\s+", "").trim();
                        passos.add(step);
                    }
                }
                if (passos.isEmpty()) {
                    int idx = 0;
                    while (idx < lines.length && passos.size() < 5) {
                        String t = lines[idx++].trim();
                        if (!t.isBlank()) passos.add(t.length() > 160 ? t.substring(0, 160) : t);
                    }
                }
                if (corpo.toLowerCase().contains("dica:")) {
                    int p = corpo.toLowerCase().indexOf("dica:");
                    String dica = corpo.substring(p).replaceFirst("(?i)dica:\\s*", "").trim();
                    observacoes.add(dica);
                }
            }
            resumo = "Vou orientar seu uso com base no conteúdo encontrado.";
            perguntaFinal = "Quer que eu abra um chamado ou detalhe outro processo?";
        } else {
            resumo = respostaBase;
            caminho = "";
            passos = List.of("Informe o caminho do menu: Menu → Módulo → Submódulo → Tela", "Descreva o objetivo e o contexto", "Envie a mensagem de erro completa, se houver");
            observacoes = List.of("Responderei com base na informação interna", "Sem dados suficientes, posso abrir um chamado automaticamente");
            perguntaFinal = "Deseja registrar essa dúvida para melhoria futura?";
        }

        Map<String,Object> passoObj = new LinkedHashMap<>();
        passoObj.put("items", passos);

        Map<String,Object> obsObj = new LinkedHashMap<>();
        obsObj.put("items", observacoes);

        sections.put("resumo", resumo);
        sections.put("caminho", caminho);
        sections.put("passo_a_passo", passoObj);
        sections.put("observacoes", obsObj);
        sections.put("pergunta_final", perguntaFinal);
        return sections;
    }

    private String inferPathFromCategory(String slug, String titulo) {
        if (slug == null) return "";
        if (slug.equalsIgnoreCase("chamados")) return "Menu → Suporte → Chamados";
        if (slug.equalsIgnoreCase("impressoras")) return "Menu → Suporte → Chamados → Novo Chamado";
        if (slug.equalsIgnoreCase("usuarios-acesso")) return "Menu → Usuários → Acesso";
        if (slug.equalsIgnoreCase("relatorios")) return "Menu → Relatórios";
        return "";
    }
}
