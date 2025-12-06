package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findTopByUserIdOrderByLastUpdatedDesc(Long userId);
    Optional<UserProgress> findByUserIdAndFlowId(Long userId, String flowId);
    List<UserProgress> findByLastUpdatedBefore(LocalDateTime cutoff);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndFlowId(Long userId, String flowId);
}
