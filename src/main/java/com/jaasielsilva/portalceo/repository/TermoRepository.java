package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TermoRepository extends JpaRepository<Termo, Long> {

    // Buscar por status
    List<Termo> findByStatusOrderByDataCriacaoDesc(Termo.StatusTermo status);

    // Buscar por tipo
    List<Termo> findByTipoOrderByDataCriacaoDesc(Termo.TipoTermo tipo);

    // Buscar por tipo e status
    List<Termo> findByTipoAndStatusOrderByDataCriacaoDesc(Termo.TipoTermo tipo, Termo.StatusTermo status);

    // Buscar termos ativos (publicados e dentro da vigência)
    @Query("SELECT t FROM Termo t WHERE t.status = 'PUBLICADO' AND " +
           "(t.dataVigenciaFim IS NULL OR t.dataVigenciaFim > :agora) AND " +
           "(t.dataVigenciaInicio IS NULL OR t.dataVigenciaInicio <= :agora) " +
           "ORDER BY t.dataCriacao DESC")
    List<Termo> findTermosAtivos(@Param("agora") LocalDateTime agora);

    // Buscar termo mais recente por tipo
    Optional<Termo> findFirstByTipoAndStatusOrderByDataCriacaoDesc(Termo.TipoTermo tipo, Termo.StatusTermo status);

    // Buscar por versão
    Optional<Termo> findByVersao(String versao);

    // Buscar por título (busca parcial)
    @Query("SELECT t FROM Termo t WHERE LOWER(t.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY t.dataCriacao DESC")
    List<Termo> findByTituloContainingIgnoreCase(@Param("titulo") String titulo);

    // Buscar por criador
    List<Termo> findByCriadoPorOrderByDataCriacaoDesc(Usuario criadoPor);

    // Contar por status
    long countByStatus(Termo.StatusTermo status);

    // Contar por tipo
    long countByTipo(Termo.TipoTermo tipo);

    // Buscar termos que precisam de aceite obrigatório
    List<Termo> findByObrigatorioAceiteAndStatusOrderByDataCriacaoDesc(boolean obrigatorioAceite, Termo.StatusTermo status);

    // Buscar termos criados em um período
    @Query("SELECT t FROM Termo t WHERE t.dataCriacao BETWEEN :inicio AND :fim ORDER BY t.dataCriacao DESC")
    List<Termo> findByDataCriacaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Buscar termos publicados em um período
    @Query("SELECT t FROM Termo t WHERE t.dataPublicacao BETWEEN :inicio AND :fim ORDER BY t.dataPublicacao DESC")
    List<Termo> findByDataPublicacaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Buscar termos pendentes de aprovação
    @Query("SELECT t FROM Termo t WHERE t.status = 'PENDENTE_APROVACAO' ORDER BY t.dataCriacao ASC")
    List<Termo> findTermosPendentesAprovacao();

    // Buscar termos que expiram em breve
    @Query("SELECT t FROM Termo t WHERE t.status = 'PUBLICADO' AND " +
           "t.dataVigenciaFim IS NOT NULL AND " +
           "t.dataVigenciaFim BETWEEN :agora AND :limite " +
           "ORDER BY t.dataVigenciaFim ASC")
    List<Termo> findTermosExpirandoEm(@Param("agora") LocalDateTime agora, @Param("limite") LocalDateTime limite);

    // Estatísticas - termo com mais aceites
    @Query("SELECT t FROM Termo t WHERE t.totalAceites = (SELECT MAX(t2.totalAceites) FROM Termo t2)")
    Optional<Termo> findTermoComMaisAceites();

    // Buscar últimos termos criados
    @Query("SELECT t FROM Termo t ORDER BY t.dataCriacao DESC")
    List<Termo> findUltimosTermos();
}