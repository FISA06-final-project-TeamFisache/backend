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
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

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
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Assets fromAsset = assetRepository.findByIdAndUserId(request.getFromAssetId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));

        Assets toAsset = assetRepository.findByIdAndUserId(request.getToAssetId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("입금 계좌를 찾을 수 없습니다."));

        if (toAsset.getBankType() != Assets.BankType.WOORI) {
            throw new IllegalArgumentException("자동이체 연결은 우리은행 계좌만 가능합니다.");
        }

        // 기존 급여 통장 해제
        assetRepository.findByUserIdAndIsSalaryTrue(userId)
                .ifPresent(Assets::unmarkAsSalary);

        // 타행 급여 통장 지정
        fromAsset.markAsSalary();

        // 우리은행 이체 대상 계좌 저장 ← 추가
        user.connectAutoTransfer(fromAsset.getId(), toAsset.getId());
    }

    // GET /assets/auto-transfer/status
    // 자동이체 연결 여부 조회
    // SALARY 계좌 존재 여부로 판단
    @Transactional(readOnly = true)
    public AutoTransferStatusResponseDto getAutoTransferStatus(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Optional<Assets> salaryAsset = assetRepository
                .findByUserIdAndIsSalaryTrue(userId);  // ← 쿼리 변경

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
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        List<Assets> assets = assetRepository.findByUserIdAndDeletedAtIsNull(userId);

        long totalBalance = assets.stream()
                .mapToLong(Assets::getBalance)
                .sum();

        return AssetSummaryResponseDto.builder()
                .totalBalance(totalBalance)
                .assetCount(assets.size())
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
                        .assetNumber(a.getAssetNumber())
                        .accountName(a.getAccountName())
                        .accountPurpose(a.getAccountPurpose())
                        .isSalary(a.getIsSalary())
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
                        .user(user)
                        .institution("우리은행")
                        .assetType(Assets.AssetType.CHECKING)
                        .assetNumber("1002-123-456789")
                        .accountName("우리 WON 입출금통장")
                        .accountPurpose("월급 통장")
                        .isSalary(false)
                        .balance(5000000L)
                        .syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.WOORI)
                        .build(),

                Assets.builder()
                        .user(user)
                        .institution("카카오뱅크")
                        .assetType(Assets.AssetType.CHECKING)
                        .assetNumber("3333-01-1234567")
                        .accountName("카카오뱅크 입출금")
                        .accountPurpose("급여 통장")
                        .isSalary(true)
                        .balance(800000L)
                        .syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.OTHER)
                        .build(),

                // 3. 비상금 — 토스뱅크 파킹
                Assets.builder()
                        .user(user)
                        .institution("토스뱅크")
                        .assetType(Assets.AssetType.PARKING)
                        .assetNumber("7755-01-9876543")
                        .accountName("토스 파킹통장")
                        .accountPurpose("비상금")
                        .isSalary(false)
                        .balance(1200000L)
                        .syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.OTHER)
                        .build(),

                // 4. 여행 적금 — 신한은행
                Assets.builder()
                        .user(user)
                        .institution("신한은행")
                        .assetType(Assets.AssetType.SAVINGS)
                        .assetNumber("110-456-789012")
                        .accountName("신한 쏠편한 적금")
                        .accountPurpose("여행 적금")
                        .isSalary(false)
                        .balance(500000L)
                        .syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.OTHER)
                        .build(),

                // 5. 청약 — 우리은행
                Assets.builder()
                        .user(user)
                        .institution("우리은행")
                        .assetType(Assets.AssetType.SAVINGS)
                        .assetNumber("1002-987-654321")
                        .accountName("우리 청약종합저축")
                        .accountPurpose("청약")
                        .isSalary(false)
                        .balance(2000000L)
                        .syncedAt(LocalDateTime.now())
                        .bankType(Assets.BankType.WOORI)
                        .build()
        );
    }
}