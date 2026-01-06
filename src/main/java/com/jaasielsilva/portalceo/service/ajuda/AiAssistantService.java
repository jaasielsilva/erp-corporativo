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
    @Autowired private com.jaasielsilva.portalceo.service.MapaPermissaoService mapaPermissaoService;
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

        String q = query == null ? "" : query.toLowerCase();
        boolean isAccessQuery = q.contains("acesso") || q.contains("permiss");
        String modulo = null;
        if (q.contains("financeiro")) modulo = "Financeiro";
        else if (q.contains("vendas")) modulo = "Vendas";
        else if (q.contains("rh")) modulo = "Recursos Humanos";
        else if (q.contains("compras")) modulo = "Compras";
        else if (q.contains("estoque")) modulo = "Estoque";
        else if (q.contains("marketing")) modulo = "Marketing";
        else if (q.contains("ti")) modulo = "Tecnologia";
        else if (q.contains("jurídico") || q.contains("juridico")) modulo = "Jurídico";
        else if (q.contains("clientes")) modulo = "Clientes";
        else if (q.contains("fornecedores")) modulo = "Fornecedores";
        else if (q.contains("produtos")) modulo = "Produtos";

        if (isAccessQuery) {
            caminho = "Serviços → Solicitações → Nova Solicitação";
            passos.add("Acesse Serviços → Solicitações → Nova Solicitação");
            if (modulo != null) passos.add("Selecione o módulo " + modulo);
            passos.add("Informe subníveis necessários usando Authorities");
            passos.add("Explique a justificativa e envie para aprovação");
            observacoes.add("Use o menor privilégio possível");
            observacoes.add("Aprovação parcial pode ser aplicada");
            resumo = "Vou orientar seu uso com base no conteúdo encontrado.";
            perguntaFinal = "Quer que eu abra um chamado ou detalhe outro processo?";
        } else if (!context.isEmpty()) {
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
                    } else if (t.startsWith("- ") || t.startsWith("* ")) {
                        String step = t.substring(2).trim();
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
        
        // Tenta usar o Mapa de Permissões para responder dinamicamente
        try {
            mapaPermissaoService.sincronizarPermissoes();
            List<com.jaasielsilva.portalceo.model.MapaPermissao> mapa = mapaPermissaoService.listarTodos();
            
            // Lógica para responder "Qual permissão precisa para X?" ou perguntas de "Como fazer X?"
            if (q.contains("permissão") || q.contains("permissao") || q.contains("authority") || q.contains("acesso") ||
                q.contains("cadastrar") || q.contains("novo") || q.contains("criar") || q.contains("onde") || q.contains("como") ||
                q.contains("gerenciar") || q.contains("visualizar")) {
                // Palavras irrelevantes para a busca (stop words)
                List<String> stopWords = Arrays.asList(
                    "qual", "que", "permissão", "permissao", "precisa", "necessária", "necessaria", 
                    "para", "acessar", "a", "o", "as", "os", "de", "do", "da", "em", "no", "na", 
                    "tela", "menu", "modulo", "módulo", "sistema", "ter", "tal", "uma", "um",
                    "pra", "pro", "perfil", "acesso", "usuario", "usuário"
                );
                
                // Expansão de sinônimos para melhorar a busca
                Map<String, List<String>> sinonimos = new HashMap<>();
                sinonimos.put("cadastrar", Arrays.asList("novo", "criar", "adicionar", "incluir", "inserir", "create", "new", "add"));
                sinonimos.put("editar", Arrays.asList("alterar", "atualizar", "modificar", "corrigir", "update", "edit"));
                sinonimos.put("excluir", Arrays.asList("remover", "deletar", "apagar", "cancelar", "delete", "remove"));
                sinonimos.put("listar", Arrays.asList("ver", "visualizar", "consultar", "buscar", "pesquisar", "list", "read", "view", "index"));
                sinonimos.put("clientes", Arrays.asList("cliente", "consumidor"));
                sinonimos.put("colaboradores", Arrays.asList("colaborador", "funcionario", "funcionário", "equipe", "time"));
                sinonimos.put("usuarios", Arrays.asList("usuario", "usuário"));
                sinonimos.put("fornecedores", Arrays.asList("fornecedor", "parceiro"));
                sinonimos.put("produtos", Arrays.asList("produto", "item", "mercadoria"));
                sinonimos.put("relatorio", Arrays.asList("exportar", "imprimir", "gerar", "report"));

                String[] tokens = q.split("\\s+");
                List<String> termosRelevantes = new ArrayList<>();
                for (String token : tokens) {
                    String limpo = token.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚàÀãÃõÕçÇêÊôÔ]", "").toLowerCase();
                    if (limpo.length() > 2 && !stopWords.contains(limpo)) {
                        termosRelevantes.add(limpo);
                        // Adiciona sinônimos se houver
                        if (sinonimos.containsKey(limpo)) {
                            termosRelevantes.addAll(sinonimos.get(limpo));
                        }
                        // Busca reversa: se o token for um dos valores do mapa, adiciona a chave (canonicalização)
                        for (Map.Entry<String, List<String>> entry : sinonimos.entrySet()) {
                            if (entry.getValue().contains(limpo)) {
                                termosRelevantes.add(entry.getKey());
                            }
                        }
                    }
                }
                                     
                if (!termosRelevantes.isEmpty()) {
                    com.jaasielsilva.portalceo.model.MapaPermissao melhorMatch = null;
                    int melhorScore = 0;
                    
                    for (com.jaasielsilva.portalceo.model.MapaPermissao item : mapa) {
                        int score = 0;
                        String rec = item.getRecurso().toLowerCase();
                        String desc = item.getDescricao().toLowerCase();
                        String mod = item.getModulo().toLowerCase();
                        String perm = item.getPermissao().toLowerCase();
                        
                        for (String termo : termosRelevantes) {
                            if (rec.contains(termo)) score += 10;
                            if (desc.contains(termo)) score += 8; // Aumentado peso da descrição
                            if (mod.contains(termo)) score += 2;
                            if (perm.contains(termo)) score += 10; // Alta relevância se bater na permissão técnica
                            
                            // Bônus para match exato de palavra inteira (evita "menu" dar match em "menu_clientes" com score baixo)
                            if (rec.equals(termo) || perm.equals(termo)) score += 15;
                        }
                        
                        // Penalidade para itens muito genéricos se a busca for específica
                        if (termosRelevantes.size() > 1 && (rec.equals("menu geral") || rec.equals("menu " + mod))) {
                             score -= 5;
                        }
                        
                        if (score > melhorScore) {
                            melhorScore = score;
                            melhorMatch = item;
                        }
                    }
                    
                    if (melhorMatch != null && melhorScore >= 10) { // Aumentado threshold mínimo para evitar falsos positivos fracos
                         StringBuilder sb = new StringBuilder();
                         sb.append("✅ **Permissão Encontrada**\n\n");
                         sb.append("Para acessar **").append(melhorMatch.getRecurso()).append("** (").append(melhorMatch.getModulo()).append("), é necessário:\n\n");
                         sb.append("🔑 **Permissão:** `").append(melhorMatch.getPermissao()).append("`\n");
                         
                         if (melhorMatch.getPerfis() != null && !melhorMatch.getPerfis().isEmpty()) {
                             sb.append("👤 **Perfis com acesso:** ").append(melhorMatch.getPerfis()).append("\n");
                         } else {
                             sb.append("👤 **Perfis:** Nenhum perfil padrão configurado explicitamente.\n");
                         }
                         
                         sb.append("\n_Caso precise deste acesso, solicite ao administrador informando o código da permissão acima._");
                         return sb.toString();
                    }
                }
            }
            
            // Identifica o módulo na pergunta
            String moduloAlvo = null;

            // Lógica específica para pergunta sobre Cards do Dashboard
            if (q.contains("dashboard") && (q.contains("card") || q.contains("cards") || q.contains("permiss"))) {
                StringBuilder sb = new StringBuilder();
                sb.append("📊 **Permissões do Dashboard**\n\n");
                sb.append("Sim, os cards da tela principal estão mapeados. Aqui estão as permissões necessárias para cada seção:\n\n");
                
                for (com.jaasielsilva.portalceo.model.MapaPermissao item : mapa) {
                    if (item.getModulo().equalsIgnoreCase("Dashboard") || 
                       (item.getModulo().equalsIgnoreCase("RH") && item.getRecurso().contains("Card")) ||
                       (item.getRecurso().contains("Card") && item.getRecurso().contains("Dashboard"))) {
                        
                        sb.append("🔹 **").append(item.getRecurso()).append("**\n");
                        sb.append("   🔑 `").append(item.getPermissao()).append("`\n");
                        if (item.getPerfis() != null && !item.getPerfis().isEmpty()) {
                            sb.append("   👤 ").append(item.getPerfis()).append("\n");
                        }
                        sb.append("\n");
                    }
                }
                return sb.toString();
            }

            if (q.contains("rh") || q.contains("recursos humanos")) moduloAlvo = "RH";
            else if (q.contains("financeiro")) moduloAlvo = "Financeiro";
            else if (q.contains("comercial") || q.contains("clientes")) moduloAlvo = "Comercial";
            else if (q.contains("vendas")) moduloAlvo = "Vendas";
            else if (q.contains("marketing")) moduloAlvo = "Marketing";
            else if (q.contains("estoque") || q.contains("compras")) moduloAlvo = "Estoque";
            else if (q.contains("ti") || q.contains("tecnologia")) moduloAlvo = "TI";
            else if (q.contains("juridico") || q.contains("jurídico")) moduloAlvo = "Jurídico";
            else if (q.contains("projetos")) moduloAlvo = "Projetos";
            else if (q.contains("admin")) moduloAlvo = "Administração";
            
            // Se encontrou um módulo e a pergunta é sobre "opções", "menu", "o que tem", "listar"
            if (moduloAlvo != null && (q.contains("opç") || q.contains("menu") || q.contains("tem") || q.contains("func") || q.contains("quais"))) {
                StringBuilder resposta = new StringBuilder();
                resposta.append("Aqui estão as opções disponíveis no módulo **").append(moduloAlvo).append("**:\n\n");
                
                // Lista para armazenar itens únicos e formatados
                Set<String> itensUnicos = new TreeSet<>();
                
                for (com.jaasielsilva.portalceo.model.MapaPermissao item : mapa) {
                    if (item.getModulo().equalsIgnoreCase(moduloAlvo)) {
                        // Filtra apenas itens relevantes para o usuário final
                        if (item.getTipo() == com.jaasielsilva.portalceo.model.MapaPermissao.TipoRecurso.MENU || 
                            item.getTipo() == com.jaasielsilva.portalceo.model.MapaPermissao.TipoRecurso.TELA ||
                            item.getTipo() == com.jaasielsilva.portalceo.model.MapaPermissao.TipoRecurso.RELATORIO) {
                            
                            // Usa a descrição limpa se disponível, ou o recurso
                            String nomeExibicao = item.getDescricao();
                            if (nomeExibicao == null || nomeExibicao.contains("Permissão para menu") || nomeExibicao.startsWith("Menu ")) {
                                nomeExibicao = item.getRecurso();
                            }
                            
                            // Limpa prefixos comuns para melhorar a leitura
                            nomeExibicao = nomeExibicao.replace("Menu ", "").replace("Tela ", "");
                            
                            // Capitaliza primeira letra
                            if (nomeExibicao.length() > 0) {
                                nomeExibicao = nomeExibicao.substring(0, 1).toUpperCase() + nomeExibicao.substring(1);
                            }
                            
                            itensUnicos.add(nomeExibicao);
                        }
                    }
                }
                
                if (!itensUnicos.isEmpty()) {
                    for (String item : itensUnicos) {
                        // Ignora itens muito genéricos ou repetitivos
                        if (item.equalsIgnoreCase("Rh") || item.equalsIgnoreCase(moduloAlvo)) continue;
                        
                        resposta.append("- ").append(item).append("\n");
                    }
                    return resposta.toString();
                }
            }
        } catch (Exception e) {
            // Ignora erro e segue para fallback estático
        }

        if (q.contains("férias") || q.contains("ferias")) return "Para solicitar ou gerenciar férias, acesse o menu **RH > Férias** (`/ferias`). Lá você encontra opções para solicitar, aprovar e consultar histórico.";
        if (q.contains("cliente")) return "Para gerenciar clientes, acesse o menu **Comercial > Clientes** (`/clientes`). Você pode listar, criar e editar registros de clientes.";
        if (q.contains("senha") || q.contains("acesso")) return "Para alterar sua senha ou dados pessoais, vá em **Pessoal > Meu Perfil** (`/perfil`). Para problemas de acesso, abra um chamado em **Suporte > Abrir Chamado**.";
        if (q.contains("chamado") || q.contains("suporte")) return "Para abrir um chamado de suporte ou reportar erros, acesse **Suporte > Abrir Chamado** (`/chamados/novo`).";
        if (q.contains("usuario") || q.contains("usuário")) return "Para gerenciar usuários (apenas Administradores), acesse **Administração > Usuários** (`/usuarios/index`).";
        if (q.contains("ponto") || q.contains("folha")) return "Para consultar ponto ou folha de pagamento, acesse o menu **RH > Ponto** ou **RH > Folha**.";
        // if (q.contains("fornecedor")) return "Para gerenciar fornecedores, acesse **Gestão > Fornecedores** (`/fornecedores`).";
        return null;
    }

    private String buildSystemStructureContext() {
        try {
            // Tenta obter o mapa dinâmico do banco de dados
            mapaPermissaoService.sincronizarPermissoes(); // Garante dados frescos
            List<com.jaasielsilva.portalceo.model.MapaPermissao> mapa = mapaPermissaoService.listarTodos();
            
            if (mapa.isEmpty()) {
                return buildStaticSystemStructureContext();
            }
            
            // Agrupa por módulo
            Map<String, List<com.jaasielsilva.portalceo.model.MapaPermissao>> porModulo = new TreeMap<>();
            for (com.jaasielsilva.portalceo.model.MapaPermissao item : mapa) {
                porModulo.computeIfAbsent(item.getModulo(), k -> new ArrayList<>()).add(item);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Mapa Completo e Atualizado do Sistema (Baseado nas Permissões Reais):\n");
            
            for (Map.Entry<String, List<com.jaasielsilva.portalceo.model.MapaPermissao>> entry : porModulo.entrySet()) {
                sb.append("\n=== Módulo ").append(entry.getKey().toUpperCase()).append(" ===\n");
                
                // Agrupa por recurso para evitar repetições excessivas
                Map<String, String> recursosUnicos = new TreeMap<>();
                for (com.jaasielsilva.portalceo.model.MapaPermissao item : entry.getValue()) {
                    // Foca principalmente em Menus e Telas para navegação
                    if (item.getTipo() == com.jaasielsilva.portalceo.model.MapaPermissao.TipoRecurso.MENU || 
                        item.getTipo() == com.jaasielsilva.portalceo.model.MapaPermissao.TipoRecurso.TELA) {
                        recursosUnicos.put(item.getRecurso(), item.getDescricao());
                    }
                }
                
                for (Map.Entry<String, String> rec : recursosUnicos.entrySet()) {
                    sb.append("   - ").append(rec.getKey())
                      .append(": ").append(rec.getValue()).append("\n");
                }
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            // Fallback em caso de erro no banco
            return buildStaticSystemStructureContext();
        }
    }

    private String buildStaticSystemStructureContext() {
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
        if (slug.equalsIgnoreCase("documentacao")) {
            if (titulo != null && titulo.toLowerCase().contains("solicitar")) {
                return "Serviços → Solicitações → Nova Solicitação";
            }
            return "Menu → Documentação";
        }
        return "";
    }
}
