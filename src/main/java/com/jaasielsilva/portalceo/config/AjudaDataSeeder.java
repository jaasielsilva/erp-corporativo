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

        if (conteudoRepo.count() == 0) {
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
