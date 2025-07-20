package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.TransferenciaEstoqueRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.AuditoriaEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferenciaEstoqueService {

    @Autowired
    private TransferenciaEstoqueRepository transferenciaRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

    @Autowired
    private AuditoriaEstoqueRepository auditoriaRepo;

    public void transferir(Long produtoId, Integer quantidade, String origem, String destino, String usuario) {
        Produto produto = produtoRepo.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getEstoque() < quantidade) {
            throw new RuntimeException("Estoque insuficiente para transferência");
        }

        produto.setEstoque(produto.getEstoque() - quantidade);
        produtoRepo.save(produto);

        TransferenciaEstoque transf = new TransferenciaEstoque();
        transf.setProduto(produto);
        transf.setQuantidade(quantidade);
        transf.setLocalOrigem(origem);
        transf.setLocalDestino(destino);
        transf.setResponsavel(usuario);
        transferenciaRepo.save(transf);

        // Auditoria
        AuditoriaEstoque auditoria = new AuditoriaEstoque();
        auditoria.setAcao("TRANSFERENCIA");
        auditoria.setDetalhes("Produto: " + produto.getNome() + ", De: " + origem + ", Para: " + destino + ", Qtd: " + quantidade);
        auditoria.setUsuario(usuario);
        auditoriaRepo.save(auditoria);
    }
}
