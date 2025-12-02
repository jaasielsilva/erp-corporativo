package com.jaasielsilva.portalceo.repository.cnpj;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CnpjFonteRepository {

    @PersistenceContext
    private EntityManager em;

    public List<String> buscarLote(int limit, int offset) {
        return em.createQuery(
                        "SELECT c.cpfCnpj FROM Cliente c WHERE LOWER(c.tipoCliente) = 'pj' AND c.cpfCnpj IS NOT NULL ORDER BY c.id",
                        String.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
