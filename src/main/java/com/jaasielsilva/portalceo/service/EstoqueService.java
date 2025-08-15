package com.jaasielsilva.portalceo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.AuditoriaEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.repository.AuditoriaEstoqueRepository;
import com.jaasielsilva.portalceo.repository.ProdutoRepository;

@Service
public class EstoqueService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuditoriaEstoqueRepository auditoriaEstoqueRepository;

    // Registrar entrada no estoque
    public void registrarEntrada(Long produtoId, Integer quantidade, String motivo, String usuario) {
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setEstoque(produto.getEstoque() + quantidade);
        produtoRepository.save(produto);

        AuditoriaEstoque auditoria = new AuditoriaEstoque();
        auditoria.setAcao("ENTRADA");
        auditoria.setDetalhes("Produto: " + produto.getNome() + ", Quantidade: " + quantidade + ", Motivo: " + motivo);
        auditoria.setUsuario(usuario);
        // dataHora será setada pelo @PrePersist

        auditoriaEstoqueRepository.save(auditoria);
    }

    // Registrar saída no estoque
    public void registrarSaida(Long produtoId, Integer quantidade, String motivo, String usuario) {
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        int estoqueAtual = produto.getEstoque();
        if (estoqueAtual < quantidade) {
            throw new RuntimeException("Estoque insuficiente para saída");
        }

        produto.setEstoque(estoqueAtual - quantidade);
        produtoRepository.save(produto);

        AuditoriaEstoque auditoria = new AuditoriaEstoque();
        auditoria.setAcao("SAÍDA");
        auditoria.setDetalhes("Produto: " + produto.getNome() + ", Quantidade: " + quantidade + ", Motivo: " + motivo);
        auditoria.setUsuario(usuario);

        auditoriaEstoqueRepository.save(auditoria);
    }
    
    // Calcula performance de logística baseada na eficiência do estoque
    public int calcularPerformanceLogistica() {
        long totalProdutos = produtoRepository.count();
        long produtosCriticos = produtoRepository.countEstoqueCritico();
        
        if (totalProdutos == 0) {
            return 85; // Valor padrão
        }
        
        // Calcula percentual de produtos com estoque adequado
        double percentualAdequado = ((double)(totalProdutos - produtosCriticos) / totalProdutos) * 100;
        
        // Converte para escala de performance (0-100)
        return (int) Math.min(100, Math.max(0, percentualAdequado));
    }
}