package com.jaasielsilva.portalceo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jaasielsilva.portalceo.model.ContratoFornecedor;
import com.jaasielsilva.portalceo.model.Fornecedor;
import com.jaasielsilva.portalceo.repository.ContratoFornecedorRepository;

@Service
public class ContratoFornecedorService {
    
    @Autowired
    private ContratoFornecedorRepository contratoRepo;

    public String gerarProximoNumeroContrato() {
        int year = LocalDate.now().getYear();
        ContratoFornecedor ultimoContrato = contratoRepo.findTopByOrderByIdDesc();
        
        long proximoId = 1;
        if (ultimoContrato != null && ultimoContrato.getId() != null) {
            proximoId = ultimoContrato.getId() + 1;
        }
        
        return String.format("%d%04d", year, proximoId);
    }

    public List<ContratoFornecedor> findByFornecedor(Fornecedor fornecedor) {
        return contratoRepo.findByFornecedor(fornecedor);
    }

    public ContratoFornecedor findById(Long id) {
        return contratoRepo.findById(id).orElseThrow();
    }

    public void salvar(ContratoFornecedor contrato) {
        contratoRepo.save(contrato);
    }

    public void excluir(Long id) {
        contratoRepo.deleteById(id);
    }

    public List<ContratoFornecedor> listarTodos() {
    return contratoRepo.findAll();
}

}

