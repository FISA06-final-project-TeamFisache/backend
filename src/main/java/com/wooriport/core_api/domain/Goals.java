package com.wooriport.core_api.domain;

import com.wooriport.core_api.domain.common.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Goals extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 투자 비용을 가져올 계좌 (nullable — 목표 없을 때 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_asset_id", nullable = false)
    private Assets sourceAsset;

    // SAVING(저축) / TRAVEL(여행) / WEDDING(결혼) / OTHER(기타)
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "target_amount", nullable = false)
    private Long targetAmount;

    // 초기 자본금
    @Column(name = "initial_amount", nullable = false)
    @Builder.Default
    private Long initialAmount = 0L;

    // 목표 기간
    // 6개월 이하 → 소비 패턴 중심 관리
    // 7개월 이상 → 주·채·예 포트폴리오 관리
    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "current_amount", nullable = false)
    @Builder.Default
    private Long currentAmount = 0L;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    // AI가 생성한 목표 요약 메시지
    @Column(name = "summary_message", columnDefinition = "TEXT")
    private String summaryMessage;

    // 비즈니스 메서드
    public void updateCurrentAmount(Long amount) {
        this.currentAmount += amount;
        if (this.currentAmount >= this.targetAmount) {
            this.status = GoalStatus.COMPLETED;
        }
    }

    public void cancel() {
        this.status = GoalStatus.CANCELLED;
        this.delete();
    }

    public void expire() {
        this.status = GoalStatus.EXPIRED;
    }

    public int getProgressRate() {
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount * 100.0) / targetAmount);
    }

    public enum GoalType {
        SAVING, TRAVEL, WEDDING, OTHER
    }

    public enum GoalStatus {
        ACTIVE, COMPLETED, CANCELLED, EXPIRED
    }

    // 6개월 이하 → 소비 패턴 중심
    public boolean isShortTerm() {
        return durationMonths != null && durationMonths <= 6;
    }

    // 7개월 이상 → 주·채·예 포트폴리오
    public boolean isLongTerm() {
        return durationMonths != null && durationMonths >= 7;
    }
}