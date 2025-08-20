package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Caixa;
import com.jaasielsilva.portalceo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {
    
    // Buscar caixa aberto
    Optional<Caixa> findByStatusAndUsuarioAbertura(String status, Usuario usuario);
    
    // Buscar último caixa aberto (qualquer usuário)
    Optional<Caixa> findFirstByStatusOrderByDataAberturaDesc(String status);
    
    // Buscar caixas por período
    @Query("SELECT c FROM Caixa c WHERE c.dataAbertura BETWEEN :inicio AND :fim ORDER BY c.dataAbertura DESC")
    List<Caixa> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    // Buscar caixas fechados por período
    @Query("SELECT c FROM Caixa c WHERE c.status = 'FECHADO' AND c.dataFechamento BETWEEN :inicio AND :fim ORDER BY c.dataFechamento DESC")
    List<Caixa> findCaixasFechadosPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    // Buscar caixas por usuário
    List<Caixa> findByUsuarioAberturaOrderByDataAberturaDesc(Usuario usuario);
    
    // Verificar se existe caixa aberto
    boolean existsByStatus(String status);
    
    // Buscar últimos caixas (para relatórios)
    @Query("SELECT c FROM Caixa c ORDER BY c.dataAbertura DESC")
    List<Caixa> findUltimosCaixas();
    
    // Somar total de vendas por período
    @Query("SELECT COALESCE(SUM(c.totalVendas), 0) FROM Caixa c WHERE c.dataAbertura BETWEEN :inicio AND :fim")
    Double somarVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}