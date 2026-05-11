package com.wooriport.core_api.controller;


import com.wooriport.core_api.base.dto.response.ResponseDTO;
import com.wooriport.core_api.base.dto.transfer.TransferExecutionListResponseDto;
import com.wooriport.core_api.base.dto.transfer.TransferExecutionResponseDto;
import com.wooriport.core_api.config.security.CustomUserDetails;
import com.wooriport.core_api.service.TransferExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transfer Execution", description = "이체 실행 및 결과 관리 API")
@RestController
@RequestMapping("/api/v1/transfer-executions")
@RequiredArgsConstructor
public class TransferExecutionController {

    private final TransferExecutionService transferExecutionService;

    /**
     * GET /api/v1/transfer-executions?year=2025&month=5
     * 월별 이체 실행 결과 내역 조회
     */
    @Operation(summary = "월별 이체 실행 내역 조회", description = "지정된 연도와 월에 해당하는 실제 이체 처리 내역(성공, 대기, 실패 등)을 조회합니다.")
    @GetMapping
    public ResponseEntity<ResponseDTO<TransferExecutionListResponseDto>> getExecutions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        TransferExecutionListResponseDto data = transferExecutionService.getExecutions(
                userDetails.getUserId(), year, month);

        return ResponseEntity.ok(
                ResponseDTO.success(200, "이체 실행 내역 조회 성공", data));
    }

    /**
     * POST /api/v1/transfer-executions/execute?year=2025&month=5
     * 실제 이체 실행 (is_confirmed = TRUE 인 계획만)
     * → @Transactional: 출금·입금·이력 저장 한 번에 처리
     */
    @Operation(summary = "수동 이체 실행", description = "해당 연/월의 이체 계획 중 사용자가 확정한(is_confirmed = TRUE) 내역에 대해 실제 출금 및 입금 트랜잭션을 수행합니다.")
    @PostMapping("/execute")
    public ResponseEntity<ResponseDTO<TransferExecutionResponseDto>> execute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        TransferExecutionResponseDto data = transferExecutionService.execute(
                userDetails.getUserId(), year, month);

        return ResponseEntity.ok(
                ResponseDTO.success(200, "이체 실행 완료", data));
    }
}