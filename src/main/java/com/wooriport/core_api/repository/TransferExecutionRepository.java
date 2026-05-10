package com.wooriport.core_api.repository;

import com.wooriport.core_api.domain.TransferExecutions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransferExecutionRepository extends JpaRepository<TransferExecutions, UUID> {

    @Query("""
        SELECT te FROM TransferExecutions te
        WHERE te.user.id = :userId
          AND FUNCTION('YEAR', te.createdAt) = :year
          AND FUNCTION('MONTH', te.createdAt) = :month
        ORDER BY te.createdAt DESC
        """)
    List<TransferExecutions> findByUserIdAndYearAndMonth(
            @Param("userId") UUID userId,
            @Param("year") int year,
            @Param("month") int month);
}
