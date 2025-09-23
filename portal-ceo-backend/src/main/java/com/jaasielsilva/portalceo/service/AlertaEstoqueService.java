package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.AlertaEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.AlertaEstoqueRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertaEstoqueService {

    @Autowired
    private ProdutoRepository produtoRepo;

    @Autowired
    private AlertaEstoqueRepository alertaRepo;

    public void verificarAlertas() {
        List<Produto> produtos = produtoRepo.findAll();
        for (Produto p : produtos) {
            if (p.getEstoque() <= p.getMinimoEstoque()) {
                AlertaEstoque alerta = new AlertaEstoque();
                alerta.setProduto(p);
                alerta.setMensagem("Produto com estoque crítico: " + p.getNome());
                alerta.setResolvido(false);
                alertaRepo.save(alerta);
            }
        }
    }

    public List<AlertaEstoque> listarPendentes() {
        return alertaRepo.findAll()
                .stream()
                .filter(alerta -> !alerta.isResolvido())
                .toList();
    }
}