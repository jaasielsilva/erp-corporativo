package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    /**
     * Lista fornecedores ativos (ativo = true).
     */
    public List<Fornecedor> listarAtivos() {
        return fornecedorRepository.findByAtivoTrue();
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
    Pageable pageable = PageRequest.of(pagina, tamanho);
    return fornecedorRepository.findAll(pageable); // traz TODOS (ativos e inativos)
}
    
}
