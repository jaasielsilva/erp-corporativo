package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.SolicitacaoAcesso;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.StatusSolicitacao;
import com.jaasielsilva.portalceo.model.SolicitacaoAcesso.Prioridade;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoAcessoRepository extends JpaRepository<SolicitacaoAcesso, Long> {
    
    // Buscar por protocolo
    Optional<SolicitacaoAcesso> findByProtocolo(String protocolo);
    
    // Buscar por status
    List<SolicitacaoAcesso> findByStatus(StatusSolicitacao status);
    Page<SolicitacaoAcesso> findByStatus(StatusSolicitacao status, Pageable pageable);
    
    // Buscar por status com JOIN FETCH para evitar N+1
    @Query("SELECT DISTINCT s FROM SolicitacaoAcesso s " +
           "LEFT JOIN FETCH s.colaborador c " +
           "LEFT JOIN FETCH c.cargo " +
           "LEFT JOIN FETCH c.departamento " +
           "LEFT JOIN FETCH s.solicitanteUsuario " +
           "LEFT JOIN FETCH s.aprovadorUsuario " +
           "WHERE s.status = :status " +
           "ORDER BY s.dataSolicitacao DESC")
    List<SolicitacaoAcesso> findByStatusWithFetch(@Param("status") StatusSolicitacao status);
    
    // Buscar solicitações pendentes
    List<SolicitacaoAcesso> findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao status);
    
    // Buscar por solicitante
    List<SolicitacaoAcesso> findBySolicitanteUsuario(Usuario solicitante);
    Page<SolicitacaoAcesso> findBySolicitanteUsuario(Usuario solicitante, Pageable pageable);
    
    // Buscar por colaborador
    List<SolicitacaoAcesso> findByColaborador(Colaborador colaborador);
    Optional<SolicitacaoAcesso> findByColaboradorAndStatusIn(Colaborador colaborador, List<StatusSolicitacao> status);
    
    // Buscar por aprovador
    List<SolicitacaoAcesso> findByAprovadorUsuario(Usuario aprovador);
    Page<SolicitacaoAcesso> findByAprovadorUsuario(Usuario aprovador, Pageable pageable);
    
    // Buscar por prioridade
    List<SolicitacaoAcesso> findByPrioridade(Prioridade prioridade);
    List<SolicitacaoAcesso> findByPrioridadeAndStatus(Prioridade prioridade, StatusSolicitacao status);
    
    // Buscar por período
    List<SolicitacaoAcesso> findByDataSolicitacaoBetween(LocalDateTime inicio, LocalDateTime fim);
    
    // Buscar solicitações com data limite próxima
    List<SolicitacaoAcesso> findByDataLimiteBetweenAndStatus(LocalDate inicio, LocalDate fim, StatusSolicitacao status);
    
    // Buscar solicitações urgentes pendentes
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE s.prioridade = 'URGENTE' AND s.status = 'PENDENTE' ORDER BY s.dataSolicitacao ASC")
    List<SolicitacaoAcesso> findSolicitacoesUrgentesPendentes();
    
    // Buscar solicitações por departamento do solicitante
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE s.solicitanteDepartamento = :departamento")
    List<SolicitacaoAcesso> findBySolicitanteDepartamento(@Param("departamento") String departamento);
    
    // Buscar solicitações por departamento do colaborador
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE s.colaborador.departamento.nome = :departamento")
    List<SolicitacaoAcesso> findByColaboradorDepartamento(@Param("departamento") String departamento);
    
    // Contar solicitações por status
    long countByStatus(StatusSolicitacao status);
    
    // Contar solicitações pendentes por prioridade
    long countByStatusAndPrioridade(StatusSolicitacao status, Prioridade prioridade);
    
    // Buscar solicitações do mês atual
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE YEAR(s.dataSolicitacao) = YEAR(CURRENT_DATE) AND MONTH(s.dataSolicitacao) = MONTH(CURRENT_DATE)")
    List<SolicitacaoAcesso> findSolicitacoesDoMesAtual();
    
    // Buscar solicitações aprovadas sem usuário criado
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE s.status IN ('APROVADO', 'APROVADO_PARCIAL') AND s.usuarioCriado IS NULL")
    List<SolicitacaoAcesso> findAprovadasSemUsuarioCriado();
    
    // Buscar solicitações com data limite vencida
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE s.dataLimite < CURRENT_DATE AND s.status = 'PENDENTE'")
    List<SolicitacaoAcesso> findSolicitacoesComPrazoVencido();
    
    // Buscar últimas atividades (para dashboard)
    @Query("SELECT s FROM SolicitacaoAcesso s ORDER BY s.dataSolicitacao DESC")
    Page<SolicitacaoAcesso> findUltimasAtividades(Pageable pageable);
    
    // Estatísticas por período
    @Query("SELECT COUNT(s) FROM SolicitacaoAcesso s WHERE s.dataSolicitacao BETWEEN :inicio AND :fim")
    long countByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT COUNT(s) FROM SolicitacaoAcesso s WHERE s.dataSolicitacao BETWEEN :inicio AND :fim AND s.status = :status")
    long countByPeriodoAndStatus(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("status") StatusSolicitacao status);
    
    // Buscar por múltiplos status
    List<SolicitacaoAcesso> findByStatusIn(List<StatusSolicitacao> status);
    Page<SolicitacaoAcesso> findByStatusIn(List<StatusSolicitacao> status, Pageable pageable);
    
    // Buscar solicitações que precisam de atenção (urgentes ou com prazo próximo)
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE " +
           "(s.prioridade = 'URGENTE' AND s.status = 'PENDENTE') OR " +
           "(s.dataLimite <= :dataLimite AND s.status = 'PENDENTE') " +
           "ORDER BY s.prioridade DESC, s.dataLimite ASC")
    List<SolicitacaoAcesso> findSolicitacoesQueNecessitamAtencao(@Param("dataLimite") LocalDate dataLimite);
    
    // Buscar solicitações por email do solicitante
    List<SolicitacaoAcesso> findBySolicitanteEmail(String email);
    
    // Verificar se colaborador já tem solicitação pendente
    @Query("SELECT COUNT(s) > 0 FROM SolicitacaoAcesso s WHERE s.colaborador = :colaborador AND s.status IN ('PENDENTE', 'EM_ANALISE')")
    boolean existsSolicitacaoPendenteParaColaborador(@Param("colaborador") Colaborador colaborador);
    
    // Buscar solicitações por texto (busca em múltiplos campos)
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE " +
           "LOWER(s.solicitanteNome) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.colaborador.nome) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.protocolo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.justificativa) LIKE LOWER(CONCAT('%', :texto, '%'))")
    Page<SolicitacaoAcesso> findByTexto(@Param("texto") String texto, Pageable pageable);
    
    // Buscar todas as solicitações com JOIN FETCH para listagem otimizada
    @Query(value = "SELECT DISTINCT s FROM SolicitacaoAcesso s " +
           "LEFT JOIN FETCH s.colaborador c " +
           "LEFT JOIN FETCH c.cargo " +
           "LEFT JOIN FETCH c.departamento " +
           "LEFT JOIN FETCH s.solicitanteUsuario " +
           "LEFT JOIN FETCH s.aprovadorUsuario " +
           "ORDER BY s.dataSolicitacao DESC",
           countQuery = "SELECT COUNT(s) FROM SolicitacaoAcesso s")
    Page<SolicitacaoAcesso> findAllWithFetch(Pageable pageable);
    
    // Buscar por status com JOIN FETCH para paginação
    @Query(value = "SELECT DISTINCT s FROM SolicitacaoAcesso s " +
           "LEFT JOIN FETCH s.colaborador c " +
           "LEFT JOIN FETCH c.cargo " +
           "LEFT JOIN FETCH c.departamento " +
           "LEFT JOIN FETCH s.solicitanteUsuario " +
           "LEFT JOIN FETCH s.aprovadorUsuario " +
           "WHERE s.status = :status " +
           "ORDER BY s.dataSolicitacao DESC",
           countQuery = "SELECT COUNT(s) FROM SolicitacaoAcesso s WHERE s.status = :status")
    Page<SolicitacaoAcesso> findByStatusWithFetch(@Param("status") StatusSolicitacao status, Pageable pageable);
    
    // Buscar por texto com JOIN FETCH para paginação
    @Query(value = "SELECT DISTINCT s FROM SolicitacaoAcesso s " +
           "LEFT JOIN FETCH s.colaborador c " +
           "LEFT JOIN FETCH c.cargo " +
           "LEFT JOIN FETCH c.departamento " +
           "LEFT JOIN FETCH s.solicitanteUsuario " +
           "LEFT JOIN FETCH s.aprovadorUsuario " +
           "WHERE LOWER(s.solicitanteNome) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.colaborador.nome) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.protocolo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.justificativa) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "ORDER BY s.dataSolicitacao DESC",
           countQuery = "SELECT COUNT(s) FROM SolicitacaoAcesso s WHERE " +
           "LOWER(s.solicitanteNome) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.colaborador.nome) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.protocolo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.justificativa) LIKE LOWER(CONCAT('%', :texto, '%'))")
    Page<SolicitacaoAcesso> findByTextoWithFetch(@Param("texto") String texto, Pageable pageable);
    
    // Estatísticas para dashboard
    @Query("SELECT s.status, COUNT(s) FROM SolicitacaoAcesso s GROUP BY s.status")
    List<Object[]> countByStatusGrouped();
    
    @Query("SELECT s.prioridade, COUNT(s) FROM SolicitacaoAcesso s WHERE s.status = 'PENDENTE' GROUP BY s.prioridade")
    List<Object[]> countPendentesByPrioridadeGrouped();
    
    @Query("SELECT s.solicitanteDepartamento, COUNT(s) FROM SolicitacaoAcesso s GROUP BY s.solicitanteDepartamento")
    List<Object[]> countByDepartamentoGrouped();
    
    // Buscar solicitações temporárias que estão expirando
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE " +
           "s.prazoAcesso = 'TEMPORARIO' AND " +
           "s.dataFim BETWEEN :inicio AND :fim AND " +
           "s.status = 'USUARIO_CRIADO'")
    List<SolicitacaoAcesso> findAcessosTemporariosExpirando(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    
    // Buscar histórico de solicitações de um colaborador
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE s.colaborador = :colaborador ORDER BY s.dataSolicitacao DESC")
    List<SolicitacaoAcesso> findHistoricoColaborador(@Param("colaborador") Colaborador colaborador);
    
    // Buscar solicitações que precisam de renovação
    @Query("SELECT s FROM SolicitacaoAcesso s WHERE " +
           "s.prazoAcesso = 'TEMPORARIO' AND " +
           "s.dataFim <= :dataLimite AND " +
           "s.status = 'USUARIO_CRIADO'")
    List<SolicitacaoAcesso> findSolicitacoesParaRenovacao(@Param("dataLimite") LocalDate dataLimite);

    Page<SolicitacaoAcesso> findBySolicitanteUsuarioAndStatus(Usuario usuario, StatusSolicitacao status, Pageable pageable);
       
}