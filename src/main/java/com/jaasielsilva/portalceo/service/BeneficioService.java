package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Beneficio;
import com.jaasielsilva.portalceo.model.PlanoSaude;
import com.jaasielsilva.portalceo.repository.BeneficioRepository;
import com.jaasielsilva.portalceo.repository.PlanoSaudeRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class BeneficioService {

    private final BeneficioRepository beneficioRepository;

    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;
    
    
    public BeneficioService(BeneficioRepository beneficioRepository) {
        this.beneficioRepository = beneficioRepository;
    }

    /**
     * Lista todos os benefícios disponíveis
     */
    public List<Beneficio> listarTodos() {
        return beneficioRepository.findAll();
    }

    /**
     * Busca um benefício pelo ID
     */
    public Optional<Beneficio> buscarPorId(Long id) {
        return beneficioRepository.findById(id);
    }

    /**
     * Salva ou atualiza um benefício
     */
    public Beneficio salvar(Beneficio beneficio) {
        return beneficioRepository.save(beneficio);
    }

    /**
     * Exclui um benefício pelo ID
     */
    public void excluir(Long id) {
        beneficioRepository.deleteById(id);
    }

    public Optional<PlanoSaude> buscarPlanoPorNome(String nome) {
        return planoSaudeRepository.findByNome(nome);
    }
}
