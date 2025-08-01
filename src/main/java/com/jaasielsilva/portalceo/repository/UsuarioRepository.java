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

     // Conta quantos usuários têm o perfil ADMIN
    @Query("select count(u) from Usuario u join u.perfis p where p.nome = :perfilNome")
    long countByPerfilNome(@Param("perfilNome") String perfilNome);

    // Outra consulta para verificar se há pelo menos um admin diferente de um dado usuário
    @Query("select count(u) from Usuario u join u.perfis p where p.nome = :perfilNome and u.id <> :userId")
    long countByPerfilNomeExcludingUser(@Param("perfilNome") String perfilNome, @Param("userId") Long userId);

   Optional<Usuario> findByColaborador_Id(Long colaboradorId);
    
}
