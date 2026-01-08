package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    /**
     * Lista fornecedores ativos (ativo = true).
     */
    @Cacheable(value = "fornecedoresAtivos", unless = "#result == null || #result.isEmpty()")
    public List<Fornecedor> listarAtivos() {
        return fornecedorRepository.findByAtivoTrue();
    }

    /**
     * Lista fornecedores ativos apenas com dados básicos (ID, Razão Social) para seleção.
     */
    @Cacheable(value = "fornecedoresSelecao", unless = "#result == null || #result.isEmpty()")
    public List<Fornecedor> listarAtivosParaSelecao() {
        return fornecedorRepository.findBasicInfoForSelection();
    }

    /**
     * Lista todos os fornecedores, ativos ou não.
     */
    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    /**
     * Salva ou atualiza um fornecedor.
     */
    public Fornecedor salvar(Fornecedor fornecedor) {
        if (fornecedor.getCnpj() != null && !fornecedor.getCnpj().isBlank()) {
            Optional<Fornecedor> existente = fornecedorRepository.findByCnpj(fornecedor.getCnpj());
            if (existente.isPresent()) {
                if (fornecedor.getId() == null || !existente.get().getId().equals(fornecedor.getId())) {
                    throw new IllegalArgumentException("Já existe um fornecedor cadastrado com o CNPJ " + fornecedor.getCnpj());
                }
            }
        }
        return fornecedorRepository.save(fornecedor);
    }

    /**
     * Busca fornecedor por ID, ou lança exceção se não encontrado.
     */
    public Fornecedor findById(Long id) {
        return fornecedorRepository.findById(id).orElse(null);
    }

    /**
     * Marca o fornecedor como inativo (exclusão lógica).
     */
    public void inativar(Long id) {
        Fornecedor fornecedor = findById(id);
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    /**
     * Marca o fornecedor como ativo (reativação).
     */
    public void reativar(Long id) {
        Fornecedor fornecedor = findById(id);
        fornecedor.setAtivo(true);
        fornecedorRepository.save(fornecedor);
    }

    /**
     * Exclusão lógica: inativa o fornecedor ao invés de excluir do banco.
     */
    public void excluir(Long id) {
        inativar(id);
    }

    public Page<Fornecedor> listarTodosPaginado(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("razaoSocial").ascending());
        return fornecedorRepository.findAll(pageable);
    }

    public Page<Fornecedor> listarPaginado(String busca, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("razaoSocial").ascending());
        String termo = (busca != null && !busca.isBlank()) ? busca.trim() : null;
        String st = (status != null && !status.isBlank()) ? status.trim() : null;
        return fornecedorRepository.buscarComFiltros(termo, st, pageable);
    }
    
}
