package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.TipoMovimentacao;
import com.jaasielsilva.portalceo.repository.MovimentacaoEstoqueRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

    public void registrarMovimentacao(Long produtoId, Integer quantidade, TipoMovimentacao tipo, String motivo, String usuario) {
    Produto produto = produtoRepo.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

    if (tipo == TipoMovimentacao.SAIDA && produto.getEstoque() < quantidade) {
        throw new RuntimeException("Estoque insuficiente para saída");
    }

    int novoEstoque = switch (tipo) {
        case ENTRADA -> produto.getEstoque() + quantidade;
        case SAIDA, AJUSTE -> produto.getEstoque() - quantidade;
    };

    produto.setEstoque(novoEstoque);
    produtoRepo.save(produto);

    MovimentacaoEstoque mov = new MovimentacaoEstoque();
    mov.setProduto(produto);
    mov.setQuantidade(quantidade);
    mov.setTipo(tipo);
    mov.setMotivo(motivo);
    mov.setUsuarioResponsavel(usuario);

    movimentacaoRepo.save(mov);
}
    // Busca todas movimentações de um produto (não paginado)
    public List<MovimentacaoEstoque> buscarPorProduto(Produto produto) {
        return movimentacaoRepo.findByProdutoOrderByDataHoraDesc(produto);
    }

    // Busca movimentações paginadas filtrando pelo nome do produto (sem filtro tipo)
    public Page<MovimentacaoEstoque> buscarPorNomeProduto(String nomeProduto, Pageable pageable) {
        return movimentacaoRepo.findByProdutoNomeContainingIgnoreCase(nomeProduto, pageable);
    }

    // Busca movimentações paginadas filtrando pelo nome do produto e tipo
    public Page<MovimentacaoEstoque> buscarPorNomeProdutoETipo(String nome, String tipoStr, Pageable pageable) {
        TipoMovimentacao tipo;
        try {
            tipo = TipoMovimentacao.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            tipo = null;
        }

        if (tipo == null) {
            return buscarPorNomeProduto(nome, pageable);
        }
        return movimentacaoRepo.findByProdutoNomeAndTipo(nome, tipo, pageable);
    }

   public List<MovimentacaoEstoque> buscarUltimosAjustes() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("dataHora").descending());
    return movimentacaoRepo.findAll(pageable).getContent();
}
    
}
