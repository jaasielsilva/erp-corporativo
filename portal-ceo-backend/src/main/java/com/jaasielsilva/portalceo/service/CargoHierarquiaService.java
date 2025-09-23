package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.CargoHierarquia;
import com.jaasielsilva.portalceo.repository.CargoHierarquiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CargoHierarquiaService {
    
    @Autowired
    private CargoHierarquiaRepository cargoHierarquiaRepository;
    
    /**
     * Busca todos os cargos subordinados de um cargo superior
     */
    public List<CargoHierarquia> buscarSubordinados(Cargo cargoSuperior) {
        return cargoHierarquiaRepository.findByCargoSuperiorAndAtivoTrue(cargoSuperior);
    }
    
    /**
     * Busca o cargo superior de um cargo subordinado
     */
    public Optional<CargoHierarquia> buscarSuperior(Cargo cargoSubordinado) {
        return cargoHierarquiaRepository.findByCargoSubordinadoAndAtivoTrue(cargoSubordinado);
    }
    
    /**
     * Busca cargos por nível hierárquico
     */
    public List<CargoHierarquia> buscarPorNivel(Integer nivel) {
        return cargoHierarquiaRepository.findByNivelHierarquicoAndAtivoTrue(nivel);
    }
    
    /**
     * Verifica se um cargo pode supervisionar outro
     */
    public boolean podeSupervisionarCargo(Cargo cargoSuperior, Cargo cargoSubordinado) {
        return cargoHierarquiaRepository.findByCargoSuperiorAndCargoSubordinadoAndAtivoTrue(
            cargoSuperior, cargoSubordinado
        ).isPresent();
    }
    
    /**
     * Busca todos os cargos executivos (nível 1)
     */
    public List<Cargo> buscarCargosExecutivos() {
        return cargoHierarquiaRepository.findCargosExecutivos();
    }
    
    /**
     * Cria uma nova relação hierárquica
     */
    public CargoHierarquia criarRelacaoHierarquica(Cargo cargoSuperior, Cargo cargoSubordinado, 
                                                   Integer nivel, String descricao) {
        // Verifica se já existe uma relação
        Optional<CargoHierarquia> existente = cargoHierarquiaRepository
            .findByCargoSuperiorAndCargoSubordinadoAndAtivoTrue(cargoSuperior, cargoSubordinado);
        
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Já existe uma relação hierárquica entre estes cargos");
        }
        
        CargoHierarquia hierarquia = new CargoHierarquia();
        hierarquia.setCargoSuperior(cargoSuperior);
        hierarquia.setCargoSubordinado(cargoSubordinado);
        hierarquia.setNivelHierarquico(nivel);
        hierarquia.setDescricao(descricao);
        hierarquia.setAtivo(true);
        
        return cargoHierarquiaRepository.save(hierarquia);
    }
    
    /**
     * Desativa uma relação hierárquica
     */
    public void desativarRelacao(Long id) {
        CargoHierarquia hierarquia = cargoHierarquiaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Relação hierárquica não encontrada"));
        
        hierarquia.setAtivo(false);
        cargoHierarquiaRepository.save(hierarquia);
    }
    
    /**
     * Verifica se um cargo está em um nível hierárquico superior a outro
     */
    public boolean isCargoSuperior(Cargo cargo1, Cargo cargo2) {
        Optional<CargoHierarquia> hierarquia1 = buscarSuperior(cargo1);
        Optional<CargoHierarquia> hierarquia2 = buscarSuperior(cargo2);
        
        if (hierarquia1.isEmpty() && hierarquia2.isEmpty()) {
            return false; // Ambos no mesmo nível
        }
        
        if (hierarquia1.isEmpty()) {
            return true; // cargo1 é executivo
        }
        
        if (hierarquia2.isEmpty()) {
            return false; // cargo2 é executivo
        }
        
        return hierarquia1.get().getNivelHierarquico() < hierarquia2.get().getNivelHierarquico();
    }
}