package com.wooriport.core_api.domain;

import com.wooriport.core_api.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "goals_portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GoalsPortfolios extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goals goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    // SAVING(적금) / DEPOSIT(예금) / STOCK(주식) / BOND(채권)
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    // 배분 비율 0~100 (합산 100%)
    @Column(name = "product_ratio", nullable = false)
    private Integer productRatio;

    // 비즈니스 메서드
    public void updateRatio(Integer ratio) {
        this.productRatio = ratio;
    }

    public enum ProductType {
        SAVING, DEPOSIT, STOCK, BOND
    }
}
