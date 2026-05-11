package com.wooriport.core_api.repository;

import com.wooriport.core_api.domain.Assets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Assets, UUID> {
    // 급여 통장 조회 (execute에서 사용)
    @Query("""
    SELECT a FROM Assets a
    WHERE a.user.id = :userId
      AND a.isSalary = true
      AND a.deletedAt IS NULL
    """)
    Optional<Assets> findByUserIdAndIsSalaryTrue(@Param("userId") UUID userId);

    // 사용자의 전체 계좌 조회 (soft delete 제외)
    @Query("""
        SELECT a FROM Assets a
        WHERE a.user.id = :userId
          AND a.deletedAt IS NULL
        ORDER BY a.accountPurpose
        """)
    List<Assets> findByUserIdAndDeletedAtIsNull(@Param("userId") UUID userId);

    // 단건 조회 (소유권 검증 포함)
    @Query("""
        SELECT a FROM Assets a
        WHERE a.id = :id
          AND a.user.id = :userId
          AND a.deletedAt IS NULL
        """)
    Optional<Assets> findByIdAndUserId(
            @Param("id") UUID id,
            @Param("userId") UUID userId);
}
