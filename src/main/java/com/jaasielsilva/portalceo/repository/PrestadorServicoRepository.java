package com.jaasielsilva.portalceo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.jaasielsilva.portalceo.model.PrestadorServico;

public interface PrestadorServicoRepository extends JpaRepository<PrestadorServico, Long> {
    List<PrestadorServico> findByAtivo(boolean ativo);

    @Query("SELECT new PrestadorServico(p.id, p.nome) FROM PrestadorServico p WHERE p.ativo = true ORDER BY p.nome")
    List<PrestadorServico> findBasicInfoForSelection();
}