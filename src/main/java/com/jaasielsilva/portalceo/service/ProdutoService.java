package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // Conta produtos com estoque crítico (estoque <= minimoEstoque)
    public long contarProdutosCriticos() {
        return produtoRepository.countEstoqueCritico();
    }

    // Busca produto pelo ID
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    // Busca produto pelo EAN
    public Optional<Produto> buscarPorEan(String ean) {
        Produto produto = produtoRepository.findByEan(ean);
        return Optional.ofNullable(produto);
    }

    public Produto salvar(Produto produto) {
    return produtoRepository.save(produto);
    }
    public Page<Produto> filtrarEstoque(String nome, String ean, Long categoriaId, Long fornecedorId, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("nome").ascending());
        return produtoRepository.buscarComFiltros(nome, ean, categoriaId, fornecedorId, pageable);
    }
    public Map<String, Integer> countProdutosPorCategoria() {
        Map<String, Integer> result = new HashMap<>();
        List<Object[]> rows = produtoRepository.countProdutosPorCategoria();
        for (Object[] r : rows) {
            String categoria = (String) r[0];
            Number total = (Number) r[1];
            result.put(categoria != null ? categoria : "-", total != null ? total.intValue() : 0);
        }
        return result;
    }

    public Page<Produto> listarPaginado(Pageable pageable) {
        return produtoRepository.buscarComFiltros(null, null, null, null, pageable);
    }

}
