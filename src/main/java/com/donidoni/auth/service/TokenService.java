package com.donidoni.auth.service;

import com.donidoni.auth.config.KeycloakProperties;
import com.donidoni.auth.exception.AuthException;
import com.donidoni.auth.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Service d'obtention et de renouvellement de tokens Keycloak.
 *
 * <p>Utilise le Token Exchange (RFC 8693) pour obtenir un token
 * Keycloak au nom d'un utilisateur identifié, et le grant
 * {@code refresh_token} pour renouveler les tokens.</p>
 *
 * <p>Si Token Exchange n'est pas disponible, un fallback via
 * Direct Access Grant avec mot de passe technique est utilisé.</p>
 */
@Slf4j
@Service
public class TokenService {

    private final KeycloakProperties keycloakProperties;
    private final RestClient restClient;

    public TokenService(final KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    /**
     * Obtient un token Keycloak pour un utilisateur via Token Exchange.
     *
     * <p>Utilise le grant {@code urn:ietf:params:oauth:grant-type:token-exchange}
     * avec {@code requested_subject} pour impersonnifier l'utilisateur.</p>
     *
     * @param userId l'identifiant Keycloak de l'utilisateur
     * @return les données du token (access_token, refresh_token, expires_in)
     * @throws AuthException si l'échange échoue
     */
    public TokenResponse getTokenForUser(final String userId) {
        final String tokenUrl = buildTokenUrl();

        final String formBody = buildFormBody(Map.of(
                "grant_type", "urn:ietf:params:oauth:grant-type:token-exchange",
                "client_id", keycloakProperties.getClientId(),
                "client_secret", keycloakProperties.getClientSecret(),
                "requested_subject", userId,
                "requested_token_type", "urn:ietf:params:oauth:token-type:refresh_token",
                "subject_token_type", "urn:ietf:params:oauth:token-type:access_token"
        ));

        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> response = restClient.post()
                    .uri(tokenUrl)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new AuthException(ErrorCode.TOKEN_EXCHANGE_FAILED,
                        "Réponse vide ou sans access_token de Keycloak");
            }

            final TokenResponse tokenResponse = mapToTokenResponse(response);
            log.info("[TOKEN] Token Exchange réussi pour utilisateur {}", userId);
            return tokenResponse;

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TOKEN] Échec Token Exchange pour {} : {}", userId, e.getMessage());
            // Fallback : essayer avec un grant password si Token Exchange échoue
            return getTokenViaPasswordGrant(userId);
        }
    }

    /**
     * Renouvelle un access token via le grant {@code refresh_token}.
     *
     * @param refreshToken le refresh token actuel
     * @return les nouveaux tokens
     * @throws AuthException si le refresh token est invalide ou expiré
     */
    public TokenResponse refreshToken(final String refreshToken) {
        final String tokenUrl = buildTokenUrl();

        final String formBody = buildFormBody(Map.of(
                "grant_type", "refresh_token",
                "client_id", keycloakProperties.getClientId(),
                "client_secret", keycloakProperties.getClientSecret(),
                "refresh_token", refreshToken
        ));

        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> response = restClient.post()
                    .uri(tokenUrl)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            log.info("[TOKEN] Refresh token réussi");
            return mapToTokenResponse(response);

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[TOKEN] Échec refresh : {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN,
                    "Le refresh token est invalide ou expiré", e);
        }
    }

    /**
     * Obtient un token via Token Exchange avec un Google ID Token externe.
     *
     * @param googleIdToken le Google ID Token validé
     * @return les tokens Keycloak
     */
    public TokenResponse exchangeGoogleToken(final String googleIdToken) {
        final String tokenUrl = buildTokenUrl();

        final String formBody = buildFormBody(Map.of(
                "grant_type", "urn:ietf:params:oauth:grant-type:token-exchange",
                "client_id", keycloakProperties.getClientId(),
                "client_secret", keycloakProperties.getClientSecret(),
                "subject_token", googleIdToken,
                "subject_token_type", "urn:ietf:params:oauth:token-type:id_token",
                "subject_issuer", "google"
        ));

        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> response = restClient.post()
                    .uri(tokenUrl)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("access_token")) {
                log.info("[TOKEN] Google Token Exchange réussi");
                return mapToTokenResponse(response);
            }
        } catch (Exception e) {
            log.debug("[TOKEN] Google Token Exchange non supporté, fallback : {}",
                    e.getMessage());
        }

        // Retourne null pour signaler au caller de passer par la voie classique
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Fallback : obtient un token via Direct Access Grant avec mot de passe technique.
     *
     * <p>Utilisé uniquement si Token Exchange n'est pas activé dans Keycloak.</p>
     */
    private TokenResponse getTokenViaPasswordGrant(final String userId) {
        log.warn("[TOKEN] Fallback vers impersonation pour utilisateur {}", userId);

        final String impersonateUrl = String.format("%s/admin/realms/%s/users/%s/impersonation",
                keycloakProperties.getServerUrl(),
                keycloakProperties.getRealm(),
                userId);

        try {
            // Étape 1 : Impersonate via Admin API
            restClient.post()
                    .uri(impersonateUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .body(Map.class);

            // Étape 2 : Utiliser le grant client_credentials + requested_subject
            // comme alternative la plus compatible
            final String tokenUrl = buildTokenUrl();
            final String formBody = buildFormBody(Map.of(
                    "grant_type", "client_credentials",
                    "client_id", keycloakProperties.getClientId(),
                    "client_secret", keycloakProperties.getClientSecret(),
                    "requested_subject", userId
            ));

            @SuppressWarnings("unchecked")
            final Map<String, Object> response = restClient.post()
                    .uri(tokenUrl)
                    .body(formBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("access_token")) {
                log.info("[TOKEN] Fallback token obtenu pour {}", userId);
                return mapToTokenResponse(response);
            }

            throw new AuthException(ErrorCode.TOKEN_EXCHANGE_FAILED,
                    "Impossible d'obtenir un token pour l'utilisateur " + userId);

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TOKEN] Échec fallback pour {} : {}", userId, e.getMessage());
            throw new AuthException(ErrorCode.TOKEN_EXCHANGE_FAILED,
                    "Impossible d'obtenir un token Keycloak", e);
        }
    }

    /**
     * Construit l'URL du token endpoint Keycloak.
     */
    private String buildTokenUrl() {
        return String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakProperties.getServerUrl(),
                keycloakProperties.getRealm());
    }

    /**
     * Construit le body URL-encoded à partir d'une map de paramètres.
     */
    private static String buildFormBody(final Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        params.forEach((key, value) -> {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(key).append("=").append(value);
        });
        return sb.toString();
    }

    /**
     * Mappe la réponse JSON de Keycloak vers un {@link TokenResponse}.
     */
    private static TokenResponse mapToTokenResponse(final Map<String, Object> response) {
        return new TokenResponse(
                (String) response.get("access_token"),
                (String) response.get("refresh_token"),
                response.get("expires_in") instanceof Number n ? n.longValue() : 0L,
                (String) response.getOrDefault("token_type", "Bearer"));
    }

    /**
     * Réponse de token Keycloak.
     *
     * @param accessToken  JWT access token
     * @param refreshToken JWT refresh token
     * @param expiresIn    durée de validité en secondes
     * @param tokenType    type de token (Bearer)
     */
    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            String tokenType
    ) {
    }
}
