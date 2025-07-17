package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public Produto buscarPorEan(String ean) {
        return repository.findByEan(ean);
    }
}
