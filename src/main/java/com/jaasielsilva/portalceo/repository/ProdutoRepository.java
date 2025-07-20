package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Produto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    Produto findByEan(String ean);

    @Query("SELECT COALESCE(SUM(p.estoque), 0) FROM Produto p WHERE p.ativo = true")
    long somarQuantidadeEstoque();

     // Filtros combinados para paginação
    @Query("SELECT p FROM Produto p " +
           "WHERE (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:ean IS NULL OR p.ean = :ean) " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
           "AND (:fornecedorId IS NULL OR p.fornecedor.id = :fornecedorId) " +
           "AND p.ativo = true")
    Page<Produto> buscarComFiltros(
            @Param("nome") String nome,
            @Param("ean") String ean,
            @Param("categoriaId") Long categoriaId,
            @Param("fornecedorId") Long fornecedorId,
            Pageable pageable);

    // Soma total estoque (somente produtos ativos)
    @Query("SELECT COALESCE(SUM(p.estoque),0) FROM Produto p WHERE p.ativo = true")
    Integer somaEstoqueTotal();

    // Contagem estoque crítico (estoque menor ou igual ao minimoEstoque)
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.estoque <= p.minimoEstoque")
    long countEstoqueCritico();

    // Contagem produtos com estoque zerado
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.estoque = 0")
    long countEstoqueZerado();

    // Contagem produtos ativos
    long countByAtivoTrue();

    // Dados para gráfico: contagem por categoria (nome e quantidade)
    @Query("SELECT p.categoria.nome, COUNT(p) FROM Produto p WHERE p.ativo = true GROUP BY p.categoria.nome")
    List<Object[]> countProdutosPorCategoria();

    @Query("SELECT p.categoria.nome AS categoriaNome, COUNT(p) AS total FROM Produto p GROUP BY p.categoria.nome")
    List<Object[]> contarProdutosAgrupadosPorCategoria();

}