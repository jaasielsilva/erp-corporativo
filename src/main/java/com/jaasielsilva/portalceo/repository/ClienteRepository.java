package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Cliente;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Conta a quantidade de clientes pelo status (ignorando maiúsculas/minúsculas).
     * Exemplo de status: "Ativo", "Inativo", "Pendente", etc.
     */
    long countByStatusIgnoreCase(String status);

    /**
     * Conta a quantidade de clientes pelo tipo (ignorando maiúsculas/minúsculas).
     * Exemplo de tipoCliente: "PF" (Pessoa Física), "PJ" (Pessoa Jurídica).
     */
    long countByTipoClienteIgnoreCase(String tipoCliente);

    /**
     * Conta clientes inativos há mais de 90 dias considerando 'dataCadastro' como referência.
     * @param dataLimite data limite para calcular 90 dias atrás.
     */
    @Query("select count(c) from Cliente c where c.status = 'Inativo' and c.dataCadastro <= :dataLimite")
    long countInativosMais90Dias(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Conta clientes ativos cadastrados nos últimos 30 dias.
     * @param dataLimite data limite para calcular últimos 30 dias.
     */
    @Query("select count(c) from Cliente c where c.status = 'Ativo' and c.dataCadastro >= :dataLimite")
    long countAtivosUltimos30Dias(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Conta clientes com status 'Pendente'.
     */
    @Query("select count(c) from Cliente c where c.status = 'Pendente'")
    long countPendentes();

    /**
     * Conta clientes com status 'Fidelizado'.
     */
    @Query("select count(c) from Cliente c where c.status = 'Fidelizado'")
    long countFidelizados();

    /**
     * Busca clientes cujo nome ou email contenha o termo de busca (ignorando maiúsculas/minúsculas).
     * Útil para busca dinâmica por nome/email.
     */
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :busca, '%'))")
    List<Cliente> findByNomeOrEmailContainingIgnoreCase(@Param("busca") String busca);

    /**
     * Busca clientes por status ignorando maiúsculas/minúsculas.
     */
    List<Cliente> findByStatusIgnoreCase(String status);

    /**
     * Busca clientes pelo status e cujo nome ou email contenha o termo dado, ignorando maiúsculas/minúsculas.
     * Útil para filtrar clientes ativos (ou outro status) com busca dinâmica.
     */
    List<Cliente> findByStatusIgnoreCaseAndNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String status, String nome, String email);

    List<Cliente> findByAtivoTrue();

    List<Cliente> findByStatus(String status);

    List<Cliente> findByAtivoTrueAndTipoCliente(String tipoCliente);

    @Query("SELECT new Cliente(c.id, c.nome, c.nomeFantasia) FROM Cliente c WHERE c.ativo = true AND c.tipoCliente = :tipoCliente ORDER BY c.nome")
    List<Cliente> findBasicInfoForSelectionByTipo(@Param("tipoCliente") String tipoCliente);

    List<Cliente> findByTipoClienteIgnoreCaseAndCpfCnpjIsNotNull(String tipoCliente);

    // Clientes VIP (exemplo: com campo boolean vip = true)
    @Query("SELECT c FROM Cliente c WHERE c.vip = true")
    List<Cliente> findClientesVIP();

    // Clientes cadastrados depois de uma data (novos)
    @Query("SELECT c FROM Cliente c WHERE c.dataCadastro >= :data")
    List<Cliente> findClientesNovos(@Param("data") LocalDate data);

    // Clientes inativos (exemplo: último acesso antes de uma data)
    @Query("SELECT c FROM Cliente c WHERE c.ultimoAcesso < :data")
    List<Cliente> findClientesInativos(@Param("data") LocalDate data);
    
    // Buscar cliente por nome exato
    Optional<Cliente> findByNome(String nome);

    // Paginação com filtros por termo (nome/email/telefone/celular/cpfCnpj) e status opcional
    @Query("SELECT c FROM Cliente c WHERE " +
           "(:busca IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.email) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.telefone) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.celular) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.cpfCnpj) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
           " AND (:status IS NULL OR LOWER(c.status) = LOWER(:status))" +
           " AND (:origem IS NULL OR LOWER(c.origem) = LOWER(:origem))")
    Page<Cliente> buscarComFiltros(@Param("busca") String busca,
                                   @Param("status") String status,
                                   @Param("origem") String origem,
                                   Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:busca IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.email) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.telefone) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.celular) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           " OR LOWER(c.cpfCnpj) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
           " AND (:status IS NULL OR LOWER(c.status) = LOWER(:status))" +
           " AND (:tipoCliente IS NULL OR LOWER(c.tipoCliente) = LOWER(:tipoCliente))" +
           " AND (:vip IS NULL OR c.vip = :vip)" +
           " AND (:ativo IS NULL OR c.ativo = :ativo)" +
           " AND (:origem IS NULL OR LOWER(c.origem) = LOWER(:origem))")
    Page<Cliente> buscarAvancado(@Param("busca") String busca,
                                 @Param("status") String status,
                                 @Param("tipoCliente") String tipoCliente,
                                 @Param("vip") Boolean vip,
                                 @Param("ativo") Boolean ativo,
                                 @Param("origem") String origem,
                                 Pageable pageable);

    @Query("SELECT c FROM Cliente c JOIN Pedido p ON p.cliente = c " +
           "WHERE p.dataCriacao = (SELECT MAX(p2.dataCriacao) FROM Pedido p2 WHERE p2.cliente = c) " +
           "AND p.dataCriacao <= :data")
    List<Cliente> findByUltimaCompraAntesDe(@Param("data") LocalDateTime data);

}
