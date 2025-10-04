package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.CorrecaoPonto;
import com.jaasielsilva.portalceo.model.CorrecaoPonto.StatusCorrecao;
import com.jaasielsilva.portalceo.model.CorrecaoPonto.TipoCorrecao;
import com.jaasielsilva.portalceo.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CorrecaoPontoRepository extends JpaRepository<CorrecaoPonto, Long>, JpaSpecificationExecutor<CorrecaoPonto> {

    long countByStatus(StatusCorrecao status);

    List<CorrecaoPonto> findByStatusAndDataAprovacaoIsNotNull(StatusCorrecao status);

}