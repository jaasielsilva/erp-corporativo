package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.repository.CategoriaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository repository) {
        this.categoriaRepository = repository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria salvar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public void deletar(Long id) {
        categoriaRepository.deleteById(id);
    }
}
