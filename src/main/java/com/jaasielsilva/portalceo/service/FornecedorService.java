package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.repository.FornecedorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public List<Fornecedor> findAll() {
        return fornecedorRepository.findAll();
    }
    public Fornecedor salvar(Fornecedor fornecedor) {
        return fornecedorRepository.save(fornecedor);
    }

    public Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id).orElse(null);
    }
}
