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

    public Fornecedor findById(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado com id: " + id));
    }

    //metodo pra excluir um fornecedor
    public void excluir(Long id) {
    fornecedorRepository.deleteById(id);
}

}
