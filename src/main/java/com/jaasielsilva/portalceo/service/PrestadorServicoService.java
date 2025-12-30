package com.jaasielsilva.portalceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

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
    @Cacheable(value = "prestadoresAtivos", unless = "#result == null || #result.isEmpty()")
    public List<PrestadorServico> findAllAtivos() {
        return prestadorServicoRepository.findByAtivo(true);
    }

    @Cacheable(value = "prestadoresSelecao", unless = "#result == null || #result.isEmpty()")
    public List<PrestadorServico> listarAtivosParaSelecao() {
        return prestadorServicoRepository.findBasicInfoForSelection();
    }

    public PrestadorServico findById(Long id) {
    return prestadorServicoRepository.findById(id).orElse(null);
}
}
