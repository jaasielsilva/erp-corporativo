package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Fornecedor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findByAtivoTrue();

    @Query("SELECT new Fornecedor(f.id, f.razaoSocial) FROM Fornecedor f WHERE f.ativo = true ORDER BY f.razaoSocial")
    List<Fornecedor> findBasicInfoForSelection();

    @Query("SELECT f FROM Fornecedor f WHERE " +
           "(:busca IS NULL OR LOWER(f.razaoSocial) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(f.nomeFantasia) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(f.email) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(f.cnpj) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(f.telefone) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(f.celular) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
           " AND (:status IS NULL OR LOWER(f.status) = LOWER(:status))")
    Page<Fornecedor> buscarComFiltros(@Param("busca") String busca,
                                      @Param("status") String status,
                                      Pageable pageable);
}
