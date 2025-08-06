package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    
    /**
     * Busca perfil por nome
     */
    Optional<Perfil> findByNome(String nome);
    
    /**
     * Busca perfis que contenham o texto no nome (case insensitive)
     */
    @Query("SELECT p FROM Perfil p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Perfil> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
    /**
     * Lista todos os perfis ordenados por nome
     */
    List<Perfil> findAllByOrderByNomeAsc();
    
    /**
     * Busca perfis que possuem uma permissão específica
     */
    @Query("SELECT p FROM Perfil p JOIN p.permissoes perm WHERE perm = :permissao")
    List<Perfil> findByPermissoesContaining(@Param("permissao") Permissao permissao);
    
    /**
     * Conta quantos perfis possuem uma permissão específica
     */
    @Query("SELECT COUNT(p) FROM Perfil p JOIN p.permissoes perm WHERE perm = :permissao")
    Long countByPermissoesContaining(@Param("permissao") Permissao permissao);
    
    /**
     * Busca perfis que não possuem usuários associados
     */
    @Query("SELECT p FROM Perfil p WHERE NOT EXISTS (SELECT u FROM Usuario u JOIN u.perfis up WHERE up = p) ORDER BY p.nome")
    List<Perfil> findPerfisNaoUtilizados();
    
    /**
     * Busca perfis mais utilizados (com mais usuários)
     */
    @Query("SELECT p FROM Perfil p ORDER BY (SELECT COUNT(u) FROM Usuario u JOIN u.perfis up WHERE up = p) DESC")
    List<Perfil> findPerfisMaisUtilizados();
    
    /**
     * Conta quantos usuários estão associados a um perfil
     */
    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.perfis p WHERE p = :perfil")
    Long countUsuariosByPerfil(@Param("perfil") Perfil perfil);
}
