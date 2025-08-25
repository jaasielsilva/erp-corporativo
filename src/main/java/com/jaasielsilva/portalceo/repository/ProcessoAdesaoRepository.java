package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ProcessoAdesao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessoAdesaoRepository extends JpaRepository<ProcessoAdesao, Long> {

    // Busca processo por sessionId
    Optional<ProcessoAdesao> findBySessionId(String sessionId);

    // Verifica se existe processo ativo para um CPF
    @Query("SELECT COUNT(p) > 0 FROM ProcessoAdesao p WHERE p.cpfColaborador = :cpf AND p.status IN ('INICIADO', 'EM_ANDAMENTO', 'AGUARDANDO_APROVACAO')")
    boolean existeProcessoAtivoPorCpf(@Param("cpf") String cpf);

    // Busca processos por status
    List<ProcessoAdesao> findByStatusOrderByDataCriacaoDesc(ProcessoAdesao.StatusProcesso status);

    // Busca processos por múltiplos status
    List<ProcessoAdesao> findByStatusInOrderByDataCriacaoDesc(List<ProcessoAdesao.StatusProcesso> status);

    // Busca processos aguardando aprovação
    @Query("SELECT p FROM ProcessoAdesao p WHERE p.status = 'AGUARDANDO_APROVACAO' ORDER BY p.dataFinalizacao ASC")
    List<ProcessoAdesao> findProcessosAguardandoAprovacao();

    // Busca processos por período de criação
    @Query("SELECT p FROM ProcessoAdesao p WHERE p.dataCriacao BETWEEN :dataInicio AND :dataFim ORDER BY p.dataCriacao DESC")
    List<ProcessoAdesao> findByPeriodoCriacao(@Param("dataInicio") LocalDateTime dataInicio,
                                              @Param("dataFim") LocalDateTime dataFim);

    // Busca processos por nome do colaborador
    @Query("SELECT p FROM ProcessoAdesao p WHERE LOWER(p.nomeColaborador) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY p.dataCriacao DESC")
    List<ProcessoAdesao> findByNomeColaboradorContaining(@Param("nome") String nome);

    // Busca processos por CPF
    List<ProcessoAdesao> findByCpfColaboradorOrderByDataCriacaoDesc(String cpfColaborador);

    // Busca processos por email
    List<ProcessoAdesao> findByEmailColaboradorOrderByDataCriacaoDesc(String emailColaborador);

    // Busca processos por cargo
    List<ProcessoAdesao> findByCargoOrderByDataCriacaoDesc(String cargo);

    // Busca processos expirados (mais de X dias sem atualização)
    @Query("SELECT p FROM ProcessoAdesao p WHERE p.status IN ('INICIADO', 'EM_ANDAMENTO') AND p.dataAtualizacao < :dataLimite")
    List<ProcessoAdesao> findProcessosExpirados(@Param("dataLimite") LocalDateTime dataLimite);

    // Conta processos por status
    @Query("SELECT COUNT(p) FROM ProcessoAdesao p WHERE p.status = :status")
    long countByStatus(@Param("status") ProcessoAdesao.StatusProcesso status);

    // Busca estatísticas de processos por período
    @Query("SELECT p.status, COUNT(p) FROM ProcessoAdesao p WHERE p.dataCriacao BETWEEN :dataInicio AND :dataFim GROUP BY p.status")
    List<Object[]> getEstatisticasPorPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                             @Param("dataFim") LocalDateTime dataFim);

    // Busca processos com custo total acima de um valor
    @Query("SELECT p FROM ProcessoAdesao p WHERE p.custoTotalMensal > :valorMinimo ORDER BY p.custoTotalMensal DESC")
    List<ProcessoAdesao> findByCustoTotalMensalGreaterThan(@Param("valorMinimo") Double valorMinimo);

    // Busca últimos processos criados com paginação
    @Query("SELECT p FROM ProcessoAdesao p ORDER BY p.dataCriacao DESC")
    List<ProcessoAdesao> findUltimosProcessos(Pageable pageable);

    // Busca processos por etapa atual
    List<ProcessoAdesao> findByEtapaAtualOrderByDataAtualizacaoDesc(String etapaAtual);

    // Remove processos cancelados antigos
    @Query("DELETE FROM ProcessoAdesao p WHERE p.status = 'CANCELADO' AND p.dataAtualizacao < :dataLimite")
    void removeProcessosCanceladosAntigos(@Param("dataLimite") LocalDateTime dataLimite);

    // Busca por status com paginação
    Page<ProcessoAdesao> findByStatus(ProcessoAdesao.StatusProcesso status, Pageable pageable);

    // Contar processos por status
    @Query("SELECT p.status, COUNT(p) FROM ProcessoAdesao p GROUP BY p.status")
    List<Object[]> contarProcessosPorStatus();

    // Busca processos criados hoje
    @Query("SELECT p FROM ProcessoAdesao p WHERE p.dataCriacao >= :inicio AND p.dataCriacao <= :fim")
    List<ProcessoAdesao> findProcessosHoje(@Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);
}
