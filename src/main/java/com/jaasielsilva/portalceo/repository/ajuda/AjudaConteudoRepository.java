package com.jaasielsilva.portalceo.repository.ajuda;

import com.jaasielsilva.portalceo.model.ajuda.AjudaConteudo;
import com.jaasielsilva.portalceo.model.ajuda.AjudaCategoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AjudaConteudoRepository extends JpaRepository<AjudaConteudo, Long> {
    @Query(value = "SELECT c FROM AjudaConteudo c LEFT JOIN AjudaFeedback f ON f.conteudo = c AND f.upvote = true WHERE c.publicado = true AND (LOWER(c.titulo) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(c.corpo) LIKE LOWER(CONCAT('%', :term, '%'))) GROUP BY c ORDER BY (COUNT(f.id) * 2 + COALESCE(c.visualizacoes, 0)) DESC",
           countQuery = "SELECT COUNT(c) FROM AjudaConteudo c WHERE c.publicado = true AND (LOWER(c.titulo) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(c.corpo) LIKE LOWER(CONCAT('%', :term, '%')))" )
    Page<AjudaConteudo> searchPublished(@Param("term") String term, Pageable pageable);

    @Query(value = "SELECT c FROM AjudaConteudo c LEFT JOIN AjudaFeedback f ON f.conteudo = c AND f.upvote = true WHERE c.publicado = true GROUP BY c ORDER BY (COUNT(f.id) * 2 + COALESCE(c.visualizacoes, 0)) DESC",
           countQuery = "SELECT COUNT(c) FROM AjudaConteudo c WHERE c.publicado = true")
    Page<AjudaConteudo> findByPublicadoTrue(Pageable pageable);

    @Query(value = "SELECT c FROM AjudaConteudo c LEFT JOIN AjudaFeedback f ON f.conteudo = c AND f.upvote = true WHERE c.publicado = true AND c.categoria = :categoria GROUP BY c ORDER BY (COUNT(f.id) * 2 + COALESCE(c.visualizacoes, 0)) DESC",
           countQuery = "SELECT COUNT(c) FROM AjudaConteudo c WHERE c.publicado = true AND c.categoria = :categoria")
    Page<AjudaConteudo> findByPublicadoTrueAndCategoria(@Param("categoria") AjudaCategoria categoria, Pageable pageable);
}
