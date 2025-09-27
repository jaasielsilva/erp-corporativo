package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepository;

    public List<Cargo> listarAtivos() {
    return cargoRepository.findByAtivoTrue();
}

    public List<Cargo> listarTodos() {
        return cargoRepository.findAll();
    }

    public Cargo findById(Long id) {
        return cargoRepository.findById(id).orElse(null);
    }

    public Cargo salvar(Cargo cargo) {
        return cargoRepository.save(cargo);
    }

    public void excluir(Long id) {
        cargoRepository.deleteById(id);
    }

    public int ativarTodosCargos() {
        List<Cargo> todosCargos = cargoRepository.findAll();
        int cargosAtualizados = 0;
        
        for (Cargo cargo : todosCargos) {
            if (!cargo.isAtivo()) {
                cargo.setAtivo(true);
                cargoRepository.save(cargo);
                cargosAtualizados++;
            }
        }
        
        return cargosAtualizados;
    }
}