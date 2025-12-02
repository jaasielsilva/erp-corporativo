package com.jaasielsilva.portalceo.repository.cnpj;

import com.jaasielsilva.portalceo.model.cnpj.CnpjConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CnpjConsultaRepository extends JpaRepository<CnpjConsulta, Long> {
    List<CnpjConsulta> findByCnpjOrderByConsultedAtDesc(String cnpj);
    List<CnpjConsulta> findByCnpjAndConsultedAtBetweenOrderByConsultedAtDesc(String cnpj, LocalDateTime from, LocalDateTime to);
}
