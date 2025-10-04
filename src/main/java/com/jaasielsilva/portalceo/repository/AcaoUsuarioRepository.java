package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.dto.AcaoUsuarioDTO;
import com.jaasielsilva.portalceo.model.AcaoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AcaoUsuarioRepository extends JpaRepository<AcaoUsuario, Long> {

    @Query("SELECT new com.jaasielsilva.portalceo.dto.AcaoUsuarioDTO(a.data, a.acao, u.nome, r.nome) " +
           "FROM AcaoUsuario a " +
           "JOIN a.usuario u " +
           "JOIN a.responsavel r " +
           "ORDER BY a.data DESC")
    Page<AcaoUsuarioDTO> buscarUltimasAcoes(Pageable pageable);
}
