package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.StatusContrato;
import com.jaasielsilva.portalceo.model.TipoContrato;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    List<Contrato> findByFornecedorId(Long fornecedorId);

    List<Contrato> findByTipo(TipoContrato tipo);

    long countByClienteIsNotNull();

    long countByClienteIsNotNullAndStatus(StatusContrato status);

    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.cliente IS NOT NULL AND c.dataFim BETWEEN :inicio AND :fim")
    long countContratosClientesVencendoEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(c.valor), 0) FROM Contrato c WHERE c.cliente IS NOT NULL AND c.status = :status")
    BigDecimal somarValorContratosClientes(@Param("status") StatusContrato status);

    @Query("SELECT c FROM Contrato c " +
            "WHERE c.cliente IS NOT NULL " +
            "  AND (:clienteId IS NULL OR c.cliente.id = :clienteId) " +
            "  AND (:tipo IS NULL OR c.tipo = :tipo) " +
            "  AND (:status IS NULL OR c.status = :status) " +
            "  AND ( " +
            "        :busca IS NULL " +
            "        OR LOWER(c.numeroContrato) LIKE LOWER(CONCAT('%', :busca, '%')) " +
            "        OR LOWER(COALESCE(c.cliente.nome, '')) LIKE LOWER(CONCAT('%', :busca, '%')) " +
            "        OR LOWER(COALESCE(c.cliente.nomeFantasia, '')) LIKE LOWER(CONCAT('%', :busca, '%')) " +
            "      )")
    Page<Contrato> buscarContratosClientes(@Param("busca") String busca,
            @Param("clienteId") Long clienteId,
            @Param("tipo") TipoContrato tipo,
            @Param("status") StatusContrato status,
            Pageable pageable);
}

