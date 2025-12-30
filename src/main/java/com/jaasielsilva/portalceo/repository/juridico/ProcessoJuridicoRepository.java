package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.ProcessoJuridico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProcessoJuridicoRepository extends JpaRepository<ProcessoJuridico, Long> {
    long countByStatus(ProcessoJuridico.StatusProcesso status);
    java.util.List<ProcessoJuridico> findByStatus(ProcessoJuridico.StatusProcesso status);

    Page<ProcessoJuridico> findByStatus(ProcessoJuridico.StatusProcesso status, Pageable pageable);

    java.util.List<ProcessoJuridico> findByDataAbertura(LocalDate dataAbertura);

    java.util.List<ProcessoJuridico> findByStatusAndDataAbertura(ProcessoJuridico.StatusProcesso status, LocalDate dataAbertura);

    Page<ProcessoJuridico> findByNumeroContainingIgnoreCaseOrParteContainingIgnoreCaseOrAssuntoContainingIgnoreCase(
            String numero, String parte, String assunto, Pageable pageable);

    @Query(
            value = "SELECT p FROM ProcessoJuridico p WHERE (:status IS NULL OR p.status = :status) AND (" +
                    "LOWER(COALESCE(p.numero,'')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(COALESCE(p.parte,'')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(COALESCE(p.assunto,'')) LIKE LOWER(CONCAT('%', :search, '%')) )",
            countQuery = "SELECT COUNT(p) FROM ProcessoJuridico p WHERE (:status IS NULL OR p.status = :status) AND (" +
                    "LOWER(COALESCE(p.numero,'')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(COALESCE(p.parte,'')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(COALESCE(p.assunto,'')) LIKE LOWER(CONCAT('%', :search, '%')) )"
    )
    Page<ProcessoJuridico> searchByStatusAndText(
            @Param("status") ProcessoJuridico.StatusProcesso status,
            @Param("search") String search,
            Pageable pageable);

    Page<ProcessoJuridico> findByClienteId(Long clienteId, Pageable pageable);
}
