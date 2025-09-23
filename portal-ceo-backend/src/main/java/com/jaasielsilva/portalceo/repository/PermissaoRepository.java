package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    
    /**
     * Busca permissão por nome
     */
    Optional<Permissao> findByNome(String nome);
    
    /**
     * Busca permissões que contenham o texto no nome (case insensitive)
     */
    @Query("SELECT p FROM Permissao p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Permissao> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
    /**
     * Lista todas as permissões ordenadas por nome
     */
    List<Permissao> findAllByOrderByNomeAsc();
    
    /**
     * Busca permissões por categoria
     */
    @Query("SELECT p FROM Permissao p WHERE p.categoria = :categoria ORDER BY p.nome")
    List<Permissao> findByCategoriaOrderByNome(@Param("categoria") String categoria);
    
    /**
     * Lista todas as categorias distintas
     */
    @Query("SELECT DISTINCT p.categoria FROM Permissao p WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> findDistinctCategorias();
    
    /**
     * Conta permissões por categoria
     */
    @Query("SELECT COUNT(p) FROM Permissao p WHERE p.categoria = :categoria")
    Long countByCategoria(@Param("categoria") String categoria);
    
    /**
     * Busca permissões que não estão sendo utilizadas por nenhum perfil
     */
    @Query("SELECT p FROM Permissao p WHERE NOT EXISTS (SELECT pr FROM Perfil pr JOIN pr.permissoes pp WHERE pp = p) ORDER BY p.nome")
    List<Permissao> findPermissoesNaoUtilizadas();
    
    /**
     * Busca permissões mais utilizadas (em mais perfis)
     */
    @Query("SELECT p FROM Permissao p ORDER BY (SELECT COUNT(pr) FROM Perfil pr JOIN pr.permissoes pp WHERE pp = p) DESC")
    List<Permissao> findPermissoesMaisUtilizadas();
}
