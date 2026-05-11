package com.wooriport.core_api.base.dto.transfer;
import lombok.Builder;
import lombok.Getter;

// ─────────────────────────────────────────
// PATCH /transfer-plans/{id} 요청 바디
// ─────────────────────────────────────────
@Getter
@Builder
public class TransferPlanUpdateRequestDto {
    private Long plannedAmount;   // 수정할 이체 금액 (선택)
}
