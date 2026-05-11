package com.wooriport.core_api.base.dto.asset;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetSummaryResponseDto {
    private Long totalBalance;   // 전체 자산 합산
    private int assetCount;      // 연동된 계좌 수
    private Long bankTotal;      // 은행 계좌 합산
    private Long stockTotal;     // 증권 계좌 합산
    private Long cardTotal;      // 카드 잔액 합산
}
