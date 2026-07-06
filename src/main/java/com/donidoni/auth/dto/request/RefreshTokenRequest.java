package com.donidoni.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de renouvellement de token via refresh token.
 *
 * @param refreshToken le refresh token Keycloak
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Le refresh token est requis")
        String refreshToken
) {
}
