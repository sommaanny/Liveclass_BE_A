package com.liveclass.be_a.global.exception;

import lombok.Getter;

/**
 * 커스텀 런타임 예외
 */
@Getter
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); //ErrorCode에 정의된 메시지로 예외 생성
        this.errorCode = errorCode;
    }
}
