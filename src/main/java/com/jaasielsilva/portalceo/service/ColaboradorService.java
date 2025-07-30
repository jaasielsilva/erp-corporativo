package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColaboradorService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    public List<Colaborador> listarAtivos() {
        return colaboradorRepository.findByAtivoTrue();
    }

    public Colaborador findById(Long id) {
        return colaboradorRepository.findById(id).orElse(null);
    }

    public Colaborador salvar(Colaborador colaborador) {
        return colaboradorRepository.save(colaborador);
    }

    public void excluir(Long id) {
        Colaborador col = findById(id);
        if (col != null) {
            col.setAtivo(false);
            salvar(col);
        }
    }
}
