package com.donidoni.auth.exception;

import lombok.Getter;

/**
 * Exception spécifique aux opérations OTP (envoi, validation, rate limiting).
 */
@Getter
public class OtpException extends RuntimeException {

    private final ErrorCode errorCode;

    public OtpException(final ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public OtpException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
