package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.TipoMovimentacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoOrderByDataHoraDesc(Produto produto);

    // Busca todas movimentações de um produto específico (não paginado)
    List<MovimentacaoEstoque> findByProduto(Produto produto);

   // Buscar por nome do produto (contendo texto) e paginar
    @Query("SELECT m FROM MovimentacaoEstoque m WHERE LOWER(m.produto.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY m.dataHora DESC")
    Page<MovimentacaoEstoque> findByProdutoNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

    // Buscar por nome do produto e tipo, paginar
    @Query("SELECT m FROM MovimentacaoEstoque m WHERE LOWER(m.produto.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND m.tipo = :tipo ORDER BY m.dataHora DESC")
    Page<MovimentacaoEstoque> findByProdutoNomeAndTipo(@Param("nome") String nome, @Param("tipo") TipoMovimentacao tipo, Pageable pageable);
}
