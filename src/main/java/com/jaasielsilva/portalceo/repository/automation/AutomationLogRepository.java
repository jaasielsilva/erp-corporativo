package com.jaasielsilva.portalceo.repository.automation;

import com.jaasielsilva.portalceo.model.automation.AutomationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationLogRepository extends JpaRepository<AutomationLog, Long> {
    List<AutomationLog> findByAutomationIdOrderByTimestampDesc(Long automationId);
    List<AutomationLog> findTop10ByAutomationIdOrderByTimestampDesc(Long automationId);
}
