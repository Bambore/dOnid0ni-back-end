package com.donidoni.auth.crud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Wrapper uniforme pour toutes les réponses API.
 *
 * <p>Garantit un format cohérent pour le frontend :</p>
 * <pre>{@code
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "Ressource créée avec succès",
 *   "timestamp": "2026-07-01T12:00:00Z"
 * }
 * }</pre>
 *
 * @param <T> le type de données encapsulées
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    @Builder.Default
    private final Instant timestamp = Instant.now();

    // ═══════════════════════════════════════════════════════════
    //  FACTORIES — SUCCÈS
    // ═══════════════════════════════════════════════════════════

    /**
     * Réponse de succès avec données.
     *
     * @param data les données à retourner
     * @param <T>  le type de données
     * @return une {@link ApiResponse} de succès
     */
    public static <T> ApiResponse<T> success(final T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Réponse de succès avec données et message.
     *
     * @param data    les données à retourner
     * @param message le message de succès
     * @param <T>     le type de données
     * @return une {@link ApiResponse} de succès
     */
    public static <T> ApiResponse<T> success(final T data, final String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Réponse de succès sans données (ex: suppression).
     *
     * @param message le message de succès
     * @return une {@link ApiResponse} de succès
     */
    public static ApiResponse<Void> success(final String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  FACTORIES — ERREUR
    // ═══════════════════════════════════════════════════════════

    /**
     * Réponse d'erreur avec message.
     *
     * @param message le message d'erreur
     * @param <T>     le type de données (sera {@code null})
     * @return une {@link ApiResponse} d'erreur
     */
    public static <T> ApiResponse<T> error(final String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
