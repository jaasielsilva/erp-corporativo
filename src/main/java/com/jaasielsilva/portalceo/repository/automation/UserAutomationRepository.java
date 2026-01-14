package com.jaasielsilva.portalceo.repository.automation;

import com.jaasielsilva.portalceo.model.automation.AutomationEventType;
import com.jaasielsilva.portalceo.model.automation.UserAutomation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAutomationRepository extends JpaRepository<UserAutomation, Long> {
    List<UserAutomation> findByUsuarioId(Long usuarioId);
    List<UserAutomation> findByEventTypeAndActiveTrue(AutomationEventType eventType);
}
