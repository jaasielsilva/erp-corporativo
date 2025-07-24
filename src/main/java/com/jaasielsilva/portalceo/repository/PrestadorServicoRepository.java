package com.jaasielsilva.portalceo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jaasielsilva.portalceo.model.PrestadorServico;

public interface PrestadorServicoRepository extends JpaRepository<PrestadorServico, Long> {
    List<PrestadorServico> findByAtivo(boolean ativo);
}