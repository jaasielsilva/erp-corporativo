package com.jaasielsilva.portalceo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.repository.AdesaoPlanoSaudeRepository;
import java.util.List;

@Service
public class AdesaoPlanoSaudeService {

    @Autowired
    private AdesaoPlanoSaudeRepository repository;

    public List<AdesaoPlanoSaude> listarTodos() {
        return repository.findAll();
    }

    public AdesaoPlanoSaude salvar(AdesaoPlanoSaude adesao) {
        return repository.save(adesao);
    }

    public AdesaoPlanoSaude buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RuntimeException("Adesão ao plano de saúde não encontrada: " + id));
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public List<AdesaoPlanoSaude> listarPorColaborador(Long colaboradorId) {
        return repository.findByColaboradorId(colaboradorId);
    }
}
