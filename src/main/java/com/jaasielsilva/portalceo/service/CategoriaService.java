package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> listarTodas() {
        return repository.findAll();
    }

    public Categoria salvar(Categoria categoria) {
        return repository.save(categoria);
    }

    public Categoria buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}
