package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Permissao;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.repository.PermissaoRepository;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissaoService {

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    // ===============================
    // MÉTODOS CRUD BÁSICOS
    // ===============================

    /**
     * Lista todas as permissões disponíveis
     */
    public List<Permissao> listarTodas() {
        return permissaoRepository.findAll();
    }

    /**
     * Busca permissão por ID
     */
    public Optional<Permissao> buscarPorId(Long id) {
        return permissaoRepository.findById(id);
    }

    /**
     * Busca permissão por nome
     */
    public Optional<Permissao> buscarPorNome(String nome) {
        return permissaoRepository.findByNome(nome);
    }

    /**
     * Salva uma nova permissão
     */
    public Permissao salvar(Permissao permissao) {
        // Validações básicas
        if (permissao.getNome() == null || permissao.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da permissão é obrigatório");
        }

        // Verifica se já existe uma permissão com o mesmo nome
        if (permissao.getId() == null && permissaoRepository.findByNome(permissao.getNome()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma permissão com este nome");
        }

        // Normaliza o nome (maiúsculo e com prefixo ROLE_ se necessário)
        String nomeNormalizado = normalizarNomePermissao(permissao.getNome());
        permissao.setNome(nomeNormalizado);

        return permissaoRepository.save(permissao);
    }

    /**
     * Atualiza uma permissão existente
     */
    public Permissao atualizar(Long id, Permissao permissaoAtualizada) {
        Permissao permissaoExistente = permissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        // Verifica se o novo nome já existe em outra permissão
        String nomeNormalizado = normalizarNomePermissao(permissaoAtualizada.getNome());
        Optional<Permissao> permissaoComMesmoNome = permissaoRepository.findByNome(nomeNormalizado);
        if (permissaoComMesmoNome.isPresent() && !permissaoComMesmoNome.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe uma permissão com este nome");
        }

        // Atualiza os dados
        permissaoExistente.setNome(nomeNormalizado);
        
        return permissaoRepository.save(permissaoExistente);
    }

    /**
     * Exclui uma permissão
     */
    public void excluir(Long id) {
        Permissao permissao = permissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        // Verifica se a permissão está sendo usada por algum perfil
        long perfisComPermissao = perfilRepository.findAll().stream()
                .mapToLong(perfil -> perfil.getPermissoes() != null && 
                    perfil.getPermissoes().contains(permissao) ? 1 : 0)
                .sum();

        if (perfisComPermissao > 0) {
            throw new IllegalStateException(
                String.format("Não é possível excluir a permissão '%s' pois está sendo usada por %d perfil(s)", 
                    permissao.getNome(), perfisComPermissao)
            );
        }

        // Verifica se é uma permissão do sistema que não pode ser excluída
        if (isPermissaoSistema(permissao.getNome())) {
            throw new IllegalStateException("Permissões do sistema não podem ser excluídas");
        }

        permissaoRepository.delete(permissao);
    }

    // ===============================
    // MÉTODOS DE CONSULTA E RELATÓRIOS
    // ===============================

    /**
     * Lista perfis que possuem uma permissão específica
     */
    public List<Perfil> listarPerfisComPermissao(Long permissaoId) {
        Permissao permissao = permissaoRepository.findById(permissaoId)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));
        
        return perfilRepository.findAll().stream()
                .filter(perfil -> perfil.getPermissoes() != null && 
                    perfil.getPermissoes().contains(permissao))
                .collect(Collectors.toList());
    }

    /**
     * Conta quantos perfis possuem uma permissão específica
     */
    public long contarPerfisComPermissao(Long permissaoId) {
        return listarPerfisComPermissao(permissaoId).size();
    }

    /**
     * Busca permissões por nome (busca parcial)
     */
    public List<Permissao> buscarPorNomeParcial(String nome) {
        return permissaoRepository.findAll().stream()
                .filter(permissao -> permissao.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lista permissões ordenadas por nome
     */
    public List<Permissao> listarOrdenadasPorNome() {
        return permissaoRepository.findAll().stream()
                .sorted(Comparator.comparing(Permissao::getNome))
                .collect(Collectors.toList());
    }

    /**
     * Lista permissões por categoria/módulo
     */
    public Map<String, List<Permissao>> listarPorCategoria() {
        List<Permissao> permissoes = permissaoRepository.findAll();
        Map<String, List<Permissao>> categorias = new HashMap<>();
        
        for (Permissao permissao : permissoes) {
            String categoria = extrairCategoria(permissao.getNome());
            categorias.computeIfAbsent(categoria, k -> new ArrayList<>()).add(permissao);
        }
        
        return categorias;
    }

    /**
     * Gera relatório de permissões com estatísticas
     */
    public Map<String, Object> gerarRelatorioEstatisticas() {
        List<Permissao> permissoes = permissaoRepository.findAll();
        Map<String, Object> relatorio = new HashMap<>();
        
        relatorio.put("totalPermissoes", permissoes.size());
        relatorio.put("totalPerfis", perfilRepository.count());
        
        Map<String, Long> perfisComPermissao = new HashMap<>();
        Map<String, String> categoriasPermissoes = new HashMap<>();
        
        for (Permissao permissao : permissoes) {
            long perfis = contarPerfisComPermissao(permissao.getId());
            String categoria = extrairCategoria(permissao.getNome());
            
            perfisComPermissao.put(permissao.getNome(), perfis);
            categoriasPermissoes.put(permissao.getNome(), categoria);
        }
        
        String permissaoMaisUsada = permissoes.stream()
                .max(Comparator.comparingLong(p -> perfisComPermissao.getOrDefault(p.getNome(), 0L)))
                .map(Permissao::getNome)
                .orElse(null);
        
        relatorio.put("perfisComPermissao", perfisComPermissao);
        relatorio.put("categoriasPermissoes", categoriasPermissoes);
        relatorio.put("permissoesPorCategoria", listarPorCategoria());
        relatorio.put("permissaoMaisUsada", permissaoMaisUsada);
        
        return relatorio;
    }

    // ===============================
    // MÉTODOS DE INICIALIZAÇÃO
    // ===============================

    /**
     * Cria permissões padrão do sistema se não existirem
     */
    public void criarPermissoesPadrao() {
        Map<String, String> permissoesPadrao = getPermissoesPadrao();
        
        for (Map.Entry<String, String> entry : permissoesPadrao.entrySet()) {
            String nome = entry.getKey();
            if (permissaoRepository.findByNome(nome).isEmpty()) {
                Permissao permissao = new Permissao();
                permissao.setNome(nome);
                permissaoRepository.save(permissao);
            }
        }
    }

    /**
     * Define as permissões padrão do sistema
     */
    private Map<String, String> getPermissoesPadrao() {
        Map<String, String> permissoes = new HashMap<>();
        
        // Permissões de Usuários
        permissoes.put("ROLE_USER_READ", "Visualizar usuários");
        permissoes.put("ROLE_USER_WRITE", "Criar/editar usuários");
        permissoes.put("ROLE_USER_DELETE", "Excluir usuários");
        permissoes.put("ROLE_USER_ADMIN", "Administrar usuários");
        
        // Permissões de RH
        permissoes.put("ROLE_RH_READ", "Visualizar RH");
        permissoes.put("ROLE_RH_WRITE", "Criar/editar RH");
        permissoes.put("ROLE_RH_DELETE", "Excluir RH");
        permissoes.put("ROLE_RH_ADMIN", "Administrar RH");
        
        // Permissões Financeiras
        permissoes.put("ROLE_FINANCEIRO_READ", "Visualizar financeiro");
        permissoes.put("ROLE_FINANCEIRO_WRITE", "Criar/editar financeiro");
        permissoes.put("ROLE_FINANCEIRO_DELETE", "Excluir financeiro");
        permissoes.put("ROLE_FINANCEIRO_ADMIN", "Administrar financeiro");
        
        // Permissões de Relatórios
        permissoes.put("ROLE_RELATORIO_READ", "Visualizar relatórios");
        permissoes.put("ROLE_RELATORIO_EXPORT", "Exportar relatórios");
        permissoes.put("ROLE_RELATORIO_ADMIN", "Administrar relatórios");
        
        // Permissões de Sistema
        permissoes.put("ROLE_ADMIN", "Administrador do sistema");
        permissoes.put("ROLE_USER", "Usuário básico");
        permissoes.put("ROLE_MASTER", "Usuário master");
        
        // Permissões de Configuração
        permissoes.put("ROLE_CONFIG_READ", "Visualizar configurações");
        permissoes.put("ROLE_CONFIG_WRITE", "Alterar configurações");

        // Menus gerais
        permissoes.put("MENU_DASHBOARD", "Menu Dashboard");
        permissoes.put("MENU_CLIENTES", "Menu Clientes");
        permissoes.put("MENU_CLIENTES_LISTAR", "Menu Clientes Listar");
        permissoes.put("MENU_CLIENTES_NOVO", "Menu Clientes Novo");
        permissoes.put("MENU_CLIENTES_CONTRATOS_LISTAR", "Menu Clientes Contratos");
        permissoes.put("MENU_CLIENTES_HISTORICO_INTERACOES", "Menu Clientes Interações");
        permissoes.put("MENU_CLIENTES_HISTORICO_PEDIDOS", "Menu Clientes Pedidos");
        permissoes.put("MENU_CLIENTES_AVANCADO_BUSCA", "Menu Clientes Busca Avançada");
        permissoes.put("MENU_CLIENTES_AVANCADO_RELATORIOS", "Menu Clientes Relatórios");
        permissoes.put("MENU_VENDAS", "Menu Vendas");
        permissoes.put("MENU_VENDAS_DASHBOARD", "Menu Vendas Dashboard");
        permissoes.put("MENU_VENDAS_PDV", "Menu Vendas PDV");
        permissoes.put("MENU_VENDAS_PEDIDOS", "Menu Vendas Pedidos");
        permissoes.put("MENU_VENDAS_RELATORIOS", "Menu Vendas Relatórios");
        permissoes.put("MENU_COMPRAS", "Menu Compras");
        permissoes.put("MENU_ESTOQUE", "Menu Estoque");
        permissoes.put("MENU_ESTOQUE_PRODUTOS", "Menu Estoque Produtos");
        permissoes.put("MENU_ESTOQUE_CATEGORIAS", "Menu Estoque Categorias");
        permissoes.put("MENU_ESTOQUE_CATEGORIAS_LISTAR", "Menu Estoque Categorias Listar");
        permissoes.put("MENU_ESTOQUE_CATEGORIAS_NOVO", "Menu Estoque Categorias Novo");

        // Menus RH
        permissoes.put("MENU_RH", "Menu RH");
        permissoes.put("MENU_RH_COLABORADORES_LISTAR", "Menu RH Colaboradores Listar");
        permissoes.put("MENU_RH_COLABORADORES_NOVO", "Menu RH Colaboradores Novo");
        permissoes.put("MENU_RH_COLABORADORES_ADESAO", "Menu RH Adesão");
        permissoes.put("MENU_RH_FOLHA", "Menu RH Folha");
        permissoes.put("MENU_RH_FOLHA_INICIO", "Menu RH Folha Início");
        permissoes.put("MENU_RH_FOLHA_LISTAR", "Menu RH Folha Listar");
        permissoes.put("MENU_RH_FOLHA_GERAR", "Menu RH Folha Gerar");
        permissoes.put("MENU_RH_FOLHA_HOLERITE", "Menu RH Folha Holerite");
        permissoes.put("MENU_RH_FOLHA_DESCONTOS", "Menu RH Folha Descontos");
        permissoes.put("MENU_RH_FOLHA_RELATORIOS", "Menu RH Folha Relatórios");
        permissoes.put("MENU_RH_BENEFICIOS", "Menu RH Benefícios");
        permissoes.put("MENU_RH_BENEFICIOS_PLANO_SAUDE", "Menu RH Plano de Saúde");
        permissoes.put("MENU_RH_BENEFICIOS_VALE_TRANSPORTE", "Menu RH VT");
        permissoes.put("MENU_RH_BENEFICIOS_VALE_REFEICAO", "Menu RH VR");
        permissoes.put("MENU_RH_BENEFICIOS_ADESAO", "Menu RH Benefícios Adesão");
        permissoes.put("MENU_RH_WORKFLOW", "Menu RH Workflow");
        permissoes.put("MENU_RH_WORKFLOW_APROVACAO", "Menu RH Workflow Aprovação");
        permissoes.put("MENU_RH_WORKFLOW_RELATORIOS", "Menu RH Workflow Relatórios");
        permissoes.put("MENU_RH_PONTO", "Menu RH Ponto");
        permissoes.put("MENU_RH_PONTO_REGISTROS", "Menu RH Ponto Registros");
        permissoes.put("MENU_RH_PONTO_CORRECOES", "Menu RH Ponto Correções");
        permissoes.put("MENU_RH_PONTO_ESCALAS", "Menu RH Ponto Escalas");
        permissoes.put("MENU_RH_PONTO_RELATORIOS", "Menu RH Ponto Relatórios");
        permissoes.put("MENU_RH_FERIAS", "Menu RH Férias");
        permissoes.put("MENU_RH_FERIAS_SOLICITAR", "Menu RH Férias Solicitar");
        permissoes.put("MENU_RH_FERIAS_APROVAR", "Menu RH Férias Aprovar");
        permissoes.put("MENU_RH_FERIAS_PLANEJAMENTO", "Menu RH Férias Planejamento");
        permissoes.put("MENU_RH_FERIAS_CALENDARIO", "Menu RH Férias Calendário");
        permissoes.put("MENU_RH_AVALIACAO", "Menu RH Avaliação");
        permissoes.put("MENU_RH_AVALIACAO_PERIODICIDADE", "Menu RH Avaliação Periodicidade");
        permissoes.put("MENU_RH_AVALIACAO_FEEDBACKS", "Menu RH Avaliação Feedbacks");
        permissoes.put("MENU_RH_AVALIACAO_RELATORIOS", "Menu RH Avaliação Relatórios");
        permissoes.put("MENU_RH_TREINAMENTOS", "Menu RH Treinamentos");
        permissoes.put("MENU_RH_TREINAMENTOS_CADASTRO", "Menu RH Treinamentos Cadastro");
        permissoes.put("MENU_RH_TREINAMENTOS_INSCRICAO", "Menu RH Treinamentos Inscrição");
        permissoes.put("MENU_RH_TREINAMENTOS_CERTIFICADO", "Menu RH Treinamentos Certificado");
        permissoes.put("MENU_RH_RECRUTAMENTO", "Menu RH Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_CANDIDATOS", "Menu RH Recrutamento Candidatos");
        permissoes.put("MENU_RH_RECRUTAMENTO_VAGAS", "Menu RH Recrutamento Vagas");
        permissoes.put("MENU_RH_RECRUTAMENTO_TRIAGEM", "Menu RH Recrutamento Triagem");
        permissoes.put("MENU_RH_RECRUTAMENTO_ENTREVISTAS", "Menu RH Recrutamento Entrevistas");
        permissoes.put("MENU_RH_RECRUTAMENTO_HISTORICO", "Menu RH Recrutamento Histórico");
        permissoes.put("MENU_RH_RECRUTAMENTO_PIPELINE", "Menu RH Recrutamento Pipeline");
        permissoes.put("MENU_RH_RECRUTAMENTO_RELATORIOS", "Menu RH Recrutamento Relatórios");
        permissoes.put("MENU_RH_RELATORIOS", "Menu RH Relatórios");
        permissoes.put("MENU_RH_RELATORIOS_TURNOVER", "Menu RH Relatórios Turnover");
        permissoes.put("MENU_RH_RELATORIOS_ABSENTEISMO", "Menu RH Relatórios Absenteísmo");
        permissoes.put("MENU_RH_RELATORIOS_HEADCOUNT", "Menu RH Relatórios Headcount");
        permissoes.put("MENU_RH_RELATORIOS_INDICADORES", "Menu RH Relatórios Indicadores");
        permissoes.put("MENU_RH_RELATORIOS_ADMISSOES_DEMISSOES", "Menu RH Relatórios Admissões/Demissões");
        permissoes.put("MENU_RH_RELATORIOS_FERIAS_BENEFICIOS", "Menu RH Relatórios Férias/Benefícios");
        permissoes.put("MENU_RH_CONFIGURACOES", "Menu RH Configurações");
        permissoes.put("MENU_RH_CONFIGURACOES_INICIO", "Menu RH Configurações Início");
        permissoes.put("MENU_RH_CONFIGURACOES_POLITICAS_FERIAS", "Menu RH Configurações Políticas de Férias");
        permissoes.put("MENU_RH_CONFIGURACOES_PONTO", "Menu RH Configurações Ponto");
        permissoes.put("MENU_RH_CONFIGURACOES_INTEGRACOES", "Menu RH Configurações Integrações");
        permissoes.put("MENU_RH_AUDITORIA", "Menu RH Auditoria");
        permissoes.put("MENU_RH_AUDITORIA_INICIO", "Menu RH Auditoria Início");
        permissoes.put("MENU_RH_AUDITORIA_ACESSOS", "Menu RH Auditoria Acessos");
        permissoes.put("MENU_RH_AUDITORIA_ALTERACOES", "Menu RH Auditoria Alterações");
        permissoes.put("MENU_RH_AUDITORIA_EXPORTACOES", "Menu RH Auditoria Exportações");
        permissoes.put("MENU_RH_AUDITORIA_REVISOES", "Menu RH Auditoria Revisões");

        // Menus Financeiro
        permissoes.put("MENU_FINANCEIRO", "Menu Financeiro");
        permissoes.put("MENU_FINANCEIRO_DASHBOARD", "Menu Financeiro Dashboard");
        permissoes.put("MENU_FINANCEIRO_CONTAS_BANCARIAS", "Menu Financeiro Contas Bancárias");
        permissoes.put("MENU_FINANCEIRO_CONTAS_PAGAR", "Menu Financeiro Contas a Pagar");
        permissoes.put("MENU_FINANCEIRO_CONTAS_RECEBER", "Menu Financeiro Contas a Receber");
        permissoes.put("MENU_FINANCEIRO_FLUXO_CAIXA", "Menu Financeiro Fluxo de Caixa");
        permissoes.put("MENU_FINANCEIRO_TRANSFERENCIAS", "Menu Financeiro Transferências");
        permissoes.put("MENU_FINANCEIRO_RELATORIOS", "Menu Financeiro Relatórios");

        // Menus Marketing
        permissoes.put("MENU_MARKETING", "Menu Marketing");
        permissoes.put("MENU_MARKETING_DASHBOARD", "Menu Marketing Dashboard");
        permissoes.put("MENU_MARKETING_CAMPANHAS", "Menu Marketing Campanhas");
        permissoes.put("MENU_MARKETING_LEADS", "Menu Marketing Leads");
        permissoes.put("MENU_MARKETING_EVENTOS", "Menu Marketing Eventos");
        permissoes.put("MENU_MARKETING_MATERIAIS", "Menu Marketing Materiais");

        // Menus TI
        permissoes.put("MENU_TI", "Menu TI");
        permissoes.put("MENU_TI_DASHBOARD", "Menu TI Dashboard");
        permissoes.put("MENU_TI_SISTEMAS", "Menu TI Sistemas");
        permissoes.put("MENU_TI_SUPORTE", "Menu TI Suporte");
        permissoes.put("MENU_TI_BACKUP", "Menu TI Backup");
        permissoes.put("MENU_TI_SEGURANCA", "Menu TI Segurança");
        permissoes.put("MENU_TI_AUDITORIA", "Menu TI Auditoria");

        // Menus Jurídico
        permissoes.put("MENU_JURIDICO", "Menu Jurídico");
        permissoes.put("MENU_JURIDICO_DASHBOARD", "Menu Jurídico Dashboard");
        permissoes.put("MENU_JURIDICO_CLIENTES", "Menu Jurídico Clientes");
        permissoes.put("MENU_JURIDICO_PREVIDENCIARIO", "Menu Jurídico Previdenciário");
        permissoes.put("MENU_JURIDICO_PREVIDENCIARIO_LISTAR", "Menu Jurídico Previdenciário Listar");
        permissoes.put("MENU_JURIDICO_PREVIDENCIARIO_NOVO", "Menu Jurídico Previdenciário Novo");
        permissoes.put("MENU_JURIDICO_WHATCHAT", "Menu Jurídico Chat");
        permissoes.put("MENU_JURIDICO_CONTRATOS", "Menu Jurídico Contratos");
        permissoes.put("MENU_JURIDICO_PROCESSOS", "Menu Jurídico Processos");
        permissoes.put("MENU_JURIDICO_PROCESSOS_LISTAR", "Menu Jurídico Processos Listar");
        permissoes.put("MENU_JURIDICO_PROCESSOS_NOVO", "Menu Jurídico Processos Novo");
        permissoes.put("MENU_JURIDICO_COMPLIANCE", "Menu Jurídico Compliance");
        permissoes.put("MENU_JURIDICO_DOCUMENTOS", "Menu Jurídico Documentos");
        permissoes.put("MENU_JURIDICO_AUDITORIA", "Menu Jurídico Auditoria");
        permissoes.put("MENU_JURIDICO_AUDITORIA_INICIO", "Menu Jurídico Auditoria Início");
        permissoes.put("MENU_JURIDICO_AUDITORIA_ACESSOS", "Menu Jurídico Auditoria Acessos");
        permissoes.put("MENU_JURIDICO_AUDITORIA_ALTERACOES", "Menu Jurídico Auditoria Alterações");
        permissoes.put("MENU_JURIDICO_AUDITORIA_EXPORTACOES", "Menu Jurídico Auditoria Exportações");
        permissoes.put("MENU_JURIDICO_AUDITORIA_REVISOES", "Menu Jurídico Auditoria Revisões");

        // Menus Cadastros/Utilidades/Projetos/Serviços/Admin/Pessoal/Ajuda/Documentos
        permissoes.put("MENU_CADASTROS", "Menu Cadastros");
        permissoes.put("MENU_UTILIDADES", "Menu Utilidades");
        permissoes.put("MENU_PROJETOS", "Menu Projetos");
        permissoes.put("MENU_PROJETOS_GERAL", "Menu Projetos Geral");
        permissoes.put("MENU_PROJETOS_GERAL_LISTAR", "Menu Projetos Geral Listar");
        permissoes.put("MENU_PROJETOS_GERAL_NOVO", "Menu Projetos Geral Novo");
        permissoes.put("MENU_PROJETOS_TAREFAS", "Menu Projetos Tarefas");
        permissoes.put("MENU_PROJETOS_TAREFAS_LISTAR", "Menu Projetos Tarefas Listar");
        permissoes.put("MENU_PROJETOS_TAREFAS_ATRIBUICOES", "Menu Projetos Tarefas Atribuições");
        permissoes.put("MENU_PROJETOS_EQUIPES_CADASTRAR", "Menu Projetos Equipes Cadastrar");
        permissoes.put("MENU_PROJETOS_EQUIPES_MEMBROS", "Menu Projetos Equipes Membros");
        permissoes.put("MENU_PROJETOS_CRONOGRAMA", "Menu Projetos Cronograma");
        permissoes.put("MENU_PROJETOS_RELATORIOS", "Menu Projetos Relatórios");
        permissoes.put("MENU_SERVICOS_SOLICITACOES_GERENCIAR", "Menu Solicitações de Serviços");
        permissoes.put("MENU_SERVICOS_SOLICITACOES_DASHBOARD", "Menu Solicitações Dashboard");
        permissoes.put("MENU_ADMIN", "Menu Administração");
        permissoes.put("MENU_ADMIN_USUARIOS", "Menu Admin Usuários");
        permissoes.put("MENU_ADMIN_GESTAO_ACESSO", "Menu Admin Gestão de Acesso");
        permissoes.put("MENU_ADMIN_GESTAO_ACESSO_PERFIS", "Menu Admin Perfis");
        permissoes.put("MENU_ADMIN_GESTAO_ACESSO_PERMISSOES", "Menu Admin Permissões");
        permissoes.put("MENU_ADMIN_RELATORIOS", "Menu Admin Relatórios");
        permissoes.put("MENU_ADMIN_CONFIGURACOES", "Menu Admin Configurações");
        permissoes.put("MENU_ADMIN_METAS", "Menu Admin Metas");
        permissoes.put("MENU_PESSOAL", "Menu Pessoal");
        permissoes.put("MENU_PESSOAL_MEUS_PEDIDOS", "Menu Pessoal Meus Pedidos");
        permissoes.put("MENU_PESSOAL_MEUS_SERVICOS", "Menu Pessoal Meus Serviços");
        permissoes.put("MENU_PESSOAL_FAVORITOS", "Menu Pessoal Favoritos");
        permissoes.put("MENU_PESSOAL_RECOMENDACOES", "Menu Pessoal Recomendações");
        permissoes.put("MENU_AJUDA", "Menu Ajuda");
        permissoes.put("MENU_DOCS_GERENCIAL", "Menu Documentos Gerenciais");
        permissoes.put("MENU_DOCS_PESSOAL", "Menu Documentos Pessoais");
        
        return permissoes;
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    /**
     * Normaliza o nome da permissão (adiciona ROLE_ se necessário)
     */
    private String normalizarNomePermissao(String nome) {
        String n = nome == null ? "" : nome.toUpperCase().trim();
        if (n.isEmpty()) return n;
        if (n.startsWith("ROLE_")) return n;
        if (n.startsWith("MENU_")) return n;
        if (n.matches("^(CHAMADO|TECNICO|ADMIN|USUARIO|FINANCEIRO|JURIDICO|MARKETING|ESTOQUE|CLIENTES|VENDAS|COMPRAS|PROJETOS|PESSOAL|DOCS)_.*$")) {
            return n;
        }
        return "ROLE_" + n;
    }

    /**
     * Extrai a categoria/módulo de uma permissão baseado no nome
     */
    private String extrairCategoria(String nomePermissao) {
        String n = nomePermissao == null ? "" : nomePermissao.toUpperCase();
        if (n.startsWith("ROLE_USER")) return "Usuários";
        if (n.startsWith("ROLE_RH")) return "Recursos Humanos";
        if (n.startsWith("ROLE_FINANCEIRO")) return "Financeiro";
        if (n.startsWith("ROLE_RELATORIO")) return "Relatórios";
        if (n.startsWith("ROLE_CONFIG")) return "Configurações";
        if (n.equals("ROLE_ADMIN") || n.equals("ROLE_MASTER")) return "Sistema";
        if (n.startsWith("MENU_FINANCEIRO")) return "Financeiro";
        if (n.startsWith("MENU_TI")) return "TI";
        if (n.startsWith("MENU_RH")) return "Recursos Humanos";
        if (n.startsWith("MENU_MARKETING")) return "Marketing";
        if (n.startsWith("MENU_JURIDICO")) return "Jurídico";
        if (n.startsWith("MENU_PROJETOS")) return "Projetos";
        if (n.startsWith("MENU_ADMIN")) return "Administração";
        if (n.startsWith("MENU_PESSOAL")) return "Pessoal";
        if (n.startsWith("MENU_CADASTROS")) return "Cadastros";
        if (n.startsWith("MENU_UTILIDADES")) return "Utilidades";
        if (n.startsWith("MENU_AJUDA")) return "Ajuda";
        if (n.startsWith("MENU_DOCS")) return "Documentos";
        if (n.startsWith("MENU_CLIENTES")) return "Clientes";
        if (n.startsWith("MENU_VENDAS")) return "Vendas";
        if (n.startsWith("MENU_COMPRAS")) return "Compras";
        if (n.startsWith("MENU_ESTOQUE")) return "Estoque";
        if (n.startsWith("MENU_SERVICOS")) return "Serviços";
        return "Geral";
    }

    /**
     * Verifica se é uma permissão do sistema que não pode ser modificada
     */
    private boolean isPermissaoSistema(String nome) {
        Set<String> permissoesSistema = Set.of(
            "ROLE_ADMIN", "ROLE_USER", "ROLE_MASTER"
        );
        return permissoesSistema.contains(nome.toUpperCase());
    }

    /**
     * Valida se uma permissão pode ser modificada
     */
    public boolean podeSerModificada(Long permissaoId) {
        Optional<Permissao> permissao = permissaoRepository.findById(permissaoId);
        return permissao.isPresent() && !isPermissaoSistema(permissao.get().getNome());
    }

    /**
     * Cria múltiplas permissões de uma vez
     */
    public List<Permissao> criarPermissoes(List<String> nomes) {
        List<Permissao> permissoesCriadas = new ArrayList<>();
        
        for (String nome : nomes) {
            String nomeNormalizado = normalizarNomePermissao(nome);
            if (permissaoRepository.findByNome(nomeNormalizado).isEmpty()) {
                Permissao permissao = new Permissao();
                permissao.setNome(nomeNormalizado);
                permissoesCriadas.add(permissaoRepository.save(permissao));
            }
        }
        
        return permissoesCriadas;
    }

    public List<Permissao> sugerirPermissoesPorModulosENivel(List<String> modulos, String nivel) {
        Set<String> sugeridas = new HashSet<>();
        boolean gerencial = nivel != null && Set.of("MASTER","ADMIN","GERENTE","COORDENADOR","SUPERVISOR").contains(nivel.toUpperCase());
        sugeridas.add("ROLE_USER");
        if (modulos != null) {
            for (String m : modulos) {
                String mm = m.toUpperCase();
                if (mm.equals("FINANCEIRO")) {
                    if (gerencial) {
                        sugeridas.add("ROLE_FINANCEIRO_READ");
                        sugeridas.add("ROLE_FINANCEIRO_WRITE");
                        sugeridas.add("ROLE_FINANCEIRO_DELETE");
                        sugeridas.add("ROLE_FINANCEIRO_ADMIN");
                    } else {
                        sugeridas.add("ROLE_FINANCEIRO_READ");
                    }
                } else if (mm.equals("RH")) {
                    if (gerencial) {
                        sugeridas.add("ROLE_RH_READ");
                        sugeridas.add("ROLE_RH_WRITE");
                        sugeridas.add("ROLE_RH_DELETE");
                        sugeridas.add("ROLE_RH_ADMIN");
                    } else {
                        sugeridas.add("ROLE_RH_READ");
                    }
                } else if (mm.equals("RELATORIOS") || mm.equals("RELATÓRIOS")) {
                    sugeridas.add("ROLE_RELATORIO_READ");
                    if (gerencial) {
                        sugeridas.add("ROLE_RELATORIO_EXPORT");
                    }
                } else if (mm.equals("CONFIG") || mm.equals("CONFIGURACOES") || mm.equals("CONFIGURAÇÕES")) {
                    sugeridas.add("ROLE_CONFIG_READ");
                    if (gerencial) {
                        sugeridas.add("ROLE_CONFIG_WRITE");
                    }
                } else if (mm.equals("JURIDICO") || mm.equals("JURÍDICO")) {
                    sugeridas.add("ROLE_JURIDICO");
                    if (gerencial) {
                        sugeridas.add("ROLE_JURIDICO_GERENTE");
                    }
                } else {
                    sugeridas.add("ROLE_" + mm + "_READ");
                }
            }
        }
        if (nivel != null) {
            String n = nivel.toUpperCase();
            if (n.equals("MASTER")) sugeridas.add("ROLE_MASTER");
            if (n.equals("ADMIN")) sugeridas.add("ROLE_ADMIN");
        }
        List<Permissao> resultado = new ArrayList<>();
        for (String nome : sugeridas) {
            permissaoRepository.findByNome(nome).ifPresent(resultado::add);
        }
        return resultado;
    }
}
