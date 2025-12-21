package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.ContaBancaria;
import com.jaasielsilva.portalceo.repository.ContaBancariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ContaBancariaService {

    @Autowired
    private ContaBancariaRepository repository;

    public List<ContaBancaria> listarContasAtivas() {
        return repository.findByAtivoTrue();
    }

    public Optional<ContaBancaria> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public ContaBancaria salvar(ContaBancaria conta) {
        return repository.save(conta);
    }

    public void inicializarContasSeNecessario() {
        criarContaSeNaoExistir("Caixa Principal");
        criarContaSeNaoExistir("Banco Itaú");
        criarContaSeNaoExistir("Banco Santander");
        criarContaSeNaoExistir("Nubank");
    }

    private void criarContaSeNaoExistir(String nome) {
        if (!repository.existsByNome(nome)) {
            repository.save(new ContaBancaria(nome, BigDecimal.ZERO));
        }
    }

    /**
     * Adiciona saldo a uma conta bancária.
     * Utilizado para aportes e entradas manuais.
     * 
     * @param contaId ID da conta a receber o valor
     * @param valor Valor a ser creditado
     */
    @org.springframework.transaction.annotation.Transactional
    public void creditar(Long contaId, BigDecimal valor) {
        ContaBancaria conta = repository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta bancária não encontrada"));
        conta.setSaldo(conta.getSaldo().add(valor));
        repository.save(conta);
    }

    /**
     * Deduz saldo de uma conta bancária.
     * Utilizado para pagamentos de folha, contas a pagar e retiradas.
     * 
     * Nota: Atualmente permite saldo negativo (cheque especial/limite), 
     * mas poderia ser restringido com validação extra.
     * 
     * @param contaId ID da conta a ser debitada
     * @param valor Valor a ser debitado
     */
    @org.springframework.transaction.annotation.Transactional
    public void debitar(Long contaId, BigDecimal valor) {
        ContaBancaria conta = repository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta bancária não encontrada"));
        // Permite saldo negativo? Por enquanto sim, mas podemos validar
        conta.setSaldo(conta.getSaldo().subtract(valor));
        repository.save(conta);
    }
}
