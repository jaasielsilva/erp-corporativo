package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.PlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanoSaudeRepository extends JpaRepository<PlanoSaude, Long> {


    // metodo que Listar todos ativos
    List<PlanoSaude> findByAtivoTrueOrderByNome();

    // metodo de Filtrar por operadora
    List<PlanoSaude> findByOperadoraOrderByNome(String operadora);

    // metodo de Filtrar por tipo de plano
    List<PlanoSaude> findByTipoAndAtivoTrueOrderByValorTitular(PlanoSaude.TipoPlano tipo);

    // metodo Buscar operadoras distintas – útil para filtros
    @Query("SELECT DISTINCT p.operadora FROM PlanoSaude p WHERE p.ativo = true ORDER BY p.operadora")
    List<String> findDistinctOperadoras();

    // metodo Filtrar por faixa de valor – ótimo para pesquisa por preço
    @Query("SELECT p FROM PlanoSaude p WHERE p.ativo = true AND p.valorTitular BETWEEN :valorMin AND :valorMax ORDER BY p.valorTitular")
    List<PlanoSaude> findByFaixaValor(@Param("valorMin") Double valorMin, @Param("valorMax") Double valorMax);

    // metodo Verificar existência pelo código – útil para validações de duplicidade
    boolean existsByCodigo(String codigo);

}