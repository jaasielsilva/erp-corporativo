package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.AvaliacaoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.repository.AvaliacaoFornecedorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoFornecedorService {

    private final AvaliacaoFornecedorRepository repository;

    public AvaliacaoFornecedorService(AvaliacaoFornecedorRepository repository) {
        this.repository = repository;
    }

    public List<AvaliacaoFornecedor> findByFornecedor(Fornecedor fornecedor) {
        return repository.findByFornecedor(fornecedor);
    }

    public AvaliacaoFornecedor save(AvaliacaoFornecedor avaliacao) {
        return repository.save(avaliacao);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public AvaliacaoFornecedor findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Page<AvaliacaoFornecedor> findByFornecedorPaged(Fornecedor fornecedor, Pageable pageable) {
    return repository.findByFornecedor(fornecedor, pageable);
}

}
