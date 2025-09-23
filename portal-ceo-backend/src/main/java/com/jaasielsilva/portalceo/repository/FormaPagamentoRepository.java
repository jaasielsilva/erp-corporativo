package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.FormaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {
    
    // Buscar formas de pagamento ativas
    List<FormaPagamento> findByAtivoTrueOrderByOrdemExibicao();
    
    // Buscar por nome
    Optional<FormaPagamento> findByNomeIgnoreCase(String nome);
    
    // Buscar formas que aceitam parcelas
    @Query("SELECT fp FROM FormaPagamento fp WHERE fp.ativo = true AND fp.aceitaParcelas = true ORDER BY fp.ordemExibicao")
    List<FormaPagamento> findFormasQueAceitamParcelas();
    
    // Contar formas ativas
    long countByAtivoTrue();
    
    // Buscar por status ativo/inativo
    List<FormaPagamento> findByAtivoOrderByOrdemExibicao(Boolean ativo);
}