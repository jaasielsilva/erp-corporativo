package com.jaasielsilva.portalceo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.repository.AdesaoPlanoSaudeRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AdesaoPlanoSaudeService {

    @Autowired
    private AdesaoPlanoSaudeRepository repository;

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

    public List<AdesaoPlanoSaude> listarAtivos() {
        return repository.findByStatus("ATIVO");
    }

    public List<AdesaoPlanoSaude> listarPendentes() {
        return repository.findByStatus("PENDENTE");
    }

    public List<AdesaoPlanoSaude> listarCancelados() {
        return repository.findByStatus("CANCELADO");
    }

    public List<AdesaoPlanoSaude> listarTodos() {
        return repository.findAllComVinculos();
    }

    public List<Colaborador> listarColaboradoresComBeneficio() {
        return repository.findDistinctColaboradoresComAdesao();
    }

    // cria paginção para listar adesões
    public Page<AdesaoPlanoSaude> listarTodosPaginado(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
