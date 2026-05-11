package com.wooriport.core_api.base.exception;

import com.wooriport.core_api.base.dto.response.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //  비즈니스 로직 에러 처리 (로그인 비밀번호 틀림, 중복 이메일 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[400] 비즈니스 로직 에러: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.fail(400, e.getMessage()));
    }

    // 이체 관련 상태 에러 (잔액 부족, confirm 미완료 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDTO> handleIllegalStateException(IllegalStateException e) {
        log.warn("[400] 이체 상태 에러: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.fail(400, e.getMessage()));
    }


    // @Valid 유효성 검사 실패 에러 처리 (이메일 양식 틀림, 비밀번호 규칙 위반 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("[400] 유효성 검사 실패: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDTO.fail(400, errorMessage));
    }

    // 그 외 서버 내부에서 터지는 예상치 못한 에러 (NullPointer 등) 최후의 보루
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleAllExceptions(Exception e) {
        log.error("[500] 서버 내부 오류 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.fail(500, "서버 내부에서 알 수 없는 오류가 발생했습니다."));
    }
}
