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
    @Autowired private AjudaConversaIaRepository conversaRepo;
    private final WebClient webClient = WebClient.builder().build();

    @Value("${ollama.enabled:false}")
    private boolean ollamaEnabled;
    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    public Map<String,Object> answer(String query, Usuario usuario) {
        Page<AjudaConteudo> r = helpService.buscar(query, null, 0, 5, usuario);
        List<Map<String,Object>> sugestoes = new ArrayList<>();
        for (AjudaConteudo c : r.getContent()) {
            Map<String,Object> s = new LinkedHashMap<>();
            s.put("id", c.getId());
            s.put("titulo", c.getTitulo());
            s.put("categoria", c.getCategoria().getSlug());
            sugestoes.add(s);
        }
        String resposta = sugestoes.isEmpty() ? "Nenhum conteúdo encontrado." : "Conteúdos relacionados encontrados.";
        AjudaConversaIa conv = new AjudaConversaIa();
        conv.setUsuario(usuario);
        conv.setPergunta(query);
        conv.setResposta(resposta);
        conv.setConfianca(sugestoes.isEmpty() ? 0.2 : 0.7);
        conv.setEscalonado(false);
        conversaRepo.save(conv);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("resposta", resposta);
        out.put("confianca", conv.getConfianca());
        out.put("sugestoes", sugestoes);
        out.put("conversaId", conv.getId());
        out.put("abrirChamadoSugerido", sugestoes.isEmpty());
        return out;
    }

    public Map<String,Object> generateWithOllama(String query, Usuario usuario) {
        Page<AjudaConteudo> r = helpService.buscar(query, null, 0, 5, usuario);
        StringBuilder contexto = new StringBuilder();
        int i = 1;
        for (AjudaConteudo c : r.getContent()) {
            contexto.append(i++).append(") ").append(c.getTitulo()).append(" [").append(c.getCategoria().getSlug()).append("]\n");
            String corpo = c.getCorpo();
            if (corpo != null) {
                String excerpt = corpo.length() > 600 ? corpo.substring(0, 600) + "..." : corpo;
                contexto.append(excerpt).append("\n\n");
            }
        }

        String prompt = "Você é um assistente do ERP Corporativo. Responda em português de forma objetiva e passo a passo. Use apenas o contexto abaixo para responder. Se o contexto não for suficiente, diga que não há conteúdo suficiente e sugira abrir um chamado.\n\nPergunta: " + query + "\n\nContexto:\n" + contexto.toString();

        String respostaGerada = null;
        Double confianca = 0.6;
        List<Map<String,Object>> sugestoes = new ArrayList<>();
        for (AjudaConteudo c : r.getContent()) {
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
            respostaGerada = (sugestoes.isEmpty() ? "Nenhum conteúdo encontrado." : "Conteúdos relacionados encontrados.");
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

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("resposta", respostaGerada);
        out.put("confianca", confianca);
        out.put("sugestoes", sugestoes);
        out.put("conversaId", conv.getId());
        out.put("abrirChamadoSugerido", abrirChamadoSugerido);
        out.put("usouOllama", ollamaEnabled);
        return out;
    }

    public AjudaConversaIa marcarEscalonado(Long conversaId) {
        AjudaConversaIa conv = conversaRepo.findById(convId(conversaId)).orElseThrow();
        conv.setEscalonado(true);
        return conversaRepo.save(conv);
    }

    private Long convId(Long id) { return id; }
}
