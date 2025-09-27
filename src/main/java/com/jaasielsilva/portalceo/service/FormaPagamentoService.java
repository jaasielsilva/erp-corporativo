package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.FormaPagamento;
import com.jaasielsilva.portalceo.repository.FormaPagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FormaPagamentoService {

    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;

    // Buscar todas as formas de pagamento ativas
    public List<FormaPagamento> buscarFormasAtivas() {
        return formaPagamentoRepository.findByAtivoTrueOrderByOrdemExibicao();
    }

    // Buscar todas as formas de pagamento
    public List<FormaPagamento> buscarTodas() {
        return formaPagamentoRepository.findAll();
    }

    public List<FormaPagamento> buscarAtivas() {
        return formaPagamentoRepository.findAll();
    }

    // Buscar por ID
    public Optional<FormaPagamento> buscarPorId(Long id) {
        return formaPagamentoRepository.findById(id);
    }

    // Buscar por nome
    public Optional<FormaPagamento> buscarPorNome(String nome) {
        return formaPagamentoRepository.findByNomeIgnoreCase(nome);
    }

    // Buscar formas que aceitam parcelas
    public List<FormaPagamento> buscarFormasQueAceitamParcelas() {
        return formaPagamentoRepository.findFormasQueAceitamParcelas();
    }

    // Salvar forma de pagamento
    public FormaPagamento salvar(FormaPagamento formaPagamento) {
        return formaPagamentoRepository.save(formaPagamento);
    }

    // Ativar/Desativar forma de pagamento
    public FormaPagamento alterarStatus(Long id, Boolean ativo) {
        Optional<FormaPagamento> formaOpt = buscarPorId(id);
        if (formaOpt.isPresent()) {
            FormaPagamento forma = formaOpt.get();
            forma.setAtivo(ativo);
            return salvar(forma);
        }
        throw new IllegalArgumentException("Forma de pagamento não encontrada");
    }

    // Deletar forma de pagamento
    public void deletar(Long id) {
        formaPagamentoRepository.deleteById(id);
    }

    // Inicializar formas de pagamento padrão
    public void inicializarFormasPadrao() {
        if (formaPagamentoRepository.count() == 0) {
            List<FormaPagamento> formasPadrao = List.of(
                new FormaPagamento("Dinheiro", "Pagamento em dinheiro", false, 1),
                new FormaPagamento("PIX", "Pagamento via PIX", false, 1),
                new FormaPagamento("Cartão de Débito", "Pagamento com cartão de débito", false, 1),
                new FormaPagamento("Cartão de Crédito", "Pagamento com cartão de crédito", true, 12)
            );
            
            // Definir ordem de exibição
            for (int i = 0; i < formasPadrao.size(); i++) {
                formasPadrao.get(i).setOrdemExibicao(i + 1);
            }
            
            formaPagamentoRepository.saveAll(formasPadrao);
        }
    }

    // Verificar se forma aceita parcelas
    public boolean aceitaParcelas(String nomeForma) {
        Optional<FormaPagamento> forma = buscarPorNome(nomeForma);
        return forma.map(FormaPagamento::getAceitaParcelas).orElse(false);
    }

    // Obter máximo de parcelas
    public Integer getMaxParcelas(String nomeForma) {
        Optional<FormaPagamento> forma = buscarPorNome(nomeForma);
        return forma.map(FormaPagamento::getMaxParcelas).orElse(1);
    }
}