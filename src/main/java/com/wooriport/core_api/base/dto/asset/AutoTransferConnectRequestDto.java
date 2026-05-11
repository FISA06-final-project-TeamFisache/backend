package com.wooriport.core_api.base.dto.asset;

import lombok.Getter;
import java.util.UUID;

@Getter
public class AutoTransferConnectRequestDto {
    private UUID fromAssetId;  // 타행 급여 계좌 UUID
    private UUID toAssetId;    // 우리은행 계좌 UUID
}
