package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    List<Colaborador> findByAtivoTrue();

    // Paginação de colaboradores ativos
    Page<Colaborador> findByAtivoTrue(Pageable pageable);

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

    @Query("SELECT new com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO(c.id, c.nome, c.email, c.cpf, cg.nome, d.nome) " +
           "FROM Colaborador c " +
           "LEFT JOIN c.cargo cg " +
           "LEFT JOIN c.departamento d " +
           "WHERE c.ativo = true " +
           "ORDER BY c.nome")
    List<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> findColaboradoresForAjax();

    @Query(value = "SELECT new com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO(c.id, c.nome, c.email, c.cpf, cg.nome, d.nome) " +
           "FROM Colaborador c " +
           "LEFT JOIN c.cargo cg " +
           "LEFT JOIN c.departamento d " +
           "WHERE c.ativo = true " +
           "AND (:q IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.cpf) LIKE LOWER(CONCAT('%', :q, '%')))",
           countQuery = "SELECT COUNT(c) FROM Colaborador c WHERE c.ativo = true AND (:q IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.cpf) LIKE LOWER(CONCAT('%', :q, '%')))"
    )
    Page<com.jaasielsilva.portalceo.dto.ColaboradorSimpleDTO> findColaboradoresForAjax(@Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Colaborador c WHERE c.dataAdmissao >= :dataInicio AND c.ativo = true")
    long countContratacoesPorPeriodo(@Param("dataInicio") LocalDate dataInicio);

    @Query("SELECT c FROM Colaborador c JOIN FETCH c.cargo WHERE c.ativo = true")
    List<Colaborador> findAllWithCargo();
    
    @Query("SELECT c FROM Colaborador c WHERE c.ativo = true AND LOWER(c.cargo.nome) LIKE LOWER(CONCAT('%', :cargoNome, '%'))")
    List<Colaborador> findByAtivoTrueAndCargoNomeContainingIgnoreCase(@Param("cargoNome") String cargoNome);

    @Query("SELECT c FROM Colaborador c WHERE c.ativo = true AND c.status = :status ORDER BY c.nome")
    List<Colaborador> findByAtivoTrueAndStatusOrderByNome(@Param("status") Colaborador.StatusColaborador status);

    Optional<Colaborador> findByUsuario(Usuario usuario);

    @Query("SELECT c FROM Colaborador c JOIN Usuario u ON u.colaborador = c WHERE u.matricula = :matricula")
    Optional<Colaborador> findByUsuarioMatricula(@Param("matricula") String matricula);

    List<Colaborador> findByCargoNomeIgnoreCase(String nomeCargo);

    long countByAtivoTrue();

    long countByAtivoTrueAndTipoContratoIgnoreCase(String tipoContrato);

    long countByAtivoTrueAndTipoContratoContainingIgnoreCase(String tipoContrato);

}
