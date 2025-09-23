package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Permissao;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.repository.PermissaoRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ===============================
    // MÉTODOS CRUD BÁSICOS
    // ===============================

    /**
     * Lista todos os perfis disponíveis
     */
    public List<Perfil> listarTodos() {
        return perfilRepository.findAll();
    }

    /**
     * Busca perfil por ID
     */
    public Optional<Perfil> buscarPorId(Long id) {
        return perfilRepository.findById(id);
    }

    /**
     * Busca perfil por nome
     */
    public Optional<Perfil> buscarPorNome(String nome) {
        return perfilRepository.findByNome(nome);
    }

    /**
     * Salva um novo perfil
     */
    public Perfil salvar(Perfil perfil) {
        // Validações básicas
        if (perfil.getNome() == null || perfil.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do perfil é obrigatório");
        }

        // Verifica se já existe um perfil com o mesmo nome
        if (perfil.getId() == null && perfilRepository.findByNome(perfil.getNome()).isPresent()) {
            throw new IllegalArgumentException("Já existe um perfil com este nome");
        }

        // Normaliza o nome (maiúsculo)
        perfil.setNome(perfil.getNome().toUpperCase().trim());

        return perfilRepository.save(perfil);
    }

    /**
     * Atualiza um perfil existente
     */
    public Perfil atualizar(Long id, Perfil perfilAtualizado) {
        Perfil perfilExistente = perfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        // Verifica se o novo nome já existe em outro perfil
        Optional<Perfil> perfilComMesmoNome = perfilRepository.findByNome(perfilAtualizado.getNome());
        if (perfilComMesmoNome.isPresent() && !perfilComMesmoNome.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe um perfil com este nome");
        }

        // Atualiza os dados
        perfilExistente.setNome(perfilAtualizado.getNome().toUpperCase().trim());
        
        return perfilRepository.save(perfilExistente);
    }

    /**
     * Exclui um perfil
     */
    public void excluir(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        // Verifica se o perfil está sendo usado por algum usuário
        long usuariosComPerfil = usuarioRepository.countByPerfisContaining(perfil);
        if (usuariosComPerfil > 0) {
            throw new IllegalStateException(
                String.format("Não é possível excluir o perfil '%s' pois está sendo usado por %d usuário(s)", 
                    perfil.getNome(), usuariosComPerfil)
            );
        }

        // Verifica se é um perfil do sistema que não pode ser excluído
        if (isPerfilSistema(perfil.getNome())) {
            throw new IllegalStateException("Perfis do sistema não podem ser excluídos");
        }

        perfilRepository.delete(perfil);
    }

    // ===============================
    // MÉTODOS DE PERMISSÕES
    // ===============================

    /**
     * Adiciona permissões a um perfil
     */
    public Perfil adicionarPermissoes(Long perfilId, Set<Long> permissaoIds) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        Set<Permissao> novasPermissoes = permissaoIds.stream()
                .map(id -> permissaoRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Permissão não encontrada: " + id)))
                .collect(Collectors.toSet());

        if (perfil.getPermissoes() == null) {
            perfil.setPermissoes(new HashSet<>());
        }

        perfil.getPermissoes().addAll(novasPermissoes);
        return perfilRepository.save(perfil);
    }

    /**
     * Remove permissões de um perfil
     */
    public Perfil removerPermissoes(Long perfilId, Set<Long> permissaoIds) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        if (perfil.getPermissoes() != null) {
            perfil.getPermissoes().removeIf(permissao -> permissaoIds.contains(permissao.getId()));
        }

        return perfilRepository.save(perfil);
    }

    /**
     * Define todas as permissões de um perfil (substitui as existentes)
     */
    public Perfil definirPermissoes(Long perfilId, Set<Long> permissaoIds) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        Set<Permissao> permissoes = permissaoIds.stream()
                .map(id -> permissaoRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Permissão não encontrada: " + id)))
                .collect(Collectors.toSet());

        perfil.setPermissoes(permissoes);
        return perfilRepository.save(perfil);
    }

    /**
     * Lista todas as permissões disponíveis
     */
    public List<Permissao> listarTodasPermissoes() {
        return permissaoRepository.findAll();
    }

    /**
     * Lista permissões de um perfil específico
     */
    public Set<Permissao> listarPermissoesDoPerfil(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        
        return perfil.getPermissoes() != null ? perfil.getPermissoes() : new HashSet<>();
    }

    // ===============================
    // MÉTODOS DE CONSULTA E RELATÓRIOS
    // ===============================

    /**
     * Lista usuários que possuem um perfil específico
     */
    public List<Usuario> listarUsuariosDoPerfil(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        
        return usuarioRepository.findByPerfisContaining(perfil);
    }

    /**
     * Conta quantos usuários possuem um perfil específico
     */
    public long contarUsuariosDoPerfil(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        
        return usuarioRepository.countByPerfisContaining(perfil);
    }

    /**
     * Busca perfis por nome (busca parcial)
     */
    public List<Perfil> buscarPorNomeParcial(String nome) {
        return perfilRepository.findAll().stream()
                .filter(perfil -> perfil.getNome().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Lista perfis ordenados por nome
     */
    public List<Perfil> listarOrdenadosPorNome() {
        return perfilRepository.findAll().stream()
                .sorted(Comparator.comparing(Perfil::getNome))
                .collect(Collectors.toList());
    }

    /**
     * Gera relatório de perfis com estatísticas
     */
    public Map<String, Object> gerarRelatorioEstatisticas() {
        List<Perfil> perfis = perfilRepository.findAll();
        Map<String, Object> relatorio = new HashMap<>();
        
        relatorio.put("totalPerfis", perfis.size());
        relatorio.put("totalUsuarios", usuarioRepository.count());
        
        Map<String, Long> usuariosPorPerfil = new HashMap<>();
        Map<String, Integer> permissoesPorPerfil = new HashMap<>();
        
        for (Perfil perfil : perfis) {
            long usuarios = usuarioRepository.countByPerfisContaining(perfil);
            int permissoes = perfil.getPermissoes() != null ? perfil.getPermissoes().size() : 0;
            
            usuariosPorPerfil.put(perfil.getNome(), usuarios);
            permissoesPorPerfil.put(perfil.getNome(), permissoes);
        }
        
        relatorio.put("usuariosPorPerfil", usuariosPorPerfil);
        relatorio.put("permissoesPorPerfil", permissoesPorPerfil);
        
        return relatorio;
    }

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    /**
     * Verifica se é um perfil do sistema que não pode ser modificado
     */
    private boolean isPerfilSistema(String nome) {
        Set<String> perfisSistema = Set.of("ADMIN", "USER", "MASTER", "GERENTE");
        return perfisSistema.contains(nome.toUpperCase());
    }

    /**
     * Valida se um perfil pode ser modificado
     */
    public boolean podeSerModificado(Long perfilId) {
        Optional<Perfil> perfil = perfilRepository.findById(perfilId);
        return perfil.isPresent() && !isPerfilSistema(perfil.get().getNome());
    }

    /**
     * Cria um perfil com permissões padrão
     */
    public Perfil criarPerfilComPermissoesPadrao(String nome, Set<String> nomesPermissoes) {
        Perfil perfil = new Perfil();
        perfil.setNome(nome.toUpperCase().trim());
        
        Set<Permissao> permissoes = nomesPermissoes.stream()
                .map(nomePermissao -> permissaoRepository.findByNome(nomePermissao)
                        .orElseThrow(() -> new RuntimeException("Permissão não encontrada: " + nomePermissao)))
                .collect(Collectors.toSet());
        
        perfil.setPermissoes(permissoes);
        return perfilRepository.save(perfil);
    }
}