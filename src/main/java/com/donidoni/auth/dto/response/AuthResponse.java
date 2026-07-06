package com.donidoni.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * Réponse d'authentification retournée à Flutter après connexion réussie.
 *
 * @param accessToken  JWT access token Keycloak
 * @param refreshToken JWT refresh token Keycloak
 * @param expiresIn    durée de validité de l'access token en secondes
 * @param tokenType    type de token (toujours "Bearer")
 * @param user         informations utilisateur
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserInfo user
) {
}
