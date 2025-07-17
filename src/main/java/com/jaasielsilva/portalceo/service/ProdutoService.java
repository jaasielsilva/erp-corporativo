package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;
    

    // metodo que retorna lista de todos os produtos
    public List<Produto> listarTodosProdutos() {
        return produtoRepository.findAll();
    }

    public long somarQuantidadeEstoque() {
        return produtoRepository.somarQuantidadeEstoque();
    }

}
