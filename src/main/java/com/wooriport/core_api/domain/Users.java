package com.wooriport.core_api.domain;

import com.wooriport.core_api.domain.common.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // 추구미 설문 결과 유형
    // ROCKET(목표로켓) / TURTLE(단단한거북이) / FOX(영리한여우) /
    // MOUNTAIN(산) / HUNTER(집중사냥꾼) / SPROUT(느린성장) /
    // SPARK(스파크) / WAVE(자유로운파도)
    @Enumerated(EnumType.STRING)
    @Column(name = "finance_type", length = 20)
    private FinanceType financeType;

    // 비즈니스 메서드
    public void updateFinanceType(FinanceType financeType) {
        this.financeType = financeType;
    }

    public void updateProfile(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.delete();
    }

    public enum UserStatus {
        // 정상, 정지, 탈퇴
        ACTIVE, SUSPENDED, WITHDRAWN
    }

    public enum FinanceType {
        ROCKET, TURTLE, FOX, MOUNTAIN, HUNTER, SPROUT, SPARK, WAVE
    }
}
