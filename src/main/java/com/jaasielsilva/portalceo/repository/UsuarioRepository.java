package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNome(String nome);  
    Optional<Usuario> findByEmail(String email);  
    
    long countByStatus(Usuario.Status status);
    Optional<Usuario> findByMatricula(String matricula);
    Optional<Usuario> findByCpf(String cpf);

    // Query para contar usuários com perfil ADMIN
    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.perfis p WHERE p.nome = :nomePerfil")
    long countUsuariosPorPerfil(@Param("nomePerfil") String nomePerfil);

     @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busca, '%'))")
    List<Usuario> buscarPorNomeOuEmail(@Param("busca") String busca);

    
    
}
