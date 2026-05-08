package com.wooriport.core_api.domain;

import com.wooriport.core_api.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notifications extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // EXPENSE_ALERT / GOAL_PROGRESS / REPORT_READY /
    // TRANSFER_CONFIRM / REBALANCE_SUGGEST /
    // BALANCE_BREAK / SPENDING_TREND
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    // 비즈니스 메서드
    public void markAsRead() {
        this.isRead = true;
    }

    public enum NotificationType {
        EXPENSE_ALERT,       // 이상 소비 감지
        GOAL_PROGRESS,       // 목표 달성률 변동
        REPORT_READY,        // 월간 리포트 생성 완료
        TRANSFER_CONFIRM,    // 이체 확인 요청
        REBALANCE_SUGGEST,   // 리밸런싱 제안
        BALANCE_BREAK,       // 밸런스 붕괴 알림
        SPENDING_TREND       // 소비 추세 모니터링
    }
}
