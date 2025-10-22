package com.jaasielsilva.portalceo.repository.projetos;

import com.jaasielsilva.portalceo.model.projetos.TarefaProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaProjetoRepository extends JpaRepository<TarefaProjeto, Long> {
    List<TarefaProjeto> findByProjetoId(Long projetoId);
    long countByStatus(com.jaasielsilva.portalceo.model.projetos.TarefaProjeto.StatusTarefa status);
    long countByProjetoIdAndStatus(Long projetoId, com.jaasielsilva.portalceo.model.projetos.TarefaProjeto.StatusTarefa status);
    java.util.List<com.jaasielsilva.portalceo.model.projetos.TarefaProjeto> findByAtribuidaAId(Long colaboradorId);
}