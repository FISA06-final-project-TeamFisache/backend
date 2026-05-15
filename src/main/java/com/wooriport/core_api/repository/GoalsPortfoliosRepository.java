package com.wooriport.core_api.repository;

import com.wooriport.core_api.domain.GoalsPortfolios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GoalsPortfoliosRepository extends JpaRepository<GoalsPortfolios, UUID> {

    // 목표별 포트폴리오 목록 조회
    @Query("""
        SELECT gp FROM GoalsPortfolios gp
        WHERE gp.goal.id = :goalId
        """)
    List<GoalsPortfolios> findByGoalId(@Param("goalId") UUID goalId);
}