package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.TermoAceite;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TermoAceiteRepository extends JpaRepository<TermoAceite, Long> {

    // Buscar aceite específico de um usuário para um termo
    Optional<TermoAceite> findByTermoAndUsuario(Termo termo, Usuario usuario);

    // Buscar todos os aceites de um termo
    List<TermoAceite> findByTermoOrderByDataAceiteDesc(Termo termo);

    // Buscar todos os aceites de um usuário
    List<TermoAceite> findByUsuarioOrderByDataAceiteDesc(Usuario usuario);

    // Buscar aceites por status
    List<TermoAceite> findByStatusOrderByDataAceiteDesc(TermoAceite.StatusAceite status);

    // Buscar aceites de um termo por status
    List<TermoAceite> findByTermoAndStatusOrderByDataAceiteDesc(Termo termo, TermoAceite.StatusAceite status);

    // Contar aceites de um termo
    long countByTermo(Termo termo);

    // Contar aceites de um termo por status
    long countByTermoAndStatus(Termo termo, TermoAceite.StatusAceite status);

    // Contar aceites de um usuário
    long countByUsuario(Usuario usuario);

    // Verificar se usuário aceitou um termo específico
    boolean existsByTermoAndUsuarioAndStatus(Termo termo, Usuario usuario, TermoAceite.StatusAceite status);

    // Buscar aceites em um período
    @Query("SELECT ta FROM TermoAceite ta WHERE ta.dataAceite BETWEEN :inicio AND :fim ORDER BY ta.dataAceite DESC")
    List<TermoAceite> findByDataAceiteBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Buscar aceites de hoje
    @Query("SELECT ta FROM TermoAceite ta WHERE DATE(ta.dataAceite) = CURRENT_DATE ORDER BY ta.dataAceite DESC")
    List<TermoAceite> findAceitesHoje();

    // Buscar aceites da semana
    @Query("SELECT ta FROM TermoAceite ta WHERE ta.dataAceite >= :inicioSemana ORDER BY ta.dataAceite DESC")
    List<TermoAceite> findAceitesSemana(@Param("inicioSemana") LocalDateTime inicioSemana);

    // Buscar aceites do mês
    @Query("SELECT ta FROM TermoAceite ta WHERE ta.dataAceite >= :inicioMes ORDER BY ta.dataAceite DESC")
    List<TermoAceite> findAceitesMes(@Param("inicioMes") LocalDateTime inicioMes);

    // Buscar usuários que não aceitaram um termo específico
    @Query("SELECT u FROM Usuario u WHERE u.id NOT IN " +
           "(SELECT ta.usuario.id FROM TermoAceite ta WHERE ta.termo = :termo AND ta.status = 'ACEITO')")
    List<Usuario> findUsuariosSemAceite(@Param("termo") Termo termo);

    // Buscar usuários com aceites pendentes para termos ativos
    @Query("SELECT DISTINCT u FROM Usuario u WHERE u.id NOT IN " +
           "(SELECT ta.usuario.id FROM TermoAceite ta " +
           "WHERE ta.termo.status = 'PUBLICADO' AND ta.status = 'ACEITO' AND " +
           "(ta.termo.dataVigenciaFim IS NULL OR ta.termo.dataVigenciaFim > :agora))")
    List<Usuario> findUsuariosComAceitesPendentes(@Param("agora") LocalDateTime agora);

    // Buscar últimos aceites para relatório
    @Query("SELECT ta FROM TermoAceite ta ORDER BY ta.dataAceite DESC")
    List<TermoAceite> findUltimosAceites();

    // Estatísticas - aceites por período
    @Query("SELECT COUNT(ta) FROM TermoAceite ta WHERE ta.dataAceite BETWEEN :inicio AND :fim")
    long countAceitesPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Buscar aceites com informações completas para relatório
    @Query("SELECT new com.jaasielsilva.portalceo.dto.TermoAceiteDTO(" +
           "ta.usuario.nome, ta.usuario.email, ta.usuario.matricula, " +
           "ta.termo.titulo, ta.termo.versao, ta.dataAceite, ta.status) " +
           "FROM TermoAceite ta ORDER BY ta.dataAceite DESC")
    List<com.jaasielsilva.portalceo.dto.TermoAceiteDTO> findAceitesParaRelatorio();

    // Buscar aceites de um termo para relatório
    @Query("SELECT new com.jaasielsilva.portalceo.dto.TermoAceiteDTO(" +
           "ta.usuario.nome, ta.usuario.email, ta.usuario.matricula, " +
           "ta.termo.titulo, ta.termo.versao, ta.dataAceite, ta.status) " +
           "FROM TermoAceite ta WHERE ta.termo = :termo ORDER BY ta.dataAceite DESC")
    List<com.jaasielsilva.portalceo.dto.TermoAceiteDTO> findAceitesDoTermoParaRelatorio(@Param("termo") Termo termo);
}