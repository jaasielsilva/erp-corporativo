package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.AuditoriaEstoque;
import com.jaasielsilva.portalceo.repository.AuditoriaEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaEstoqueService {

    @Autowired
    private AuditoriaEstoqueRepository auditoriaRepo;

    public List<AuditoriaEstoque> listarTudo() {
        return auditoriaRepo.findAll();
    }
}
