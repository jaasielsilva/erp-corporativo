package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ReacaoMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReacaoMensagemRepository extends JpaRepository<ReacaoMensagem, Long> {
    List<ReacaoMensagem> findByMensagemId(Long mensagemId);
}