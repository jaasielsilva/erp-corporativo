package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.MapaPermissao;
import com.jaasielsilva.portalceo.model.MapaPermissao.TipoRecurso;
import com.jaasielsilva.portalceo.repository.MapaPermissaoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Permissao;
import java.util.stream.Collectors;

@Service
public class MapaPermissaoService {

    @Autowired
    private MapaPermissaoRepository mapaPermissaoRepository;

    @Autowired
    private PermissaoService permissaoService;

    /**
     * Inicializa o mapa de permissões se estiver vazio
     */
    @PostConstruct
    public void inicializarMapa() {
        // Sempre tenta sincronizar na inicialização para pegar novos perfis/permissões
        sincronizarPermissoes();
    }

    /**
     * Sincroniza as permissões do sistema com o mapa
     */
    @Transactional
    public void sincronizarPermissoes() {
        List<Permissao> todasPermissoes = permissaoService.listarTodas();
        
        for (Permissao permissao : todasPermissoes) {
            atualizarOuCriarMapeamento(permissao);
        }
        
        // Além disso, vamos cadastrar explicitamente algumas importantes para garantir a qualidade dos dados
        cadastrarMapeamentosExplicitos();
    }
    
    private void atualizarOuCriarMapeamento(Permissao permissao) {
        Optional<MapaPermissao> mapaOpt = mapaPermissaoRepository.findByPermissao(permissao.getNome());
        MapaPermissao mapa = mapaOpt.orElse(new MapaPermissao());
        
        if (mapa.getId() == null) {
            mapa.setPermissao(permissao.getNome());
            preencherMetadadosAutomaticos(mapa, permissao.getNome());
        }
        
        // Atualiza a lista de perfis
        List<Perfil> perfis = permissaoService.listarPerfisComPermissao(permissao.getId());
        String listaPerfis = perfis.stream()
                .map(Perfil::getNome)
                .sorted()
                .collect(Collectors.joining(", "));
        
        mapa.setPerfis(listaPerfis);
        
        mapaPermissaoRepository.save(mapa);
    }
    
    private void preencherMetadadosAutomaticos(MapaPermissao mapa, String permissaoNome) {
        // Inferir Módulo
        String p = permissaoNome.toUpperCase();
        if (p.contains("_RH_") || p.contains("RH")) mapa.setModulo("RH");
        else if (p.contains("_FINANCEIRO_") || p.contains("FINANCEIRO")) mapa.setModulo("Financeiro");
        else if (p.contains("_TI_") || p.contains("TI") || p.contains("SUPORTE")) mapa.setModulo("TI");
        else if (p.contains("_JURIDICO_") || p.contains("JURIDICO")) mapa.setModulo("Jurídico");
        else if (p.contains("_CLIENTES_") || p.contains("CLIENTES")) mapa.setModulo("Comercial");
        else if (p.contains("_VENDAS_") || p.contains("VENDAS")) mapa.setModulo("Vendas");
        else if (p.contains("_MARKETING_") || p.contains("MARKETING")) mapa.setModulo("Marketing");
        else if (p.contains("_ESTOQUE_") || p.contains("ESTOQUE")) mapa.setModulo("Estoque");
        else if (p.contains("_PROJETOS_") || p.contains("PROJETOS")) mapa.setModulo("Projetos");
        else if (p.contains("DASHBOARD")) mapa.setModulo("Dashboard");
        else if (p.contains("ADMIN")) mapa.setModulo("Administração");
        else mapa.setModulo("Geral");
        
        // Obter descrição padrão se existir
        Map<String, String> permissoesPadrao = permissaoService.getPermissoesPadrao();
        String descricaoPadrao = permissoesPadrao.get(permissaoNome);
        
        // Inferir Tipo e Recurso
        if (permissaoNome.startsWith("MENU_")) {
            mapa.setTipo(TipoRecurso.MENU);
            mapa.setRecurso(descricaoPadrao != null ? descricaoPadrao : "Menu " + capitalize(mapa.getModulo()));
        } else if (permissaoNome.contains("DASHBOARD") && permissaoNome.contains("VISUALIZAR")) {
            mapa.setTipo(TipoRecurso.GRAFICO);
            mapa.setRecurso(descricaoPadrao != null ? descricaoPadrao : "Gráfico/Dashboard");
        } else if (permissaoNome.contains("RELATORIO") || permissaoNome.contains("EXPORT")) {
            mapa.setTipo(TipoRecurso.RELATORIO);
            mapa.setRecurso(descricaoPadrao != null ? descricaoPadrao : "Relatórios");
        } else if (permissaoNome.contains("BOTAO")) {
            mapa.setTipo(TipoRecurso.BOTAO);
            mapa.setRecurso(descricaoPadrao != null ? descricaoPadrao : "Botão Ação");
        } else {
            mapa.setTipo(TipoRecurso.ACAO);
            mapa.setRecurso(descricaoPadrao != null ? descricaoPadrao : "Funcionalidade Geral");
        }
        
        // Descrição
        if (descricaoPadrao != null) {
            mapa.setDescricao(descricaoPadrao);
        } else {
            mapa.setDescricao("Permissão para " + permissaoNome.toLowerCase().replace("_", " "));
        }
    }

    private void cadastrarMapeamentosExplicitos() {
        // Mapeamento explícito para garantir qualidade nos itens principais solicitados pelo usuário
        
        // Suporte
        salvarMapeamento("TI", "Painel de Suporte", TipoRecurso.MENU, "MENU_TI_SUPORTE", "Acesso ao menu principal de suporte");
        salvarMapeamento("TI", "Novo Chamado", TipoRecurso.BOTAO, "CHAMADO_CRIAR", "Botão para criar novos chamados");
        salvarMapeamento("TI", "Atender Chamado", TipoRecurso.ACAO, "TECNICO_ATENDER_CHAMADOS", "Permite assumir e atender chamados");
        salvarMapeamento("TI", "Resolver Chamado", TipoRecurso.ACAO, "CHAMADO_RESOLVER", "Permite marcar chamado como resolvido");
        salvarMapeamento("TI", "Fechar Chamado", TipoRecurso.ACAO, "CHAMADO_FECHAR", "Permite finalizar o chamado");
        salvarMapeamento("TI", "Reabrir Chamado", TipoRecurso.ACAO, "CHAMADO_REABRIR", "Permite reabrir chamados");
        salvarMapeamento("TI", "Exportar Chamados", TipoRecurso.RELATORIO, "CHAMADO_VISUALIZAR", "Permite exportar lista de chamados");
        
        // Dashboard
        salvarMapeamento("Dashboard", "Gráfico de Adesão", TipoRecurso.GRAFICO, "ROLE_DASHBOARD_ADESAO_VISUALIZAR", "Visualizar gráfico de adesão");
        salvarMapeamento("Dashboard", "Dashboard Executivo", TipoRecurso.TELA, "DASHBOARD_EXECUTIVO_VISUALIZAR", "Acesso à dashboard executiva");
        
        // RH
        salvarMapeamento("RH", "Menu RH", TipoRecurso.MENU, "MENU_RH", "Acesso ao módulo de Recursos Humanos");
        salvarMapeamento("RH", "Visualizar Colaboradores", TipoRecurso.TELA, "ROLE_RH_READ", "Visualizar lista de colaboradores");
    }
    
    private void salvarMapeamento(String modulo, String recurso, TipoRecurso tipo, String permissao, String descricao) {
        Optional<MapaPermissao> existente = mapaPermissaoRepository.findByPermissao(permissao);
        MapaPermissao mapa;
        if (existente.isPresent()) {
            mapa = existente.get();
        } else {
            mapa = new MapaPermissao();
            mapa.setPermissao(permissao);
        }
        
        mapa.setModulo(modulo);
        mapa.setRecurso(recurso);
        mapa.setTipo(tipo);
        mapa.setDescricao(descricao);
        
        mapaPermissaoRepository.save(mapa);
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    public Optional<MapaPermissao> buscarPorPermissao(String permissao) {
        return mapaPermissaoRepository.findByPermissao(permissao);
    }
    
    public List<MapaPermissao> listarPorModulo(String modulo) {
        return mapaPermissaoRepository.findByModulo(modulo);
    }
    
    public List<MapaPermissao> listarTodos() {
        return mapaPermissaoRepository.findAll();
    }
}
