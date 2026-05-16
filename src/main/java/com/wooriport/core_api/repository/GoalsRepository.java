package com.wooriport.core_api.repository;

import com.wooriport.core_api.domain.Goals;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface GoalsRepository extends JpaRepository<Goals, UUID> {
    // GoalsRepository.java
    @Query("""
    SELECT g FROM Goals g
    WHERE g.id = :goalId
      AND g.user.id = :userId
      AND g.deletedAt IS NULL
    """)
    Optional<Goals> findByIdAndUserId(
            @Param("goalId") UUID goalId,
            @Param("userId") UUID userId);
}
