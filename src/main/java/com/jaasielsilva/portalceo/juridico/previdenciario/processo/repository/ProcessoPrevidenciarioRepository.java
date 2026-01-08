package com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository;

import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciarioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessoPrevidenciarioRepository extends JpaRepository<ProcessoPrevidenciario, Long> {
    @EntityGraph(attributePaths = { "cliente", "responsavel" })
    List<ProcessoPrevidenciario> findAllByOrderByDataAberturaDesc();

    @EntityGraph(attributePaths = { "cliente", "responsavel" })
    List<ProcessoPrevidenciario> findByCliente_IdOrderByDataAberturaDesc(Long clienteId);

    @EntityGraph(attributePaths = { "cliente", "responsavel" })
    @Query("SELECT p FROM ProcessoPrevidenciario p WHERE " +
           "(:status IS NULL OR p.statusAtual = :status) AND " +
           "(:search IS NULL OR LOWER(p.cliente.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.numeroProtocolo) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProcessoPrevidenciario> buscarComFiltros(@Param("status") ProcessoPrevidenciarioStatus status, 
                                                  @Param("search") String search, 
                                                  Pageable pageable);
}
