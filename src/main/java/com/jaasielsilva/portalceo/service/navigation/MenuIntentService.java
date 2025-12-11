package com.jaasielsilva.portalceo.service.navigation;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuIntentService {
    public static class IntentDef {
        public String id;
        public String label;
        public List<String> routes;
        public List<String> synonyms;
        public IntentDef(String id, String label, List<String> routes, List<String> synonyms) {
            this.id = id; this.label = label; this.routes = routes; this.synonyms = synonyms;
        }
    }

    private final List<IntentDef> intents;

    public MenuIntentService() {
        intents = new ArrayList<>();
        // 1. Principal / Sistema
        intents.add(new IntentDef("principal", "Principal/Sistema",
                List.of("/dashboard","/ajuda","/chat"),
                List.of("voltar para o inicio","dashboard","painel inicial","tela principal","home",
                        "ver notificacoes","campainha","alertas","chat","conversar com atendente",
                        "como usar o sistema","ajuda","faq","manual")));

        // 2. Comercial - Clientes
        intents.add(new IntentDef("clientes", "Clientes",
                List.of("/clientes","/clientes/cadastro","/clientes/contratos/listar","/clientes/historico/interacoes"),
                List.of("cadastrar cliente","novo cliente","editar cliente","buscar cliente","cliente duplicado",
                        "historico do cliente","contrato do cliente","dados cliente","interacoes cliente")));

        // 2. Comercial - Vendas
        intents.add(new IntentDef("vendas", "Vendas",
                List.of("/vendas/pdv","/vendas/pedidos","/vendas/relatorios"),
                List.of("registrar venda","fazer pedido","pdv","cupom","nota","vendas do mes",
                        "relatorio de vendas","abrir caixa","fechar caixa")));

        // 2. Comercial - Fornecedores
        intents.add(new IntentDef("fornecedores", "Fornecedores",
                List.of("/fornecedores"),
                List.of("cadastrar fornecedor","novo fornecedor","lista fornecedores","dados fornecedor","fornecedor atrasando")));

        // 3. Operacional - Produtos
        intents.add(new IntentDef("produtos", "Produtos",
                List.of("/produtos"),
                List.of("cadastrar produto","novo produto","produto faltando","produto errado","informacoes do produto","listar produtos")));

        // 3. Operacional - Estoque
        intents.add(new IntentDef("estoque", "Estoque",
                List.of("/estoque"),
                List.of("baixar estoque","entrada de estoque","saida de estoque","estoque travado","estoque zerado","item nao baixa","controle de estoque","inventario")));

        // 3. Operacional - Categorias
        intents.add(new IntentDef("categorias", "Categorias",
                List.of("/categorias","/categorias/nova"),
                List.of("criar categoria","nova categoria","editar categoria","tipo de produto")));

        // 4. RH - Colaboradores
        intents.add(new IntentDef("rh_colaboradores", "RH - Colaboradores",
                List.of("/rh/colaboradores/listar","/rh/colaboradores/novo","/rh/colaboradores/adesao"),
                List.of("cadastrar colaborador","novo funcionario","ficha do colaborador","dados do funcionario","admissao","incluir pessoa","ver lista de colaboradores")));

        // 4. RH - Folha
        intents.add(new IntentDef("rh_folha", "RH - Folha de Pagamento",
                List.of("/rh/folha-pagamento"),
                List.of("pagar salario","folha","holerite","gerar folha","descontos","proventos","demonstrativo")));

        // 4. RH - Beneficios
        intents.add(new IntentDef("rh_beneficios", "RH - Beneficios",
                List.of("/rh/beneficios/plano-saude","/rh/beneficios/vale-transporte/listar"),
                List.of("beneficios","vale transporte","vt","vale refeicao","vr","plano de saude","incluir beneficio")));

        // 4. RH - Workflow
        intents.add(new IntentDef("rh_workflow", "RH - Workflow",
                List.of("/rh/workflow/aprovacao"),
                List.of("aprovar algo","gestao de aprovacao","workflow","processos pendentes")));

        // 4. RH - Ponto e Escalas
        intents.add(new IntentDef("rh_ponto", "RH - Ponto e Escalas",
                List.of("/rh/ponto-escalas/registros","/rh/ponto-escalas/correcoes"),
                List.of("bater ponto","erro no ponto","corrigir ponto","escala","horas trabalhadas")));

        // 4. RH - Ferias
        intents.add(new IntentDef("rh_ferias", "RH - Ferias",
                List.of("/rh/ferias/solicitar","/rh/ferias/aprovar"),
                List.of("tirar ferias","solicitar ferias","aprovar ferias","calendario de ferias")));

        // 4. RH - Avaliacao
        intents.add(new IntentDef("rh_avaliacao", "RH - Avaliacao",
                List.of("/rh/avaliacao/periodicidade","/rh/avaliacao/feedbacks"),
                List.of("avaliacao","feedback","pesquisa interna","indicadores de desempenho")));

        // 4. RH - Treinamentos
        intents.add(new IntentDef("rh_treinamentos", "RH - Treinamentos",
                List.of("/rh/treinamentos/cadastro","/rh/treinamentos/inscricao"),
                List.of("treinamento","curso","certificado","inscricao em treinamento")));

        // 4. RH - Recrutamento
        intents.add(new IntentDef("rh_recrutamento", "RH - Recrutamento",
                List.of("/rh/recrutamento/candidatos","/rh/recrutamento/vagas"),
                List.of("vagas","recrutamento","candidato","triagem","entrevista")));

        // 5. Gestao - Financeiro
        intents.add(new IntentDef("financeiro", "Financeiro",
                List.of("/financeiro/contas-pagar","/financeiro/contas-receber","/financeiro/fluxo-caixa","/financeiro/transferencias"),
                List.of("gerar boleto","contas a pagar","contas a receber","adiantamento","transferir dinheiro","fluxo de caixa","financeiro travado")));

        // 5. Gestao - Marketing
        intents.add(new IntentDef("marketing", "Marketing",
                List.of("/marketing/campanhas","/marketing/leads"),
                List.of("campanha","leads","evento","material de marketing","dashboard marketing")));

        // 5. Gestao - TI
        intents.add(new IntentDef("ti", "Tecnologia",
                List.of("/ti/suporte","/ti/sistemas","/ti/backup","/ti/seguranca"),
                List.of("sistema travando","erro no sistema","nao abre","backup","restaurar","protegido","seguranca","suporte ti",
                        "impressora travando","nao imprime","erro impressao","impressora nao funciona","imprimir travou","falha de impressao")));

        // 5. Gestao - Juridico
        intents.add(new IntentDef("juridico", "Juridico",
                List.of("/juridico/contratos","/juridico/processos"),
                List.of("processos","contratos juridicos","advogado","compliance","documentos juridicos")));

        // 6. Administracao
        intents.add(new IntentDef("administracao", "Administracao",
                List.of("/usuarios/index","/perfis","/permissoes","/configuracoes","/metas"),
                List.of("usuario sem acesso","criar usuario","liberar acesso","trocar permissao","configuracoes","metas","relatorios gerenciais")));

        // 7. Servicos
        intents.add(new IntentDef("servicos", "Servicos",
                List.of("/solicitacoes/nova","/solicitacoes","/agenda","/servicos"),
                List.of("abrir solicitacao","minhas solicitacoes","formulario","agendamento","servico tecnico","suporte")));

        // 8. Suporte & Documentacao
        intents.add(new IntentDef("suporte_docs", "Suporte e Documentacao",
                List.of("/ajuda","/documentos","/suporte"),
                List.of("suporte","ajuda","manual","documentos","quero ajuda")));

        // 9. Pessoal
        intents.add(new IntentDef("pessoal", "Pessoal",
                List.of("/meus-pedidos","/favoritos"),
                List.of("favoritos","recomendacoes","meus pedidos","meus servicos")));
    }

    public Map<String,Object> route(String query) {
        String q = normalize(query);
        Map<String,Double> intentScore = new LinkedHashMap<>();
        Map<String,IntentDef> byId = intents.stream().collect(Collectors.toMap(i -> i.id, i -> i));
        Set<String> qTokens = new HashSet<>(tokenize(q));

        for (IntentDef intent : intents) {
            double score = 0.0;
            for (String syn : intent.synonyms) {
                String s = normalize(syn);
                if (q.contains(s)) score += 3.0; // phrase containment boost
                Set<String> sTokens = new HashSet<>(tokenize(s));
                int overlap = 0;
                for (String t : sTokens) if (qTokens.contains(t)) overlap++;
                if (overlap > 0) score += Math.sqrt(overlap); // token overlap
            }

            // special rules
            if (intent.id.equals("ti") && (q.contains("impressora") || q.contains("imprimir"))) score += 2.0;
            if (intent.id.equals("vendas") && (q.contains("cupom") || q.contains("nota"))) score += 1.0;
            if (intent.id.equals("financeiro") && q.contains("boleto")) score += 1.5;

            if (score > 0) intentScore.put(intent.id, score);
        }

        List<Map.Entry<String,Double>> ranked = intentScore.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        List<Map<String,Object>> suggestions = new ArrayList<>();
        for (Map.Entry<String,Double> e : ranked) {
            IntentDef def = byId.get(e.getKey());
            Map<String,Object> s = new LinkedHashMap<>();
            s.put("intentId", def.id);
            s.put("intent", def.label);
            s.put("score", e.getValue());
            s.put("routes", def.routes);
            suggestions.add(s);
        }

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("query", query);
        out.put("suggestions", suggestions);
        if (!suggestions.isEmpty()) {
            // choose first route from top intent as primary
            @SuppressWarnings("unchecked") List<String> r = (List<String>) suggestions.get(0).get("routes");
            out.put("primaryRoute", r.get(0));
        }
        return out;
    }

    private String normalize(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        n = n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        n = n.replaceAll("[^a-z0-9\u00a0-\u00ff ]", " ");
        return n.trim().replaceAll("\\s+", " ");
    }

    private List<String> tokenize(String s) {
        return Arrays.stream(s.split(" "))
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());
    }
}

