package com.liveclass.be_a.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //비즈니스 에러(커스텀 에러) 발생시 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        log.warn("[BusinessException] code: {}, message: {}", exception.getErrorCode().getCode(), exception.getMessage());
        return build(exception.getErrorCode(), exception.getMessage(), request);
    }

    //Lock 충돌 예외 처리
    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleLockConflict(RuntimeException exception, HttpServletRequest request) {
        return build(ErrorCode.LOCK_CONFLICT, ErrorCode.LOCK_CONFLICT.getMessage(), request);
    }

    //JSON 요청 검증 실패, enum 변환 실패, 필수 값 누락 등
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(ErrorCode.INVALID_REQUEST, getBadRequestMessage(exception), request);
    }

    //그 외 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        return build(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode errorCode, String message, HttpServletRequest request) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, message, request.getRequestURI()));
    }

    /**
     * 잘못된 요청 예외에서 클라이언트에게 보여줄 메시지를 추출합니다.
     *
     * Bean Validation 실패인 경우 첫 번째 필드 오류를 반환합니다.
     * 그 외의 경우 예외 메시지를 그대로 사용합니다.
     */
    private String getBadRequestMessage(Exception exception) {
        // 1. @Valid 검증 실패로 발생한 예외인지 확인합니다.
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            // 2. 첫 번째 필드 오류를 찾아 필드명과 오류 메시지를 조합합니다.
            return methodArgumentNotValidException.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .findFirst()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .orElse("Invalid request body.");
        }

        // 3. 검증 예외가 아니면 예외 메시지를 그대로 반환합니다.
        return exception.getMessage();
    }

}
