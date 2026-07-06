package com.donidoni.auth.exception;

import lombok.Getter;

/**
 * Exception d'authentification générique.
 */
@Getter
public class AuthException extends RuntimeException {

    private final ErrorCode errorCode;

    public AuthException(final ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public AuthException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthException(final ErrorCode errorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
