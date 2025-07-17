package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    public List<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Fornecedor salvar(Fornecedor fornecedor) {
        return repository.save(fornecedor);
    }

    public Fornecedor buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}
