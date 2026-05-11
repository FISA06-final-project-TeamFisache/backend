package com.wooriport.core_api.base.dto.asset;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AssetListResponseDto {
    private List<AssetItem> assets;
    private int totalCount;
    private Long totalBalance;

    @Getter
    @Builder
    public static class AssetItem {
        private UUID id;
        private String institution;     // 금융사명
        private String assetType;       // BANK / CARD / STOCK
        private String accountPurpose;  // SALARY / SPENDING / EMERGENCY / TARGET
        private Long balance;
        private String bankType;        // WOORI / OTHER
        private String syncedAt;
    }
}
