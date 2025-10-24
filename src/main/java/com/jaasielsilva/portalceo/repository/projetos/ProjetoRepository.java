package com.jaasielsilva.portalceo.repository.projetos;

import com.jaasielsilva.portalceo.model.projetos.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.equipe e LEFT JOIN FETCH e.membros LEFT JOIN FETCH p.tarefas WHERE p.ativo = true ORDER BY p.dataCriacao DESC")
List<Projeto> findAllActiveWithEquipeAndMembros();

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = com.jaasielsilva.portalceo.model.projetos.Projeto.StatusProjeto.EM_ANDAMENTO")
    long countEmAndamento();

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = com.jaasielsilva.portalceo.model.projetos.Projeto.StatusProjeto.CONCLUIDO")
    long countConcluidos();

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = com.jaasielsilva.portalceo.model.projetos.Projeto.StatusProjeto.ATRASADO")
    long countAtrasados();

    // Fetch-join para evitar LazyInitializationException no template do cronograma
    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.equipe e LEFT JOIN FETCH e.membros WHERE p.id = :id")
    Optional<Projeto> findByIdWithEquipeAndMembros(@Param("id") Long id);
}