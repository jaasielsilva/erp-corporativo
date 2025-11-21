package com.jaasielsilva.portalceo.repository.juridico;

import com.jaasielsilva.portalceo.model.juridico.DocumentoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentoJuridicoRepository extends JpaRepository<DocumentoJuridico, Long> {
    java.util.List<DocumentoJuridico> findTop10ByOrderByCriadoEmDesc();

    @Query("select coalesce(d.categoria, '—') as categoria, count(d) as total from DocumentoJuridico d group by d.categoria")
    java.util.List<Object[]> contagemPorCategoria();
}