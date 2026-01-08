package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNome(String nome);

    interface DepartamentoIdNomeProjection {
        Long getId();
        String getNome();
    }

    @Query("SELECT d.id as id, d.nome as nome FROM Departamento d ORDER BY d.nome ASC")
    List<DepartamentoIdNomeProjection> findAllIdNome();
}
