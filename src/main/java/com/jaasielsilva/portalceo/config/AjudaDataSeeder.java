package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.ajuda.AjudaCategoria;
import com.jaasielsilva.portalceo.model.ajuda.AjudaConteudo;
import com.jaasielsilva.portalceo.repository.ajuda.AjudaCategoriaRepository;
import com.jaasielsilva.portalceo.repository.ajuda.AjudaConteudoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AjudaDataSeeder implements ApplicationRunner {
    private final AjudaCategoriaRepository catRepo;
    private final AjudaConteudoRepository conteudoRepo;

    public AjudaDataSeeder(AjudaCategoriaRepository catRepo, AjudaConteudoRepository conteudoRepo) {
        this.catRepo = catRepo;
        this.conteudoRepo = conteudoRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AjudaCategoria suporte = ensureCat("suporte", "Suporte", null);
        AjudaCategoria impressoras = ensureCat("impressoras", "Impressoras", suporte);
        AjudaCategoria chamados = ensureCat("chamados", "Chamados", suporte);

        ensureCat("ti", "TI", null);
        ensureCat("sistemas", "Sistemas", null);
        ensureCat("financeiro", "Financeiro", null);
        ensureCat("rh", "Recursos Humanos", null);
        ensureCat("comercial", "Comercial", null);
        ensureCat("estoque", "Estoque", null);
        ensureCat("seguranca", "Segurança", null);
        ensureCat("relatorios", "Relatórios", null);
        ensureCat("integracoes", "Integrações", null);
        ensureCat("usuarios-acesso", "Usuários & Acesso", null);
        ensureCat("documentacao", "Documentação", null);

        // Seed initial content if empty (legacy check)
        if (conteudoRepo.count() == 0) {
            seedInitialContent(impressoras, chamados);
        }

        // Ensure documentation content exists (new check)
        AjudaCategoria documentacao = catRepo.findBySlug("documentacao").orElse(null);
        if (documentacao != null) {
            ensureConteudo(documentacao, "Hierarquia de Menus e Permissões (Authorities)", 
                "Lista completa de módulos e subníveis com seus respectivos códigos de permissão (Authorities):\n\n" +
                "**Painel Administrativo** (`MENU_ADMIN`)\n" +
                "**Dashboard** (`MENU_DASHBOARD`)\n" +
                "**Clientes** (`MENU_CLIENTES`)\n" +
                "- Geral: `MENU_CLIENTES_LISTAR`\n" +
                "- Novo Cliente: `MENU_CLIENTES_CRIAR`\n" +
                "- Histórico: `MENU_CLIENTES_HISTORICO`\n" +
                "- Relatórios: `MENU_CLIENTES_RELATORIOS`\n\n" +
                "**Fornecedores** (`MENU_FORNECEDORES`)\n" +
                "- Geral: `MENU_FORNECEDORES_LISTAR`\n" +
                "- Novo Fornecedor: `MENU_FORNECEDORES_CRIAR`\n" +
                "- Contratos: `MENU_FORNECEDORES_CONTRATOS`\n\n" +
                "**Produtos** (`MENU_PRODUTOS`)\n" +
                "- Geral: `MENU_PRODUTOS_LISTAR`\n" +
                "- Novo Produto: `MENU_PRODUTOS_CRIAR`\n" +
                "- Categorias: `MENU_PRODUTOS_CATEGORIAS`\n" +
                "- Estoque: `MENU_PRODUTOS_ESTOQUE`\n\n" +
                "**Vendas** (`MENU_VENDAS`)\n" +
                "- Nova Venda: `MENU_VENDAS_CRIAR`\n" +
                "- Histórico: `MENU_VENDAS_HISTORICO`\n" +
                "- Relatórios: `MENU_VENDAS_RELATORIOS`\n\n" +
                "**Financeiro** (`MENU_FINANCEIRO`)\n" +
                "- Contas a Pagar: `MENU_FINANCEIRO_PAGAR`\n" +
                "- Contas a Receber: `MENU_FINANCEIRO_RECEBER`\n" +
                "- Fluxo de Caixa: `MENU_FINANCEIRO_CAIXA`\n" +
                "- Relatórios: `MENU_FINANCEIRO_RELATORIOS`\n\n" +
                "**RH** (`MENU_RH`)\n" +
                "- Funcionários: `MENU_RH_FUNCIONARIOS`\n" +
                "- Folha de Pagamento: `MENU_RH_FOLHA`\n" +
                "- Ponto: `MENU_RH_PONTO`\n" +
                "- Benefícios: `MENU_RH_BENEFICIOS`\n\n" +
                "**Configurações** (`MENU_CONFIGURACOES`)\n" +
                "- Usuários: `MENU_CONFIGURACOES_USUARIOS`\n" +
                "- Perfis: `MENU_CONFIGURACOES_PERFIS`\n" +
                "- Sistema: `MENU_CONFIGURACOES_SISTEMA`\n" +
                "- Logs: `MENU_CONFIGURACOES_LOGS`",
                "menus, permissoes, authorities, hierarquia, acesso");

            ensureConteudo(documentacao, "Como Solicitar Acessos e Permissões",
                "Guia para solicitação de acessos no sistema:\n\n" +
                "1. Acesse: **Serviços** -> **Solicitações** -> **Nova Solicitação**.\n" +
                "2. No campo **Módulos**, selecione os módulos macro necessários (ex: Clientes, Financeiro).\n" +
                "3. No campo **Subníveis**, informe os códigos das permissões específicas desejadas (Authorities).\n" +
                "   - Exemplo: Para apenas visualizar clientes sem poder criar, solicite `MENU_CLIENTES_LISTAR`.\n" +
                "   - Exemplo: Para acesso completo a vendas, solicite `MENU_VENDAS` e todos os subníveis (`_CRIAR`, `_HISTORICO`, etc.).\n" +
                "4. **Importante**: Utilize o princípio do menor privilégio. Solicite apenas o necessário para sua função.\n" +
                "5. Após a aprovação pelo gestor, um perfil personalizado será criado automaticamente com as permissões exatas solicitadas.",
                "solicitacao, acesso, permissoes, guia, como solicitar");
        }
    }

    private void seedInitialContent(AjudaCategoria impressoras, AjudaCategoria chamados) {
            AjudaConteudo c1 = new AjudaConteudo();
            c1.setCategoria(impressoras);
            c1.setTitulo("Como abrir um chamado para erro de impressora");
            c1.setTipo(AjudaConteudo.Tipo.FAQ);
            c1.setCorpo("1. Acesse Suporte > Chamados.\n2. Clique em 'Novo Chamado'.\n3. Selecione categoria 'Impressoras'.\n4. Descreva o erro e anexe foto se possível.\n5. Envie e acompanhe pelo painel.\nDica: informe modelo e número de patrimônio.");
            c1.setTags("impressora, chamado, erro");
            c1.setPublicado(true);
            c1.setCriadoAt(LocalDateTime.now());
            conteudoRepo.save(c1);

            AjudaConteudo c2 = new AjudaConteudo();
            c2.setCategoria(chamados);
            c2.setTitulo("Como acompanhar o status do chamado");
            c2.setTipo(AjudaConteudo.Tipo.ARTIGO);
            c2.setCorpo("Abra Suporte > Chamados e use os filtros por status. Clique no número do chamado para ver detalhes, comentários e SLA.");
            c2.setTags("chamados, status, sla");
            c2.setPublicado(true);
            c2.setCriadoAt(LocalDateTime.now());
            conteudoRepo.save(c2);
    }

    private void ensureConteudo(AjudaCategoria cat, String titulo, String corpo, String tags) {
        if (conteudoRepo.findByTitulo(titulo).isPresent()) return;
        AjudaConteudo c = new AjudaConteudo();
        c.setCategoria(cat);
        c.setTitulo(titulo);
        c.setTipo(AjudaConteudo.Tipo.ARTIGO);
        c.setCorpo(corpo);
        c.setTags(tags);
        c.setPublicado(true);
        c.setCriadoAt(LocalDateTime.now());
        conteudoRepo.save(c);
    }

    private AjudaCategoria ensureCat(String slug, String nome, AjudaCategoria parent) {
        return catRepo.findBySlug(slug).map(c -> {
            boolean updateNeeded = false;
            if (c.getNome() == null || !c.getNome().equals(nome)) { c.setNome(nome); updateNeeded = true; }
            if ((parent != null && c.getParent() == null) || (parent != null && c.getParent() != null && !c.getParent().getSlug().equals(parent.getSlug()))) { c.setParent(parent); updateNeeded = true; }
            return updateNeeded ? catRepo.save(c) : c;
        }).orElseGet(() -> {
            AjudaCategoria novo = new AjudaCategoria();
            novo.setSlug(slug);
            novo.setNome(nome);
            novo.setParent(parent);
            return catRepo.save(novo);
        });
    }
}
