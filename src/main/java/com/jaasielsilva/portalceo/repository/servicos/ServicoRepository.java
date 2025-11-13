package com.jaasielsilva.portalceo.repository.servicos;

import com.jaasielsilva.portalceo.model.servicos.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByAtivoTrueOrderByNomeAsc();
}