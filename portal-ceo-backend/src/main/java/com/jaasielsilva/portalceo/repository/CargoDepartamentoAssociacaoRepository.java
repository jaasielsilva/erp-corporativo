package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.CargoDepartamentoAssociacao;
import com.jaasielsilva.portalceo.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoDepartamentoAssociacaoRepository extends JpaRepository<CargoDepartamentoAssociacao, Long> {
    
    /**
     * Busca todos os cargos válidos para um departamento
     */
    @Query("SELECT cda.cargo FROM CargoDepartamentoAssociacao cda WHERE cda.departamento = :departamento AND cda.ativo = true")
    List<Cargo> findCargosByDepartamentoAndAtivoTrue(@Param("departamento") Departamento departamento);
    
    /**
     * Busca todos os departamentos válidos para um cargo
     */
    @Query("SELECT cda.departamento FROM CargoDepartamentoAssociacao cda WHERE cda.cargo = :cargo AND cda.ativo = true")
    List<Departamento> findDepartamentosByCargoAndAtivoTrue(@Param("cargo") Cargo cargo);
    
    /**
     * Verifica se uma associação cargo-departamento é válida
     */
    @Query("SELECT cda FROM CargoDepartamentoAssociacao cda WHERE cda.cargo = :cargo AND cda.departamento = :departamento AND cda.ativo = true")
    Optional<CargoDepartamentoAssociacao> findByCargoAndDepartamentoAndAtivoTrue(
        @Param("cargo") Cargo cargo, 
        @Param("departamento") Departamento departamento
    );
    
    /**
     * Busca associações obrigatórias para um cargo
     */
    @Query("SELECT cda FROM CargoDepartamentoAssociacao cda WHERE cda.cargo = :cargo AND cda.obrigatorio = true AND cda.ativo = true")
    List<CargoDepartamentoAssociacao> findByCargoAndObrigatorioTrueAndAtivoTrue(@Param("cargo") Cargo cargo);
    
    /**
     * Busca todas as associações ativas
     */
    @Query("SELECT cda FROM CargoDepartamentoAssociacao cda WHERE cda.ativo = true ORDER BY cda.departamento.nome, cda.cargo.nome")
    List<CargoDepartamentoAssociacao> findAllAtivasOrderByDepartamentoAndCargo();
    
    /**
     * Verifica se um cargo tem associações obrigatórias
     */
    @Query("SELECT COUNT(cda) > 0 FROM CargoDepartamentoAssociacao cda WHERE cda.cargo = :cargo AND cda.obrigatorio = true AND cda.ativo = true")
    boolean hasAssociacaoObrigatoria(@Param("cargo") Cargo cargo);
}