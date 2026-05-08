package com.wooriport.core_api.domain;


import com.wooriport.core_api.domain.common.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Assets extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "institution", nullable = false, length = 100)
    private String institution;

    // BANK / CARD / STOCK
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    // SALARY(급여) / SPENDING(소비) / EMERGENCY(비상금) / TARGET(목적)
    @Enumerated(EnumType.STRING)
    @Column(name = "account_purpose", length = 20)
    private AccountPurpose accountPurpose;

    // 마지막 동기화 시점의 잔액
    @Column(name = "balance", nullable = false)
    @Builder.Default
    private Long balance = 0L;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    // WOORI(우리은행) / OTHER(타은행)
    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", nullable = false, length = 20)
    private BankType bankType;

    // 비즈니스 메서드
    public void updateBalance(Long balance) {
        this.balance = balance;
        this.syncedAt = LocalDateTime.now();
    }

    public void updateAccountPurpose(AccountPurpose accountPurpose) {
        this.accountPurpose = accountPurpose;
    }

    public boolean isWooriBank() {
        return this.bankType == BankType.WOORI;
    }

    public enum AssetType {
        BANK, CARD, STOCK
    }

    public enum AccountPurpose {
        SALARY, SPENDING, EMERGENCY, TARGET
    }

    public enum BankType {
        WOORI, OTHER
    }
}