package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.HistoricoColaborador;
import com.jaasielsilva.portalceo.repository.HistoricoColaboradorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HistoricoColaboradorService {

    private final HistoricoColaboradorRepository historicoRepository;

    public HistoricoColaboradorService(HistoricoColaboradorRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public List<HistoricoColaborador> listarPorColaborador(Long colaboradorId) {
        return historicoRepository.findByColaboradorId(colaboradorId);
    }
}
