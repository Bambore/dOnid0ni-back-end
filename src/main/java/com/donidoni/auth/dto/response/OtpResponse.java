package com.donidoni.auth.dto.response;

/**
 * Réponse d'envoi OTP retournée à Flutter.
 *
 * @param success   {@code true} si l'OTP a été envoyé avec succès
 * @param expiresIn durée de validité de l'OTP en secondes
 * @param message   message descriptif
 */
public record OtpResponse(
        boolean success,
        long expiresIn,
        String message
) {
}
