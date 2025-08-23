package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.ValeTransporte;
import com.jaasielsilva.portalceo.repository.ValeTransporteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValeTransporteService {

    private final ValeTransporteRepository repository;

    public ValeTransporteService(ValeTransporteRepository repository) {
        this.repository = repository;
    }

    public List<ValeTransporte> listarTodos() {
        return repository.findAll();
    }

    public ValeTransporte salvar(ValeTransporte vt) {
        return repository.save(vt);
    }

    public ValeTransporte buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vale Transporte não encontrado: " + id));
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
