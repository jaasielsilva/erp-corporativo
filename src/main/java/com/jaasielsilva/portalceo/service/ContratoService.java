package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import com.jaasielsilva.portalceo.repository.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepository;

    public List<Contrato> findAll() {
        return contratoRepository.findAll();
    }

    public Contrato findById(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado: " + id));
    }

    public Contrato save(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public void deleteById(Long id) {
        contratoRepository.deleteById(id);
    }

    // metodo pra salvar um contrato
    public Contrato salvar(Contrato contrato) {
        return contratoRepository.save(contrato);
    }

    public List<Contrato> findByTipo(TipoContrato tipo) {
    return contratoRepository.findByTipo(tipo);
}

}
