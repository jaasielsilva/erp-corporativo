package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.PlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanoSaudeRepository extends JpaRepository<PlanoSaude, Long> {

    Optional<PlanoSaude> findByNome(String nome);

    List<PlanoSaude> findByAtivoTrueOrderByNome();

    List<PlanoSaude> findByOperadoraOrderByNome(String operadora);

    List<PlanoSaude> findByTipoAndAtivoTrueOrderByValorTitular(PlanoSaude.TipoPlano tipo);

    @Query("SELECT DISTINCT p.operadora FROM PlanoSaude p WHERE p.ativo = true ORDER BY p.operadora")
    List<String> findDistinctOperadoras();

    @Query("SELECT p FROM PlanoSaude p WHERE p.ativo = true AND p.valorTitular BETWEEN :valorMin AND :valorMax ORDER BY p.valorTitular")
    List<PlanoSaude> findByFaixaValor(@Param("valorMin") Double valorMin, @Param("valorMax") Double valorMax);

    boolean existsByCodigo(String codigo);
}
