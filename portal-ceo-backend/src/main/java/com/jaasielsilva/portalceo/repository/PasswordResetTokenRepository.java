package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
     Optional<PasswordResetToken> findByToken(String token);
    
    // Aqui está o método que você precisa adicionar:
    Optional<PasswordResetToken> findByUsuarioId(Long usuarioId);
    
    void deleteByUsuarioId(Long usuarioId);
}
