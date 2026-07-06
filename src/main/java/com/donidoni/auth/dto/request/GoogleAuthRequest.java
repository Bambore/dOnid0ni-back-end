package com.donidoni.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête d'authentification via Google Sign-In.
 *
 * @param idToken le Google ID Token obtenu par le SDK Flutter
 */
public record GoogleAuthRequest(
        @NotBlank(message = "Le Google ID Token est requis")
        String idToken
) {
}
