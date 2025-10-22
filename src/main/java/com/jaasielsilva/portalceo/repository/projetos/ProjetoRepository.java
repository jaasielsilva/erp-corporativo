package com.jaasielsilva.portalceo.repository.projetos;

import com.jaasielsilva.portalceo.model.projetos.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findByAtivoTrueOrderByDataCriacaoDesc();

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = com.jaasielsilva.portalceo.model.projetos.Projeto.StatusProjeto.EM_ANDAMENTO")
    long countEmAndamento();

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = com.jaasielsilva.portalceo.model.projetos.Projeto.StatusProjeto.CONCLUIDO")
    long countConcluidos();

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = com.jaasielsilva.portalceo.model.projetos.Projeto.StatusProjeto.ATRASADO")
    long countAtrasados();
}