package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.dto.TicketMedioDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketMedioRepository {
    
    @Query(value = "SELECT ROUND(SUM(total) / COUNT(*), 2) FROM venda WHERE status = 'FINALIZADA'", nativeQuery = true)
    Double calcularTicketMedio();
}
