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

        // Sempre injetamos o mapa do sistema para garantir que a IA conheça a navegação mesmo com resultados do RAG
        contexto.append("\n\n").append(buildSystemStructureContext());

        String prompt = "Você é o Assistente Inteligente do Portal CEO. Responda a dúvida do usuário de forma direta e útil.\n" +
                        "Use o contexto fornecido abaixo. Se o contexto não tiver detalhes exatos, use seu conhecimento geral sobre sistemas ERP corporativos para dar uma orientação plausível, mas avise que é uma sugestão geral.\n" +
                        "NUNCA responda apenas 'Vou orientar seu uso com base no conteúdo encontrado'. Dê a resposta real.\n\n" +
                        "Estrutura da resposta desejada:\n" +
                        "1. Resumo direto da solução.\n" +
                        "2. Caminho de menu provável (ex: RH > Férias > Solicitar).\n" +
                        "3. Passo a passo simples.\n\n" +
                        "Pergunta: " + query + "\n\n" +
                        "Contexto:\n" + contexto.toString();

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
            
            // Aumentar temperatura para respostas menos robóticas, mas controladas
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.3); 
            req.put("options", options);

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
            // Em caso de erro na IA, tentamos usar o mapa estático para responder
            String fallback = tryStaticFallback(query);
            if (fallback != null) {
                respostaGerada = fallback;
                abrirChamadoSugerido = false;
                confianca = 0.8;
            } else if (!sugestoes.isEmpty()) {
                 respostaGerada = "Encontrei alguns tópicos que podem ajudar: " + sugestoes.get(0).get("titulo");
                 abrirChamadoSugerido = false;
                 confianca = 0.5;
            } else {
                 respostaGerada = "Não consegui processar sua solicitação com a IA no momento. Tente buscar por termos como 'Férias', 'Senha' ou 'Acesso' na barra de busca.";
                 abrirChamadoSugerido = true;
                 confianca = 0.2;
            }
        }

        AjudaConversaIa conv = new AjudaConversaIa();
        conv.setUsuario(usuario);
        conv.setPergunta(query);
        conv.setResposta(respostaGerada);
        conv.setConfianca(confianca);
        conv.setEscalonado(false);
        conversaRepo.save(conv);

        // Simplificação do buildSections para evitar sobrescrever a resposta da IA com templates vazios
        Map<String,Object> sections = new LinkedHashMap<>();
        sections.put("resumo", respostaGerada); 

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

    private String tryStaticFallback(String query) {
        String q = query.toLowerCase();
        if (q.contains("férias") || q.contains("ferias")) return "Para solicitar ou gerenciar férias, acesse o menu **RH > Férias** (`/ferias`). Lá você encontra opções para solicitar, aprovar e consultar histórico.";
        if (q.contains("cliente")) return "Para gerenciar clientes, acesse o menu **Comercial > Clientes** (`/clientes`). Você pode listar, criar e editar registros de clientes.";
        if (q.contains("senha") || q.contains("acesso")) return "Para alterar sua senha ou dados pessoais, vá em **Pessoal > Meu Perfil** (`/perfil`). Para problemas de acesso, abra um chamado em **Suporte > Abrir Chamado**.";
        if (q.contains("chamado") || q.contains("suporte")) return "Para abrir um chamado de suporte ou reportar erros, acesse **Suporte > Abrir Chamado** (`/chamados/novo`).";
        if (q.contains("usuario") || q.contains("usuário")) return "Para gerenciar usuários (apenas Administradores), acesse **Administração > Usuários** (`/usuarios/index`).";
        if (q.contains("ponto") || q.contains("folha")) return "Para consultar ponto ou folha de pagamento, acesse o menu **RH > Ponto** ou **RH > Folha**.";
        if (q.contains("fornecedor")) return "Para gerenciar fornecedores, acesse **Gestão > Fornecedores** (`/fornecedores`).";
        return null;
    }

    private String buildSystemStructureContext() {
        return "Mapa Completo do Sistema (Use para indicar caminhos):\n" +
               "1. Módulo PRINCIPAL:\n" +
               "   - Dashboard: /dashboard (Visão geral, Gráficos)\n\n" +
               "2. Módulo COMERCIAL:\n" +
               "   - Clientes: /clientes (Listar, Criar, Editar Clientes)\n" +
               "   - Oportunidades: /oportunidades (Funil de Vendas)\n\n" +
               "3. Módulo OPERACIONAL:\n" +
               "   - Projetos: /projetos (Gestão de Projetos)\n" +
               "   - Tarefas: /tarefas (Minhas Tarefas, Kanban)\n\n" +
               "4. Módulo RH (Recursos Humanos):\n" +
               "   - Colaboradores: /colaboradores (Cadastro de Funcionários)\n" +
               "   - Férias: /ferias (Solicitar Férias, Aprovar Férias)\n" +
               "   - Ponto: /ponto (Registro de Ponto, Espelho)\n" +
               "   - Folha: /folha (Holerites, Pagamentos)\n\n" +
               "5. Módulo GESTÃO:\n" +
               "   - Contratos: /contratos (Gestão Contratual)\n" +
               "   - Fornecedores: /fornecedores (Base de Fornecedores)\n\n" +
               "6. Módulo SERVIÇOS:\n" +
               "   - Gerenciar Serviços: /servicos\n" +
               "   - Catálogo: /servicos/catalogo\n\n" +
               "7. Módulo ADMINISTRAÇÃO (Apenas Admins):\n" +
               "   - Usuários: /usuarios/index (Criar Usuário, Resetar Senha)\n" +
               "   - Gestão de Acesso > Perfis: /perfis (Criar Perfil, Definir Permissões)\n" +
               "   - Gestão de Acesso > Permissões: /permissoes\n\n" +
               "8. Módulo PESSOAL:\n" +
               "   - Meu Perfil: /perfil (Alterar Senha, Dados Pessoais)\n" +
               "   - Meus Chamados: /meus-chamados (Acompanhar solicitações)\n\n" +
               "9. Módulo SUPORTE:\n" +
               "   - Documentos: /documentos\n" +
               "   - Abrir Chamado: /chamados/novo (Reportar Erro, Solicitar Acesso)\n";
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
