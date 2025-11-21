package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.Pedido;
import com.jaasielsilva.portalceo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteOrderByDataCriacaoDesc(Cliente cliente);

    List<Pedido> findByStatusOrderByDataCriacaoDesc(Pedido.Status status);

    @Query("select p from Pedido p join fetch p.cliente where p.id = :id")
    Pedido findWithCliente(@Param("id") Long id);
}