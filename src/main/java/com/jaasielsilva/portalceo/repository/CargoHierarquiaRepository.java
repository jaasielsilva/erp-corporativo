package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.CargoHierarquia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoHierarquiaRepository extends JpaRepository<CargoHierarquia, Long> {
    
    /**
     * Busca todos os cargos subordinados de um cargo superior
     */
    @Query("SELECT ch FROM CargoHierarquia ch WHERE ch.cargoSuperior = :cargoSuperior AND ch.ativo = true")
    List<CargoHierarquia> findByCargoSuperiorAndAtivoTrue(@Param("cargoSuperior") Cargo cargoSuperior);
    
    /**
     * Busca o cargo superior de um cargo subordinado
     */
    @Query("SELECT ch FROM CargoHierarquia ch WHERE ch.cargoSubordinado = :cargoSubordinado AND ch.ativo = true")
    Optional<CargoHierarquia> findByCargoSubordinadoAndAtivoTrue(@Param("cargoSubordinado") Cargo cargoSubordinado);
    
    /**
     * Busca cargos por nível hierárquico
     */
    @Query("SELECT ch FROM CargoHierarquia ch WHERE ch.nivelHierarquico = :nivel AND ch.ativo = true ORDER BY ch.cargoSuperior.nome")
    List<CargoHierarquia> findByNivelHierarquicoAndAtivoTrue(@Param("nivel") Integer nivel);
    
    /**
     * Verifica se existe uma relação hierárquica entre dois cargos
     */
    @Query("SELECT ch FROM CargoHierarquia ch WHERE ch.cargoSuperior = :cargoSuperior AND ch.cargoSubordinado = :cargoSubordinado AND ch.ativo = true")
    Optional<CargoHierarquia> findByCargoSuperiorAndCargoSubordinadoAndAtivoTrue(
        @Param("cargoSuperior") Cargo cargoSuperior, 
        @Param("cargoSubordinado") Cargo cargoSubordinado
    );
    
    /**
     * Busca todos os cargos de nível executivo (nível 1)
     */
    @Query("SELECT DISTINCT ch.cargoSuperior FROM CargoHierarquia ch WHERE ch.nivelHierarquico = 1 AND ch.ativo = true")
    List<Cargo> findCargosExecutivos();
}