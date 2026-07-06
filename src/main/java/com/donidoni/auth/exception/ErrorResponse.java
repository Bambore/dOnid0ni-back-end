package com.donidoni.auth.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Réponse d'erreur normalisée retournée par l'API.
 *
 * @param code      code d'erreur applicatif (ex: AUTH_001)
 * @param message   message d'erreur lisible
 * @param timestamp horodatage de l'erreur
 * @param details   détails supplémentaires (erreurs de validation, etc.)
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        List<FieldError> details
) {

    /**
     * Détail d'une erreur de validation par champ.
     *
     * @param field   nom du champ en erreur
     * @param message message d'erreur
     */
    public record FieldError(String field, String message) {
    }

    /**
     * Factory pour créer une réponse d'erreur simple.
     *
     * @param errorCode le code d'erreur
     * @param message   le message d'erreur
     * @return une {@link ErrorResponse} pré-remplie
     */
    public static ErrorResponse of(final ErrorCode errorCode, final String message) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
