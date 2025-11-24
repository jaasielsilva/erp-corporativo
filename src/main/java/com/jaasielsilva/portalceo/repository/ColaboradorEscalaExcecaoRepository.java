package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.ColaboradorEscalaExcecao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ColaboradorEscalaExcecaoRepository extends JpaRepository<ColaboradorEscalaExcecao, Long> {
    List<ColaboradorEscalaExcecao> findByColaborador_IdAndDataBetweenOrderByDataAsc(Long colaboradorId, LocalDate inicio, LocalDate fim);
    List<ColaboradorEscalaExcecao> findByColaborador_IdAndData(Long colaboradorId, LocalDate data);
}
