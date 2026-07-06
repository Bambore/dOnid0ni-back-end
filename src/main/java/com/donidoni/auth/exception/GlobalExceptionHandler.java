package com.donidoni.auth.exception;

import jakarta.validation.ConstraintViolationException;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Gestionnaire global des exceptions pour normaliser les réponses d'erreur.
 *
 * <p>Intercepte les exceptions métier ({@link AuthException}, {@link OtpException})
 * et les erreurs de validation pour retourner un format {@link ErrorResponse} cohérent.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les exceptions d'authentification.
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(final AuthException ex) {
        log.warn("[AUTH] {} — {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * Gère les exceptions OTP.
     */
    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(final OtpException ex) {
        log.warn("[OTP] {} — {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * Gère les erreurs de validation {@code @Valid} sur les DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            final MethodArgumentNotValidException ex) {

        final List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        final ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Erreur de validation des données")
                .timestamp(Instant.now())
                .details(fieldErrors)
                .build();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(response);
    }

    /**
     * Gère les violations de contraintes de validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            final ConstraintViolationException ex) {

        final List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> new ErrorResponse.FieldError(
                        cv.getPropertyPath().toString(),
                        cv.getMessage()))
                .toList();

        final ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Erreur de validation des paramètres")
                .timestamp(Instant.now())
                .details(fieldErrors)
                .build();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(response);
    }

    /**
     * Gère les ressources introuvables (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(final ResourceNotFoundException ex) {
        log.warn("[CRUD] {} — {}", ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()));
    }

    /**
     * Gère toutes les exceptions non prévues.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Exception ex) {
        log.error("[ERROR] Exception non gérée : {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR,
                        "Une erreur interne est survenue"));
    }
}
