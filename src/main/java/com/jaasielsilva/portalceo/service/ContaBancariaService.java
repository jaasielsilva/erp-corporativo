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
        if (repository.count() == 0) {
            repository.save(new ContaBancaria("Caixa Principal", BigDecimal.ZERO));
            repository.save(new ContaBancaria("Banco Itaú", BigDecimal.ZERO));
            repository.save(new ContaBancaria("Banco Santander", BigDecimal.ZERO));
        }
    }
}
