package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Permissao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    Optional<Permissao> findByNome(String nome);
}
