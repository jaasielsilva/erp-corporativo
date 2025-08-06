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
        
        relatorio.put("perfisComPermissao", perfisComPermissao);
        relatorio.put("categoriasPermissoes", categoriasPermissoes);
        relatorio.put("permissoesPorCategoria", listarPorCategoria());
        
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
        
        return permissoes;
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    /**
     * Normaliza o nome da permissão (adiciona ROLE_ se necessário)
     */
    private String normalizarNomePermissao(String nome) {
        String nomeNormalizado = nome.toUpperCase().trim();
        if (!nomeNormalizado.startsWith("ROLE_")) {
            nomeNormalizado = "ROLE_" + nomeNormalizado;
        }
        return nomeNormalizado;
    }

    /**
     * Extrai a categoria/módulo de uma permissão baseado no nome
     */
    private String extrairCategoria(String nomePermissao) {
        if (nomePermissao.startsWith("ROLE_USER")) return "Usuários";
        if (nomePermissao.startsWith("ROLE_RH")) return "Recursos Humanos";
        if (nomePermissao.startsWith("ROLE_FINANCEIRO")) return "Financeiro";
        if (nomePermissao.startsWith("ROLE_RELATORIO")) return "Relatórios";
        if (nomePermissao.startsWith("ROLE_CONFIG")) return "Configurações";
        if (nomePermissao.equals("ROLE_ADMIN") || nomePermissao.equals("ROLE_MASTER")) return "Sistema";
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
}