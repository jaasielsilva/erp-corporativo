package com.jaasielsilva.portalceo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import com.jaasielsilva.portalceo.model.Colaborador;

import java.util.List;

@Repository
public interface AdesaoPlanoSaudeRepository extends JpaRepository<AdesaoPlanoSaude, Long> {
    List<AdesaoPlanoSaude> findByStatus(String status);

    List<AdesaoPlanoSaude> findByColaboradorId(Long colaboradorId);

    @Query("""
        select a from AdesaoPlanoSaude a
        join fetch a.colaborador c
        left join fetch c.departamento
        left join fetch a.planoSaude
    """)
    List<AdesaoPlanoSaude> findAllComVinculos();

    @Query("select distinct a.colaborador from AdesaoPlanoSaude a where a.colaborador is not null")
    List<Colaborador> findDistinctColaboradoresComAdesao();
    
}
