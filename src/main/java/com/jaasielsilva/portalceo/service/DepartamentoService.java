package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @org.springframework.cache.annotation.Cacheable(value = "departamentosAll")
    public List<Departamento> listarTodos() {
        return departamentoRepository.findAll();
    }

    public List<DepartamentoRepository.DepartamentoIdNomeProjection> listarParaDropdown() {
        return departamentoRepository.findAllIdNome();
    }

    public Departamento findById(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado: " + id));
    }
}
