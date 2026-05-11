package com.wooriport.core_api.repository;

import com.wooriport.core_api.domain.Assets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Assets, UUID> {
    // 급여 통장 조회 (execute에서 사용)
    @Query("""
        SELECT a FROM Assets a
        WHERE a.user.id = :userId
          AND a.accountPurpose = :purpose
          AND a.deletedAt IS NULL
        """)
    Optional<Assets> findByUserIdAndAccountPurpose(
            @Param("userId") UUID userId,
            @Param("purpose") Assets.AccountPurpose purpose);
}
