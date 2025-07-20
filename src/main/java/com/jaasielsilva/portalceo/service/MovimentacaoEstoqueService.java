package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.MovimentacaoEstoqueRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.AuditoriaEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovimentacaoEstoqueService {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

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
}
