package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.BacklogChamado;
import com.jaasielsilva.portalceo.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento do backlog de chamados
 * Fornece queries otimizadas para priorização e gestão da fila
 */
@Repository
public interface BacklogChamadoRepository extends JpaRepository<BacklogChamado, Long> {

    // Buscar item do backlog por chamado
    Optional<BacklogChamado> findByChamado(Chamado chamado);
    
    Optional<BacklogChamado> findByChamadoId(Long chamadoId);

    // Listar backlog ordenado por prioridade (score decrescente)
    @Query("SELECT b FROM BacklogChamado b " +
           "ORDER BY b.scorePrioridade DESC, b.dataEntradaBacklog ASC")
    List<BacklogChamado> findAllOrderedByPriority();

    // Listar próximos N chamados da fila
    @Query("SELECT b FROM BacklogChamado b " +
           "ORDER BY b.scorePrioridade DESC, b.dataEntradaBacklog ASC " +
           "LIMIT :limite")
    List<BacklogChamado> findProximosChamados(@Param("limite") int limite);

    // Buscar chamados por categoria de urgência
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.categoriaUrgencia = :categoria " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findByCategoriaUrgencia(@Param("categoria") BacklogChamado.CategoriaUrgencia categoria);

    // Buscar chamados com SLA crítico
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.slaCritico = true " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findChamadosSlaCritico();

    // Buscar chamados de clientes VIP
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.clienteVip = true " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findChamadosClienteVip();

    // Buscar chamados por complexidade
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.complexidadeEstimada = :complexidade " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findByComplexidade(@Param("complexidade") BacklogChamado.ComplexidadeEstimada complexidade);

    // Buscar chamados por impacto no negócio
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.impactoNegocio = :impacto " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findByImpactoNegocio(@Param("impacto") BacklogChamado.ImpactoNegocio impacto);

    // Buscar chamados aguardando há mais de X horas
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.dataEntradaBacklog < :dataLimite " +
           "ORDER BY b.dataEntradaBacklog ASC")
    List<BacklogChamado> findChamadosAguardandoMaisTempo(@Param("dataLimite") LocalDateTime dataLimite);

    // Contar total de chamados no backlog
    @Query("SELECT COUNT(b) FROM BacklogChamado b")
    Long countTotalBacklog();

    // Contar chamados por categoria de urgência
    @Query("SELECT b.categoriaUrgencia, COUNT(b) FROM BacklogChamado b " +
           "GROUP BY b.categoriaUrgencia")
    List<Object[]> countByCategoriaUrgencia();

    // Contar chamados por complexidade
    @Query("SELECT b.complexidadeEstimada, COUNT(b) FROM BacklogChamado b " +
           "GROUP BY b.complexidadeEstimada")
    List<Object[]> countByComplexidade();

    // Calcular tempo médio de espera
    @Query("SELECT AVG(b.tempoEsperaMinutos) FROM BacklogChamado b " +
           "WHERE b.tempoEsperaMinutos IS NOT NULL")
    Double calcularTempoMedioEspera();

    // Buscar chamados com score acima de um valor
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.scorePrioridade >= :scoreMinimo " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findByScoreAcimaDe(@Param("scoreMinimo") Double scoreMinimo);

    // Buscar chamados por técnico sugerido
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.tecnicoSugerido = :tecnico " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findByTecnicoSugerido(@Param("tecnico") String tecnico);

    // Buscar estatísticas do backlog
    @Query("SELECT " +
           "COUNT(b) as total, " +
           "AVG(b.scorePrioridade) as scoremedio, " +
           "MAX(b.scorePrioridade) as scoreMaximo, " +
           "MIN(b.scorePrioridade) as scoreMinimo, " +
           "AVG(b.tempoEsperaMinutos) as tempoMedioEspera " +
           "FROM BacklogChamado b")
    Object[] getEstatisticasBacklog();

    // Buscar chamados que precisam de reavaliação (mais de 2 horas sem atualização)
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.dataEntradaBacklog < :dataReavaliacao " +
           "AND b.scorePrioridade < 50 " +
           "ORDER BY b.dataEntradaBacklog ASC")
    List<BacklogChamado> findChamadosParaReavaliacao(@Param("dataReavaliacao") LocalDateTime dataReavaliacao);

    // Buscar próximo chamado para atendimento (maior score)
    @Query("SELECT b FROM BacklogChamado b " +
           "ORDER BY b.scorePrioridade DESC, b.dataEntradaBacklog ASC " +
           "LIMIT 1")
    Optional<BacklogChamado> findProximoChamadoParaAtendimento();

    // Buscar chamados por faixa de score
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.scorePrioridade BETWEEN :scoreMin AND :scoreMax " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findByFaixaScore(@Param("scoreMin") Double scoreMin, 
                                          @Param("scoreMax") Double scoreMax);

    // Deletar item do backlog por chamado
    void deleteByChamado(Chamado chamado);
    
    void deleteByChamadoId(Long chamadoId);

    // Verificar se chamado existe no backlog
    boolean existsByChamado(Chamado chamado);
    
    boolean existsByChamadoId(Long chamadoId);

    // Buscar chamados com estimativa de atendimento vencida
    @Query("SELECT b FROM BacklogChamado b " +
           "WHERE b.estimativaAtendimento IS NOT NULL " +
           "AND b.estimativaAtendimento < :agora " +
           "ORDER BY b.scorePrioridade DESC")
    List<BacklogChamado> findChamadosComEstimativaVencida(@Param("agora") LocalDateTime agora);
}