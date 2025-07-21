package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.MovimentacaoEstoqueRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.AuditoriaEstoqueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovimentacaoEstoqueService {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private AuditoriaEstoqueRepository auditoriaRepo;

    public void registrarMovimentacao(Long produtoId, Integer quantidade, TipoMovimentacao tipo, String motivo, String usuario) {
        Produto produto = produtoRepo.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (tipo == TipoMovimentacao.SAIDA && produto.getEstoque() < quantidade) {
            throw new RuntimeException("Estoque insuficiente para saída");
        }

        // Atualiza estoque
        int novoEstoque = switch (tipo) {
            case ENTRADA -> produto.getEstoque() + quantidade;
            case SAIDA, AJUSTE -> produto.getEstoque() - quantidade;
        };

        produto.setEstoque(novoEstoque);
        produtoRepo.save(produto);

        // Salva movimentação
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setQuantidade(quantidade);
        mov.setTipo(tipo);
        mov.setMotivo(motivo);
        mov.setUsuarioResponsavel(usuario);

        movimentacaoRepo.save(mov);

        // Auditoria
        AuditoriaEstoque auditoria = new AuditoriaEstoque();
        auditoria.setAcao(tipo.name());
        auditoria.setDetalhes("Produto: " + produto.getNome() + ", Quantidade: " + quantidade + ", Motivo: " + motivo);
        auditoria.setUsuario(usuario);
        auditoriaRepo.save(auditoria);
    }

   // Busca todas movimentações de um produto
    public List<MovimentacaoEstoque> buscarPorProduto(Produto produto) {
        return movimentacaoRepository.findByProduto(produto);
    }

    // Busca movimentações paginadas filtrando pelo nome do produto (sem filtro tipo)
    public Page<MovimentacaoEstoque> buscarPorNomeProduto(String nomeProduto, Pageable pageable) {
        return movimentacaoRepository.findByProdutoNomeContainingIgnoreCase(nomeProduto, pageable);
    }
    
    public Page<MovimentacaoEstoque> buscarPorNomeProdutoETipo(String nome, String tipoStr, Pageable pageable) {
        TipoMovimentacao tipo;
        try {
            tipo = TipoMovimentacao.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            tipo = null; // ou lançar exceção se preferir
        }

        if (tipo == null) {
            return buscarPorNomeProduto(nome, pageable);
        }
        return movimentacaoRepository.findByProdutoNomeAndTipo(nome, tipo, pageable);
    }

}
