package com.wooriport.core_api.service;

import com.wooriport.core_api.base.dto.asset.*;
import com.wooriport.core_api.domain.Assets;
import com.wooriport.core_api.domain.Users;
import com.wooriport.core_api.repository.AssetRepository;
import com.wooriport.core_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    // POST /assets/sync
    // 더미 계좌 연동 (마이데이터 대체)
    @Transactional
    public AssetListResponseDto syncAssets(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 연동된 계좌 있으면 synced_at만 갱신
        List<Assets> existing = assetRepository.findByUserIdAndDeletedAtIsNull(userId);
        if (!existing.isEmpty()) {
            existing.forEach(a -> a.updateBalance(a.getBalance()));
            return toListResponse(existing);
        }

        // 더미 계좌 5개 신규 생성
        List<Assets> dummyAssets = createDummyAssets(user);
        assetRepository.saveAll(dummyAssets);

        return toListResponse(dummyAssets);
    }

    // GET /assets
    // 전체 자산 목록 조회
    @Transactional(readOnly = true)
    public AssetListResponseDto getAssets(UUID userId) {
        List<Assets> assets = assetRepository.findByUserIdAndDeletedAtIsNull(userId);

        if (assets.isEmpty()) {
            throw new IllegalStateException("연동된 계좌가 없습니다. 먼저 계좌를 연동해주세요.");
        }

        return toListResponse(assets);
    }

    // POST /assets/auto-transfer/connect
    // 타행 급여 계좌 → 우리은행 자동이체 연결
    // users 테이블 수정 없음 — assets만 사용
    @Transactional
    public void connectAutoTransfer(UUID userId, AutoTransferConnectRequestDto request) {
        Assets fromAsset = assetRepository.findByIdAndUserId(request.getFromAssetId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));

        Assets toAsset = assetRepository.findByIdAndUserId(request.getToAssetId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("입금 계좌를 찾을 수 없습니다."));

        // 입금 계좌가 우리은행인지 검증
        if (toAsset.getBankType() != Assets.BankType.WOORI) {
            throw new IllegalArgumentException("자동이체 연결은 우리은행 계좌만 가능합니다.");
        }

        // 출금 계좌를 급여 통장(SALARY)으로 지정
        // → 이후 execute() 에서 account_purpose = SALARY 로 급여 통장 조회
        fromAsset.updateAccountPurpose(Assets.AccountPurpose.SALARY);
    }

    // GET /assets/auto-transfer/status
    // 자동이체 연결 여부 조회
    // SALARY 계좌 존재 여부로 판단
    @Transactional(readOnly = true)
    public AutoTransferStatusResponseDto getAutoTransferStatus(UUID userId) {
        Optional<Assets> salaryAsset = assetRepository
                .findByUserIdAndAccountPurpose(userId, Assets.AccountPurpose.SALARY);

        // SALARY 계좌 없음 → 자동이체 미연결
        if (salaryAsset.isEmpty()) {
            return AutoTransferStatusResponseDto.builder()
                    .isConnected(false)
                    .restrictedFeatures(List.of(
                            "AI 기반 급여 자동 분배",
                            "월급 입금 감지 리밸런싱",
                            "자동 이체 실행"))
                    .build();
        }

        // SALARY 계좌 있음 + WOORI 계좌 존재 → 정상 연결
        boolean hasWooriAccount = assetRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .anyMatch(a -> a.getBankType() == Assets.BankType.WOORI);

        return AutoTransferStatusResponseDto.builder()
                .isConnected(hasWooriAccount)
                .restrictedFeatures(hasWooriAccount
                        ? List.of()
                        : List.of("AI 기반 급여 자동 분배", "자동 이체 실행"))
                .fromAssetId(salaryAsset.get().getId())
                .fromInstitution(salaryAsset.get().getInstitution())
                .build();
    }

    // PATCH /transfer-plans/scheduled-date
    // 자동이체 실행일 설정 → users.salary_date 저장
    @Transactional
    public void updateScheduledDate(UUID userId, ScheduledDateRequestDto request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateSalaryDate(request.getScheduledDate());
    }

    // GET /assets/summary
    // 총 자산 요약
    @Transactional(readOnly = true)
    public AssetSummaryResponseDto getAssetSummary(UUID userId) {
        List<Assets> assets = assetRepository.findByUserIdAndDeletedAtIsNull(userId);

        long totalBalance = assets.stream().mapToLong(Assets::getBalance).sum();
        long bankTotal    = assets.stream()
                .filter(a -> a.getAssetType() == Assets.AssetType.BANK)
                .mapToLong(Assets::getBalance).sum();
        long stockTotal   = assets.stream()
                .filter(a -> a.getAssetType() == Assets.AssetType.STOCK)
                .mapToLong(Assets::getBalance).sum();
        long cardTotal    = assets.stream()
                .filter(a -> a.getAssetType() == Assets.AssetType.CARD)
                .mapToLong(Assets::getBalance).sum();

        return AssetSummaryResponseDto.builder()
                .totalBalance(totalBalance)
                .assetCount(assets.size())
                .bankTotal(bankTotal)
                .stockTotal(stockTotal)
                .cardTotal(cardTotal)
                .build();
    }

    // Entity → Response 변환
    private AssetListResponseDto toListResponse(List<Assets> assets) {
        long totalBalance = assets.stream().mapToLong(Assets::getBalance).sum();

        List<AssetListResponseDto.AssetItem> items = assets.stream()
                .map(a -> AssetListResponseDto.AssetItem.builder()
                        .id(a.getId())
                        .institution(a.getInstitution())
                        .assetType(a.getAssetType().name())
                        .accountPurpose(a.getAccountPurpose() != null
                                ? a.getAccountPurpose().name() : null)
                        .balance(a.getBalance())
                        .bankType(a.getBankType().name())
                        .syncedAt(a.getSyncedAt().toString())
                        .build())
                .collect(Collectors.toList());

        return AssetListResponseDto.builder()
                .assets(items)
                .totalCount(items.size())
                .totalBalance(totalBalance)
                .build();
    }

    // 더미 계좌 5개 생성 (마이데이터 대체)
    private List<Assets> createDummyAssets(Users user) {
        return List.of(
                Assets.builder()
                        .user(user).institution("우리은행")
                        .assetType(Assets.AssetType.BANK)
                        .accountPurpose(Assets.AccountPurpose.SALARY)
                        .balance(5000000L).syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.WOORI).build(),

                Assets.builder()
                        .user(user).institution("카카오뱅크")
                        .assetType(Assets.AssetType.BANK)
                        .accountPurpose(Assets.AccountPurpose.SPENDING)
                        .balance(800000L).syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.OTHER).build(),

                Assets.builder()
                        .user(user).institution("토스뱅크")
                        .assetType(Assets.AssetType.BANK)
                        .accountPurpose(Assets.AccountPurpose.EMERGENCY)
                        .balance(1200000L).syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.OTHER).build(),

                Assets.builder()
                        .user(user).institution("신한은행")
                        .assetType(Assets.AssetType.BANK)
                        .accountPurpose(Assets.AccountPurpose.TARGET)
                        .balance(500000L).syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.OTHER).build(),

                Assets.builder()
                        .user(user).institution("우리은행")
                        .assetType(Assets.AssetType.BANK)
                        .accountPurpose(Assets.AccountPurpose.TARGET)
                        .balance(2000000L).syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.WOORI).build()
        );
    }
}