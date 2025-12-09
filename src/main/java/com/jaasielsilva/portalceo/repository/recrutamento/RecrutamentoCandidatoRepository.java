package com.jaasielsilva.portalceo.repository.recrutamento;

import com.jaasielsilva.portalceo.model.recrutamento.RecrutamentoCandidato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RecrutamentoCandidatoRepository extends JpaRepository<RecrutamentoCandidato, Long> {
    @Query("SELECT c FROM RecrutamentoCandidato c WHERE " +
            "(:q IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.telefone) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
            "(:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:telefone IS NULL OR c.telefone LIKE CONCAT('%', :telefone, '%')) AND " +
            "(:genero IS NULL OR c.genero = :genero) AND " +
            "(:nasc IS NULL OR c.dataNascimento = :nasc)" )
    Page<RecrutamentoCandidato> buscarComFiltros(@Param("q") String q,
                                                 @Param("nome") String nome,
                                                 @Param("email") String email,
                                                 @Param("telefone") String telefone,
                                                 @Param("genero") String genero,
                                                 @Param("nasc") LocalDate nasc,
                                                 Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);
}
