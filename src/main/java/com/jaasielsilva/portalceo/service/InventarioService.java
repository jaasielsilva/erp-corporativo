package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.InventarioRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import com.jaasielsilva.portalceo.repository.AuditoriaEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepo;

    @Autowired
    private ProdutoRepository produtoRepo;

    @Autowired
    private AuditoriaEstoqueRepository auditoriaRepo;

    public void registrarInventario(Long produtoId, Integer quantidadeContada, String responsavel, String obs) {
        Produto produto = produtoRepo.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Inventario inventario = new Inventario();
        inventario.setProduto(produto);
        inventario.setQuantidadeContada(quantidadeContada);
        inventario.setResponsavel(responsavel);
        inventario.setData(LocalDate.now());
        inventario.setObservacoes(obs);
        inventarioRepo.save(inventario);

        int diferenca = quantidadeContada - produto.getEstoque();
        produto.setEstoque(quantidadeContada);
        produtoRepo.save(produto);

        AuditoriaEstoque auditoria = new AuditoriaEstoque();
        auditoria.setAcao("INVENTARIO");
        auditoria.setDetalhes("Produto: " + produto.getNome() + ", Ajuste de " + diferenca + " unidades");
        auditoria.setUsuario(responsavel);
        auditoriaRepo.save(auditoria);
    }
}
