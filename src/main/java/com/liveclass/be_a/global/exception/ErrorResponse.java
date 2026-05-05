package com.liveclass.be_a.global.exception;

import java.time.LocalDateTime;

/**
 * Error 응답 Dto
 * @param timestamp
 * @param status
 * @param error
 * @param code
 * @param message
 * @param path
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                errorCode.getCode(),
                message,
                path
        );
    }
}
