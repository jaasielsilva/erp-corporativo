package com.jaasielsilva.portalceo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jaasielsilva.portalceo.model.AdesaoPlanoSaude;
import java.util.List;

@Repository
public interface AdesaoPlanoSaudeRepository extends JpaRepository<AdesaoPlanoSaude, Long> {
    List<AdesaoPlanoSaude> findByColaboradorId(Long colaboradorId);
}
