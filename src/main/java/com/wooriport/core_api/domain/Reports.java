package com.wooriport.core_api.domain;

import com.wooriport.core_api.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reports extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "total_income", nullable = false)
    @Builder.Default
    private Long totalIncome = 0L;

    @Column(name = "total_expense", nullable = false)
    @Builder.Default
    private Long totalExpense = 0L;

    // 잉여자금 = total_income - total_expense
    @Column(name = "surplus", nullable = false)
    @Builder.Default
    private Long surplus = 0L;

    // Claude API가 생성한 소비 패턴 분석 코멘트
    @Column(name = "ai_comment", columnDefinition = "TEXT")
    private String aiComment;

    // 비즈니스 메서드
    public void updateAiComment(String aiComment) {
        this.aiComment = aiComment;
    }

    public void calculateSurplus() {
        this.surplus = this.totalIncome - this.totalExpense;
    }
}