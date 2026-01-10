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
                            permissao.getNome(), perfisComPermissao));
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

    public List<Permissao> sugerirPermissoesPorModulosENivel(List<String> modulos, String nivel) {
        if (modulos == null || modulos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Permissao> todas = permissaoRepository.findAll();
        List<Permissao> sugeridas = new ArrayList<>();

        for (Permissao p : todas) {
            boolean moduloMatch = false;
            String nomeUpper = p.getNome().toUpperCase();
            
            for (String mod : modulos) {
                String modUpper = mod.toUpperCase();
                if (nomeUpper.contains(modUpper)) {
                    moduloMatch = true;
                    break;
                }
                // Heurísticas de mapeamento
                if (modUpper.equals("JURIDICO") && nomeUpper.contains("JURIDICO")) moduloMatch = true;
                if (modUpper.equals("RH") && nomeUpper.contains("_RH_")) moduloMatch = true;
                if (modUpper.equals("FINANCEIRO") && nomeUpper.contains("FINANCEIRO")) moduloMatch = true;
                if (modUpper.equals("TI") && nomeUpper.contains("_TI_")) moduloMatch = true;
                if (modUpper.equals("COMERCIAL") && (nomeUpper.contains("VENDAS") || nomeUpper.contains("CLIENTES"))) moduloMatch = true;
            }

            if (moduloMatch) {
                boolean nivelMatch = true;
                if ("BASICO".equalsIgnoreCase(nivel)) {
                    if (nomeUpper.contains("EXCLUIR") || nomeUpper.contains("CONFIG") || nomeUpper.contains("ADMIN") || nomeUpper.contains("GERENCIAR")) {
                        nivelMatch = false;
                    }
                } else if ("GERENTE".equalsIgnoreCase(nivel)) {
                    if (nomeUpper.contains("ADMIN")) {
                        nivelMatch = false;
                    }
                }
                
                if (nivelMatch) {
                    sugeridas.add(p);
                }
            }
        }
        return sugeridas;
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
    public Map<String, String> getPermissoesPadrao() {
        Map<String, String> permissoes = new HashMap<>();

        // --- Dashboards ---
        permissoes.put("DASHBOARD_EXECUTIVO_VISUALIZAR", "Visualizar Dashboard Executivo");
        permissoes.put("DASHBOARD_OPERACIONAL_VISUALIZAR", "Visualizar Dashboard Operacional");
        permissoes.put("DASHBOARD_FINANCEIRO_VISUALIZAR", "Visualizar Dashboard Financeiro");

        // --- Comercial ---
        permissoes.put("MENU_CLIENTES", "Menu Clientes");
        permissoes.put("MENU_CLIENTES_LISTAR", "Listar Clientes");
        permissoes.put("MENU_CLIENTES_NOVO", "Novo Cliente");
        permissoes.put("MENU_CLIENTES_CONTRATOS_LISTAR", "Listar Contratos");
        permissoes.put("MENU_CLIENTES_HISTORICO_INTERACOES", "Histórico de Interações");
        permissoes.put("MENU_CLIENTES_HISTORICO_PEDIDOS", "Histórico de Pedidos");
        permissoes.put("MENU_CLIENTES_AVANCADO_BUSCA", "Busca Avançada Clientes");
        permissoes.put("MENU_CLIENTES_AVANCADO_RELATORIOS", "Relatórios Clientes");

        permissoes.put("MENU_VENDAS", "Menu Vendas");
        permissoes.put("MENU_VENDAS_DASHBOARD", "Dashboard Vendas");
        permissoes.put("MENU_VENDAS_PDV", "PDV Vendas");
        permissoes.put("MENU_VENDAS_PEDIDOS", "Pedidos Vendas");
        permissoes.put("MENU_VENDAS_RELATORIOS", "Relatórios Vendas");

        // --- Compras / Estoque ---
        permissoes.put("MENU_COMPRAS", "Menu Compras");
        permissoes.put("MENU_ESTOQUE", "Menu Estoque");
        permissoes.put("MENU_ESTOQUE_PRODUTOS", "Produtos Estoque");
        permissoes.put("MENU_ESTOQUE_CATEGORIAS", "Categorias Estoque");
        permissoes.put("MENU_ESTOQUE_CATEGORIAS_LISTAR", "Listar Categorias");
        permissoes.put("MENU_ESTOQUE_CATEGORIAS_NOVO", "Nova Categoria");

        // --- Recursos Humanos (RH) ---
        permissoes.put("MENU_RH", "Menu RH");

        // Colaboradores
        permissoes.put("MENU_RH_COLABORADORES_LISTAR", "Listar Colaboradores");
        permissoes.put("MENU_RH_COLABORADORES_NOVO", "Novo Colaborador");
        permissoes.put("MENU_RH_COLABORADORES_ADESAO", "Adesão Colaboradores");

        // Folha
        permissoes.put("MENU_RH_FOLHA", "Menu Folha de Pagamento");
        permissoes.put("MENU_RH_FOLHA_INICIO", "Início Folha");
        permissoes.put("MENU_RH_FOLHA_LISTAR", "Listar Folha");
        permissoes.put("MENU_RH_FOLHA_GERAR", "Gerar Folha");
        permissoes.put("MENU_RH_FOLHA_HOLERITE", "Holerite");
        permissoes.put("MENU_RH_FOLHA_DESCONTOS", "Descontos Folha");
        permissoes.put("MENU_RH_FOLHA_RELATORIOS", "Relatórios Folha");

        // Benefícios
        permissoes.put("MENU_RH_BENEFICIOS", "Menu Benefícios");
        permissoes.put("MENU_RH_BENEFICIOS_PLANO_SAUDE", "Plano de Saúde");
        permissoes.put("MENU_RH_BENEFICIOS_VALE_TRANSPORTE", "Vale Transporte");
        permissoes.put("MENU_RH_BENEFICIOS_VALE_REFEICAO", "Vale Refeição");
        permissoes.put("MENU_RH_BENEFICIOS_ADESAO", "Adesão Benefícios");

        // Workflow
        permissoes.put("MENU_RH_WORKFLOW", "Menu Workflow");
        permissoes.put("MENU_RH_WORKFLOW_APROVACAO", "Aprovação Workflow");
        permissoes.put("MENU_RH_WORKFLOW_RELATORIOS", "Relatórios Workflow");

        // Ponto
        permissoes.put("MENU_RH_PONTO", "Menu Ponto");
        permissoes.put("MENU_RH_PONTO_REGISTROS", "Registros Ponto");
        permissoes.put("MENU_RH_PONTO_CORRECOES", "Correções Ponto");
        permissoes.put("MENU_RH_PONTO_ESCALAS", "Escalas Ponto");
        permissoes.put("MENU_RH_PONTO_RELATORIOS", "Relatórios Ponto");

        // Férias
        permissoes.put("MENU_RH_FERIAS", "Menu Férias");
        permissoes.put("MENU_RH_FERIAS_SOLICITAR", "Solicitar Férias");
        permissoes.put("MENU_RH_FERIAS_APROVAR", "Aprovar Férias");
        permissoes.put("MENU_RH_FERIAS_PLANEJAMENTO", "Planejamento Férias");
        permissoes.put("MENU_RH_FERIAS_CALENDARIO", "Calendário Férias");

        // Avaliação
        permissoes.put("MENU_RH_AVALIACAO", "Menu Avaliação");
        permissoes.put("MENU_RH_AVALIACAO_PERIODICIDADE", "Periodicidade Avaliação");
        permissoes.put("MENU_RH_AVALIACAO_FEEDBACKS", "Feedbacks Avaliação");
        permissoes.put("MENU_RH_AVALIACAO_RELATORIOS", "Relatórios Avaliação");

        // Treinamentos
        permissoes.put("MENU_RH_TREINAMENTOS", "Menu Treinamentos");
        permissoes.put("MENU_RH_TREINAMENTOS_CADASTRO", "Cadastro Treinamentos");
        permissoes.put("MENU_RH_TREINAMENTOS_INSCRICAO", "Inscrição Treinamentos");
        permissoes.put("MENU_RH_TREINAMENTOS_CERTIFICADO", "Certificado Treinamentos");

        // Recrutamento
        permissoes.put("MENU_RH_RECRUTAMENTO", "Menu Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_CANDIDATOS", "Candidatos Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_VAGAS", "Vagas Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_TRIAGEM", "Triagem Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_ENTREVISTAS", "Entrevistas Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_HISTORICO", "Histórico Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_PIPELINE", "Pipeline Recrutamento");
        permissoes.put("MENU_RH_RECRUTAMENTO_RELATORIOS", "Relatórios Recrutamento");

        // Relatórios RH
        permissoes.put("MENU_RH_RELATORIOS", "Menu Relatórios RH");
        permissoes.put("MENU_RH_RELATORIOS_TURNOVER", "Turnover RH");
        permissoes.put("MENU_RH_RELATORIOS_ABSENTEISMO", "Absenteísmo RH");
        permissoes.put("MENU_RH_RELATORIOS_HEADCOUNT", "Headcount RH");
        permissoes.put("MENU_RH_RELATORIOS_INDICADORES", "Indicadores RH");
        permissoes.put("MENU_RH_RELATORIOS_ADMISSOES_DEMISSOES", "Admissões/Demissões RH");
        permissoes.put("MENU_RH_RELATORIOS_FERIAS_BENEFICIOS", "Relatórios Férias/Benefícios");

        // Configurações RH
        permissoes.put("MENU_RH_CONFIGURACOES", "Menu Configurações RH");
        permissoes.put("MENU_RH_CONFIGURACOES_INICIO", "Início Configurações RH");
        permissoes.put("MENU_RH_CONFIGURACOES_POLITICAS_FERIAS", "Políticas Férias");
        permissoes.put("MENU_RH_CONFIGURACOES_PONTO", "Parâmetros Ponto");
        permissoes.put("MENU_RH_CONFIGURACOES_INTEGRACOES", "Integrações RH");

        // Auditoria RH
        permissoes.put("MENU_RH_AUDITORIA", "Menu Auditoria RH");
        permissoes.put("MENU_RH_AUDITORIA_INICIO", "Início Auditoria RH");
        permissoes.put("MENU_RH_AUDITORIA_ACESSOS", "Log Acessos RH");
        permissoes.put("MENU_RH_AUDITORIA_ALTERACOES", "Alterações Dados RH");
        permissoes.put("MENU_RH_AUDITORIA_EXPORTACOES", "Exportações RH");
        permissoes.put("MENU_RH_AUDITORIA_REVISOES", "Revisões Periódicas RH");

        // --- Financeiro ---
        permissoes.put("MENU_FINANCEIRO", "Menu Financeiro");
        permissoes.put("MENU_FINANCEIRO_DASHBOARD", "Dashboard Financeiro");
        permissoes.put("MENU_FINANCEIRO_CONTAS_BANCARIAS", "Contas Bancárias");
        permissoes.put("MENU_FINANCEIRO_CONTAS_PAGAR", "Contas a Pagar");
        permissoes.put("MENU_FINANCEIRO_CONTAS_RECEBER", "Contas a Receber");
        permissoes.put("MENU_FINANCEIRO_FLUXO_CAIXA", "Fluxo de Caixa");
        permissoes.put("MENU_FINANCEIRO_TRANSFERENCIAS", "Transferências Financeiras");
        permissoes.put("MENU_FINANCEIRO_RELATORIOS", "Relatórios Financeiros");

        // --- Marketing ---
        permissoes.put("MENU_MARKETING", "Menu Marketing");
        permissoes.put("MENU_MARKETING_DASHBOARD", "Dashboard Marketing");
        permissoes.put("MENU_MARKETING_CAMPANHAS", "Campanhas Marketing");
        permissoes.put("MENU_MARKETING_LEADS", "Leads Marketing");
        permissoes.put("MENU_MARKETING_EVENTOS", "Eventos Marketing");
        permissoes.put("MENU_MARKETING_MATERIAIS", "Materiais Marketing");

        // --- Tecnologia (TI) ---
        permissoes.put("MENU_TI", "Menu TI");
        permissoes.put("MENU_TI_DASHBOARD", "Dashboard TI");
        permissoes.put("MENU_TI_SISTEMAS", "Sistemas TI");
        permissoes.put("MENU_TI_SUPORTE", "Suporte TI");
        permissoes.put("MENU_TI_BACKUP", "Backup TI");
        permissoes.put("MENU_TI_SEGURANCA", "Segurança TI");
        permissoes.put("MENU_TI_AUDITORIA", "Auditoria TI");

        // --- Jurídico ---
        permissoes.put("MENU_JURIDICO", "Menu Jurídico");
        permissoes.put("MENU_JURIDICO_DASHBOARD", "Dashboard Jurídico");
        permissoes.put("MENU_JURIDICO_CLIENTES", "Clientes Jurídico");

        // Previdenciário
        permissoes.put("MENU_JURIDICO_PREVIDENCIARIO", "Menu Previdenciário");
        permissoes.put("MENU_JURIDICO_PREVIDENCIARIO_LISTAR", "Listar Previdenciário");
        permissoes.put("MENU_JURIDICO_PREVIDENCIARIO_NOVO", "Novo Previdenciário");

        permissoes.put("MENU_JURIDICO_WHATCHAT", "WhaTchat Jurídico");
        permissoes.put("MENU_JURIDICO_CONTRATOS", "Contratos Jurídico");

        // Processos
        permissoes.put("MENU_JURIDICO_PROCESSOS", "Menu Processos");
        permissoes.put("MENU_JURIDICO_PROCESSOS_LISTAR", "Listar Processos");
        permissoes.put("MENU_JURIDICO_PROCESSOS_NOVO", "Novo Processo");

        // Exportação
        permissoes.put("JURIDICO_CONTRATOS_EXPORTAR", "Exportar Contratos Jurídicos");
        permissoes.put("JURIDICO_PROCESSOS_EXPORTAR", "Exportar Processos Jurídicos");
        permissoes.put("JURIDICO_PREVIDENCIARIO_EXPORTAR", "Exportar Processos Previdenciários");

        permissoes.put("MENU_JURIDICO_COMPLIANCE", "Compliance Jurídico");
        permissoes.put("MENU_JURIDICO_DOCUMENTOS", "Documentos Jurídico");

        // Auditoria Jurídico
        permissoes.put("MENU_JURIDICO_AUDITORIA", "Menu Auditoria Jurídico");
        permissoes.put("MENU_JURIDICO_AUDITORIA_INICIO", "Início Auditoria Jurídico");
        permissoes.put("MENU_JURIDICO_AUDITORIA_ACESSOS", "Log Acessos Jurídico");
        permissoes.put("MENU_JURIDICO_AUDITORIA_ALTERACOES", "Alterações Dados Jurídico");
        permissoes.put("MENU_JURIDICO_AUDITORIA_EXPORTACOES", "Exportações Jurídico");
        permissoes.put("MENU_JURIDICO_AUDITORIA_REVISOES", "Revisões Periódicas Jurídico");

        // --- Cadastros / Utilidades ---
        permissoes.put("MENU_CADASTROS", "Menu Cadastros");
        permissoes.put("MENU_UTILIDADES", "Menu Utilidades");

        // --- Projetos ---
        permissoes.put("MENU_PROJETOS", "Menu Projetos");
        permissoes.put("MENU_PROJETOS_GERAL", "Geral Projetos");
        permissoes.put("MENU_PROJETOS_GERAL_LISTAR", "Listar Projetos");
        permissoes.put("MENU_PROJETOS_GERAL_NOVO", "Novo Projeto");

        permissoes.put("MENU_PROJETOS_TAREFAS", "Tarefas Projetos");
        permissoes.put("MENU_PROJETOS_TAREFAS_LISTAR", "Listar Tarefas");
        permissoes.put("MENU_PROJETOS_TAREFAS_ATRIBUICOES", "Atribuições Tarefas");

        permissoes.put("MENU_PROJETOS_EQUIPES_CADASTRAR", "Cadastrar Equipes");
        permissoes.put("MENU_PROJETOS_EQUIPES_MEMBROS", "Membros Equipe");
        permissoes.put("MENU_PROJETOS_CRONOGRAMA", "Cronograma Projetos");
        permissoes.put("MENU_PROJETOS_RELATORIOS", "Relatórios Projetos");

        // --- Serviços ---
        permissoes.put("MENU_SERVICOS_SOLICITACOES_GERENCIAR", "Gerenciar Solicitações");
        permissoes.put("MENU_SERVICOS_SOLICITACOES_DASHBOARD", "Dashboard Solicitações");

        // --- Administração ---
        permissoes.put("MENU_ADMIN", "Menu Admin");
        permissoes.put("MENU_ADMIN_USUARIOS", "Menu Admin Usuários");
        permissoes.put("MENU_ADMIN_GESTAO_ACESSO", "Menu Admin Gestão de Acesso");
        permissoes.put("MENU_ADMIN_GESTAO_ACESSO_PERFIS", "Menu Admin Perfis");
        permissoes.put("MENU_ADMIN_GESTAO_ACESSO_PERMISSOES", "Menu Admin Permissões");
        permissoes.put("MENU_ADMIN_RELATORIOS", "Menu Admin Relatórios");
        permissoes.put("MENU_ADMIN_CONFIGURACOES", "Menu Admin Configurações");
        permissoes.put("MENU_ADMIN_METAS", "Menu Admin Metas");

        // --- Pessoal ---
        permissoes.put("MENU_PESSOAL", "Menu Pessoal");
        permissoes.put("MENU_PESSOAL_MEUS_PEDIDOS", "Meus Pedidos");
        permissoes.put("MENU_PESSOAL_MEUS_SERVICOS", "Meus Serviços");
        permissoes.put("MENU_PESSOAL_FAVORITOS", "Favoritos");
        permissoes.put("MENU_PESSOAL_RECOMENDACOES", "Recomendações");

        // --- Suporte & Documentos ---
        permissoes.put("MENU_AJUDA", "Central de Ajuda");
        permissoes.put("MENU_DOCS_GERENCIAL", "Documentos Gerenciais");
        permissoes.put("MENU_DOCS_PESSOAL", "Documentos Pessoais");

        // --- Permissões Gerais (Legado) ---
        permissoes.put("ROLE_USER_READ", "Visualizar usuários");
        permissoes.put("ROLE_USER_WRITE", "Criar/editar usuários");
        permissoes.put("ROLE_USER_DELETE", "Excluir usuários");
        permissoes.put("ROLE_USER_ADMIN", "Administrar usuários");

        permissoes.put("ROLE_RH_READ", "Visualizar RH");
        permissoes.put("ROLE_RH_WRITE", "Criar/editar RH");
        permissoes.put("ROLE_RH_DELETE", "Excluir RH");
        permissoes.put("ROLE_RH_ADMIN", "Administrar RH");

        permissoes.put("ROLE_FINANCEIRO_READ", "Visualizar financeiro");
        permissoes.put("ROLE_FINANCEIRO_WRITE", "Criar/editar financeiro");
        permissoes.put("ROLE_FINANCEIRO_DELETE", "Excluir financeiro");
        permissoes.put("ROLE_FINANCEIRO_ADMIN", "Administrar financeiro");

        permissoes.put("ROLE_RELATORIO_READ", "Visualizar relatórios");
        permissoes.put("ROLE_RELATORIO_EXPORT", "Exportar relatórios");
        permissoes.put("ROLE_RELATORIO_ADMIN", "Administrar relatórios");

        permissoes.put("ROLE_ADMIN", "Administrador do sistema");
        permissoes.put("ROLE_USER", "Usuário básico");
        permissoes.put("ROLE_MASTER", "Usuário master");

        // Permissões de Configuração
        permissoes.put("ROLE_CONFIG_READ", "Visualizar configurações");
        permissoes.put("ROLE_CONFIG_WRITE", "Alterar configurações");

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
        if (n.isEmpty())
            return n;
        if (n.startsWith("ROLE_"))
            return n;
        if (n.startsWith("MENU_"))
            return n;
        if (n.matches(
                "^(CHAMADO|TECNICO|ADMIN|USUARIO|FINANCEIRO|JURIDICO|MARKETING|ESTOQUE|CLIENTES|VENDAS|COMPRAS|PROJETOS|PESSOAL|DOCS)_.*$")) {
            return n;
        }
        return "ROLE_" + n;
    }

    /**
     * Extrai a categoria/módulo de uma permissão baseado no nome
     */
    private String extrairCategoria(String nomePermissao) {
        String n = nomePermissao == null ? "" : nomePermissao.toUpperCase();
        if (n.startsWith("ROLE_USER"))
            return "Usuários";
        if (n.startsWith("ROLE_RH"))
            return "Recursos Humanos";
        if (n.startsWith("ROLE_FINANCEIRO"))
            return "Financeiro";
        if (n.startsWith("ROLE_RELATORIO"))
            return "Relatórios";
        if (n.startsWith("ROLE_CONFIG"))
            return "Configurações";
        if (n.equals("ROLE_ADMIN") || n.equals("ROLE_MASTER"))
            return "Sistema";
        if (n.startsWith("MENU_FINANCEIRO"))
            return "Financeiro";
        if (n.startsWith("MENU_TI"))
            return "TI";
        if (n.startsWith("MENU_RH"))
            return "Recursos Humanos";
        if (n.startsWith("MENU_MARKETING"))
            return "Marketing";
        if (n.startsWith("MENU_JURIDICO"))
            return "Jurídico";
        if (n.startsWith("MENU_PROJETOS"))
            return "Projetos";
        if (n.startsWith("MENU_ADMIN"))
            return "Administração";
        if (n.startsWith("MENU_PESSOAL"))
            return "Pessoal";
        if (n.startsWith("MENU_CADASTROS"))
            return "Cadastros";
        if (n.startsWith("MENU_UTILIDADES"))
            return "Utilidades";
        if (n.startsWith("MENU_AJUDA"))
            return "Ajuda";
        if (n.startsWith("MENU_DOCS"))
            return "Documentos";
        if (n.startsWith("MENU_CLIENTES"))
            return "Clientes";
        if (n.startsWith("MENU_VENDAS"))
            return "Vendas";
        if (n.startsWith("MENU_COMPRAS"))
            return "Compras";
        if (n.startsWith("MENU_ESTOQUE"))
            return "Estoque";
        if (n.startsWith("MENU_SERVICOS"))
            return "Serviços";
        return "Geral";
    }

    /**
     * Verifica se é uma permissão do sistema que não pode ser modificada
     */
    private boolean isPermissaoSistema(String nome) {
        Set<String> permissoesSistema = Set.of(
                "ROLE_ADMIN", "ROLE_USER", "ROLE_MASTER");
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
        boolean gerencial = nivel != null
                && Set.of("MASTER", "ADMIN", "GERENTE", "COORDENADOR", "SUPERVISOR").contains(nivel.toUpperCase());
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
            if (n.equals("MASTER"))
                sugeridas.add("ROLE_MASTER");
            if (n.equals("ADMIN"))
                sugeridas.add("ROLE_ADMIN");
        }
        List<Permissao> resultado = new ArrayList<>();
        for (String nome : sugeridas) {
            permissaoRepository.findByNome(nome).ifPresent(resultado::add);
        }
        return resultado;
    }
}
