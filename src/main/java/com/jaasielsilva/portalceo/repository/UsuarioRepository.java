package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNome(String nome);  
    Optional<Usuario> findByEmail(String email);  
    // Método removido - campo 'usuario' não existe na entidade Usuario
    // Use findByEmail ou findByMatricula para autenticação
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
   
   // Métodos para gerenciamento de perfis
   List<Usuario> findByPerfisContaining(Perfil perfil);
   long countByPerfisContaining(Perfil perfil);
   
   @Query("SELECT u FROM Usuario u JOIN u.perfis p WHERE p.id = :perfilId")
   List<Usuario> findByPerfilId(@Param("perfilId") Long perfilId);
   
   @Query("SELECT COUNT(u) FROM Usuario u JOIN u.perfis p WHERE p.id = :perfilId")
   long countByPerfilId(@Param("perfilId") Long perfilId);
   
   // Método para buscar usuários ativos excluindo um usuário específico (para chat)
   List<Usuario> findByIdNotAndStatusOrderByNome(Long id, Usuario.Status status);
   
   // Método para buscar usuários ativos excluindo um usuário específico com filtro de nome (para chat)
   List<Usuario> findByIdNotAndStatusAndNomeContainingIgnoreCaseOrderByNome(Long id, Usuario.Status status, String nome);
   
   // Método otimizado para busca simples de usuário (apenas campos básicos)
   @Query("SELECT new Usuario(u.id, u.nome, u.email, u.fotoPerfil, u.online, u.status) FROM Usuario u WHERE u.email = :email")
   Optional<Usuario> findByEmailSimple(@Param("email") String email);
   
   // Método otimizado para busca por nome (apenas campos básicos)
   @Query("SELECT new Usuario(u.id, u.nome, u.email, u.fotoPerfil, u.online, u.status) FROM Usuario u WHERE u.nome = :nome")
   Optional<Usuario> findByNomeSimple(@Param("nome") String nome);
}
