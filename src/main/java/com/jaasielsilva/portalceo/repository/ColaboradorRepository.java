package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    
    List<Colaborador> findByAtivoTrue();
    
    Optional<Colaborador> findByCpf(String cpf);
    
    Optional<Colaborador> findByEmail(String email);
    
    @Query("SELECT c FROM Colaborador c WHERE c.ativo = true AND c.id != :excludeId")
    List<Colaborador> findAllActiveExcluding(@Param("excludeId") Long excludeId);
    
    @Query("SELECT c FROM Colaborador c WHERE c.departamento.id = :departamentoId AND c.ativo = true")
    List<Colaborador> findByDepartamentoIdAndAtivoTrue(@Param("departamentoId") Long departamentoId);
    
    @Query("SELECT c FROM Colaborador c WHERE c.cargo.id = :cargoId AND c.ativo = true")
    List<Colaborador> findByCargoIdAndAtivoTrue(@Param("cargoId") Long cargoId);
    
    @Query("SELECT c FROM Colaborador c WHERE c.ativo = true AND c.status = 'ATIVO' ORDER BY c.nome")
    List<Colaborador> findPotentialSupervisors();
    
    @Query("SELECT c FROM Colaborador c WHERE c.ativo = true AND c.status = 'ATIVO' AND c.id != :excludeId ORDER BY c.nome")
    List<Colaborador> findPotentialSupervisorsExcluding(@Param("excludeId") Long excludeId);
    
    @Query("SELECT new Colaborador(c.id, c.nome, c.email, c.cpf) FROM Colaborador c WHERE c.ativo = true ORDER BY c.nome")
    List<Colaborador> findBasicInfoForSelection();
    
    @Query("SELECT COUNT(c) FROM Colaborador c WHERE c.dataAdmissao >= :dataInicio AND c.ativo = true")
    long countContratacoesPorPeriodo(@Param("dataInicio") LocalDate dataInicio);
}
