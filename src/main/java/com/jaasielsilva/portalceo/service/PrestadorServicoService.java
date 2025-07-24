package com.jaasielsilva.portalceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.jaasielsilva.portalceo.model.PrestadorServico;
import com.jaasielsilva.portalceo.repository.PrestadorServicoRepository;

@Service
public class PrestadorServicoService {

    private final PrestadorServicoRepository prestadorServicoRepository;

    @Autowired
    public PrestadorServicoService(PrestadorServicoRepository prestadorServicoRepository) {
        this.prestadorServicoRepository = prestadorServicoRepository;
    }

    // Busca todos os prestadores ativos
    public List<PrestadorServico> findAllAtivos() {
        return prestadorServicoRepository.findByAtivo(true);
    }

    public PrestadorServico findById(Long id) {
    return prestadorServicoRepository.findById(id).orElse(null);
}
}
