package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ContaContabil;
import com.jaasielsilva.portalceo.model.LancamentoContabilItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LancamentoContabilItemRepository extends JpaRepository<LancamentoContabilItem, Long> {

    @Query("""
            select i.contaContabil.id,
                   coalesce(sum(i.debito), 0),
                   coalesce(sum(i.credito), 0)
              from LancamentoContabilItem i
              join i.lancamento l
             where l.status = 'LANCADO'
               and l.data <= :ate
             group by i.contaContabil.id
            """)
    List<Object[]> sumDebitoCreditoAte(@Param("ate") LocalDate ate);

    @Query("""
            select i.contaContabil.id,
                   coalesce(sum(i.debito), 0),
                   coalesce(sum(i.credito), 0)
              from LancamentoContabilItem i
              join i.lancamento l
             where l.status = 'LANCADO'
               and l.data between :inicio and :fim
             group by i.contaContabil.id
            """)
    List<Object[]> sumDebitoCreditoPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    List<LancamentoContabilItem> findByContaContabil(ContaContabil contaContabil);
}

