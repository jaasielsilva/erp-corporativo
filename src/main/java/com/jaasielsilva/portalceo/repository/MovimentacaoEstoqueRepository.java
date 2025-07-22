package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import com.jaasielsilva.portalceo.model.Produto;
import com.jaasielsilva.portalceo.model.TipoMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoOrderByDataHoraDesc(Produto produto);

    List<MovimentacaoEstoque> findByProduto(Produto produto);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE LOWER(m.produto.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<MovimentacaoEstoque> findByProdutoNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE LOWER(m.produto.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND m.tipo = :tipo")
    Page<MovimentacaoEstoque> findByProdutoNomeAndTipo(@Param("nome") String nome, @Param("tipo") TipoMovimentacao tipo, Pageable pageable);
}
