package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContratoAlerta;
import com.jaasielsilva.portalceo.model.ContratoLegal;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContratoAlertaRepository extends JpaRepository<ContratoAlerta, Long> {

    List<ContratoAlerta> findByContratoOrderByDataAlertaDesc(ContratoLegal contrato);

    List<ContratoAlerta> findByTipoOrderByDataAlertaDesc(ContratoAlerta.TipoAlerta tipo);

    List<ContratoAlerta> findByPrioridadeOrderByDataAlertaDesc(ContratoAlerta.PrioridadeAlerta prioridade);

    List<ContratoAlerta> findByUsuarioResponsavelOrderByDataAlertaDesc(Usuario usuario);

    List<ContratoAlerta> findByDataAlertaBetweenOrderByDataAlerta(LocalDate inicio, LocalDate fim);

    List<ContratoAlerta> findByDataVencimentoBetweenOrderByDataVencimento(LocalDate inicio, LocalDate fim);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.contrato.id = :contratoId ORDER BY a.dataAlerta DESC")
    List<ContratoAlerta> findByContratoId(@Param("contratoId") Long contratoId);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.ativo = true AND a.resolvido = false ORDER BY a.prioridade DESC, a.dataAlerta")
    List<ContratoAlerta> findAlertasAtivos();

    @Query("SELECT a FROM ContratoAlerta a WHERE a.resolvido = false ORDER BY a.prioridade DESC, a.dataAlerta")
    List<ContratoAlerta> findAlertasNaoResolvidos();

    @Query("SELECT a FROM ContratoAlerta a WHERE a.resolvido = true ORDER BY a.dataResolucao DESC")
    List<ContratoAlerta> findAlertasResolvidos();

    @Query("SELECT a FROM ContratoAlerta a WHERE a.dataVencimento < :hoje AND a.resolvido = false ORDER BY a.dataVencimento")
    List<ContratoAlerta> findAlertasVencidos(@Param("hoje") LocalDate hoje);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.dataVencimento BETWEEN :hoje AND :dataLimite AND a.resolvido = false ORDER BY a.dataVencimento")
    List<ContratoAlerta> findAlertasVencendoEm(@Param("hoje") LocalDate hoje, @Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.prioridade = 'CRITICA' AND a.ativo = true AND a.resolvido = false ORDER BY a.dataAlerta")
    List<ContratoAlerta> findAlertasCriticos();

    @Query("SELECT a FROM ContratoAlerta a WHERE a.prioridade IN ('ALTA', 'CRITICA') AND a.ativo = true AND a.resolvido = false ORDER BY a.prioridade DESC, a.dataAlerta")
    List<ContratoAlerta> findAlertasUrgentes();

    @Query("SELECT COUNT(a) FROM ContratoAlerta a WHERE a.contrato = :contrato AND a.resolvido = false")
    Long countAlertasAtivosByContrato(@Param("contrato") ContratoLegal contrato);

    @Query("SELECT COUNT(a) FROM ContratoAlerta a WHERE a.tipo = :tipo AND a.resolvido = false")
    Long countByTipoAndNaoResolvido(@Param("tipo") ContratoAlerta.TipoAlerta tipo);

    @Query("SELECT COUNT(a) FROM ContratoAlerta a WHERE a.prioridade = :prioridade AND a.resolvido = false")
    Long countByPrioridadeAndNaoResolvido(@Param("prioridade") ContratoAlerta.PrioridadeAlerta prioridade);

    @Query("SELECT COUNT(a) FROM ContratoAlerta a WHERE a.usuarioResponsavel = :usuario AND a.resolvido = false")
    Long countAlertasAtivosByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT a.tipo, COUNT(a) FROM ContratoAlerta a WHERE a.resolvido = false GROUP BY a.tipo ORDER BY COUNT(a) DESC")
    List<Object[]> countAlertasAtivosByTipo();

    @Query("SELECT a.prioridade, COUNT(a) FROM ContratoAlerta a WHERE a.resolvido = false GROUP BY a.prioridade ORDER BY COUNT(a) DESC")
    List<Object[]> countAlertasAtivosByPrioridade();

    @Query("SELECT YEAR(a.dataCriacao), MONTH(a.dataCriacao), COUNT(a) FROM ContratoAlerta a WHERE a.dataCriacao BETWEEN :inicio AND :fim GROUP BY YEAR(a.dataCriacao), MONTH(a.dataCriacao) ORDER BY YEAR(a.dataCriacao), MONTH(a.dataCriacao)")
    List<Object[]> getAlertasCriadosPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT YEAR(a.dataResolucao), MONTH(a.dataResolucao), COUNT(a) FROM ContratoAlerta a WHERE a.dataResolucao BETWEEN :inicio AND :fim GROUP BY YEAR(a.dataResolucao), MONTH(a.dataResolucao) ORDER BY YEAR(a.dataResolucao), MONTH(a.dataResolucao)")
    List<Object[]> getAlertasResolvidosPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT a.usuarioResponsavel, COUNT(a) FROM ContratoAlerta a WHERE a.usuarioResponsavel IS NOT NULL AND a.resolvido = false GROUP BY a.usuarioResponsavel ORDER BY COUNT(a) DESC")
    List<Object[]> getAlertasAtivosPorUsuario();

    @Query("SELECT a.usuarioResolucao, COUNT(a) FROM ContratoAlerta a WHERE a.usuarioResolucao IS NOT NULL AND a.dataResolucao BETWEEN :inicio AND :fim GROUP BY a.usuarioResolucao ORDER BY COUNT(a) DESC")
    List<Object[]> getAlertasResolvidosPorUsuario(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT a.contrato.id, COUNT(a) FROM ContratoAlerta a WHERE a.resolvido = false GROUP BY a.contrato.id HAVING COUNT(a) > :quantidadeMinima ORDER BY COUNT(a) DESC")
    List<Object[]> findContratosComMaisAlertas(@Param("quantidadeMinima") Long quantidadeMinima);

    @Query("SELECT AVG(DATEDIFF(a.dataResolucao, a.dataCriacao)) FROM ContratoAlerta a WHERE a.dataResolucao IS NOT NULL AND a.dataCriacao BETWEEN :inicio AND :fim")
    Double getTempoMedioResolucao(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT a.tipo, AVG(DATEDIFF(a.dataResolucao, a.dataCriacao)) FROM ContratoAlerta a WHERE a.dataResolucao IS NOT NULL GROUP BY a.tipo ORDER BY AVG(DATEDIFF(a.dataResolucao, a.dataCriacao)) DESC")
    List<Object[]> getTempoMedioResolucaoPorTipo();

    @Query("SELECT a FROM ContratoAlerta a WHERE a.titulo LIKE %:titulo% ORDER BY a.dataCriacao DESC")
    List<ContratoAlerta> findByTituloContaining(@Param("titulo") String titulo);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.descricao LIKE %:descricao% ORDER BY a.dataCriacao DESC")
    List<ContratoAlerta> findByDescricaoContaining(@Param("descricao") String descricao);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.tipo = :tipo AND a.prioridade = :prioridade AND a.resolvido = false ORDER BY a.dataAlerta")
    List<ContratoAlerta> findByTipoAndPrioridadeAndNaoResolvido(@Param("tipo") ContratoAlerta.TipoAlerta tipo, @Param("prioridade") ContratoAlerta.PrioridadeAlerta prioridade);

    // Dashboard Analytics
    @Query("SELECT " +
           "COUNT(a) as totalAlertas, " +
           "COUNT(CASE WHEN a.resolvido = false THEN 1 END) as alertasAtivos, " +
           "COUNT(CASE WHEN a.resolvido = true THEN 1 END) as alertasResolvidos, " +
           "COUNT(CASE WHEN a.prioridade = 'CRITICA' AND a.resolvido = false THEN 1 END) as alertasCriticos, " +
           "COUNT(CASE WHEN a.dataVencimento < CURRENT_DATE AND a.resolvido = false THEN 1 END) as alertasVencidos " +
           "FROM ContratoAlerta a " +
           "WHERE a.dataCriacao BETWEEN :inicio AND :fim")
    List<Object[]> getDashboardAlertas(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT a.tipo, " +
           "COUNT(a) as total, " +
           "COUNT(CASE WHEN a.resolvido = false THEN 1 END) as ativos, " +
           "COUNT(CASE WHEN a.resolvido = true THEN 1 END) as resolvidos, " +
           "AVG(CASE WHEN a.dataResolucao IS NOT NULL THEN DATEDIFF(a.dataResolucao, a.dataCriacao) ELSE NULL END) as tempoMedioResolucao " +
           "FROM ContratoAlerta a " +
           "WHERE a.dataCriacao BETWEEN :inicio AND :fim " +
           "GROUP BY a.tipo " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getEstatisticasPorTipo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // Compliance monitoring
    @Query("SELECT a FROM ContratoAlerta a WHERE a.tipo = 'COMPLIANCE' AND a.resolvido = false ORDER BY a.prioridade DESC, a.dataAlerta")
    List<ContratoAlerta> findAlertasCompliance();

    @Query("SELECT a FROM ContratoAlerta a WHERE a.tipo IN ('VENCIMENTO_CONTRATO', 'RENOVACAO_CONTRATO') AND a.dataVencimento BETWEEN :hoje AND :dataLimite ORDER BY a.dataVencimento")
    List<ContratoAlerta> findAlertasVencimentoRenovacao(@Param("hoje") LocalDate hoje, @Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT a FROM ContratoAlerta a WHERE a.tipo = 'PAGAMENTO_PENDENTE' AND a.resolvido = false ORDER BY a.dataVencimento")
    List<ContratoAlerta> findAlertasPagamentoPendente();

    boolean existsByContratoAndTipoAndResolvido(ContratoLegal contrato, ContratoAlerta.TipoAlerta tipo, Boolean resolvido);
}