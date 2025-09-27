package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Contrato;
import com.jaasielsilva.portalceo.model.TipoContrato;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    List<Contrato> findByFornecedorId(Long fornecedorId);

    List<Contrato> findByTipo(TipoContrato tipo);

}

