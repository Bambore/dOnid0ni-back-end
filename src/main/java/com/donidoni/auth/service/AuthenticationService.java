package com.donidoni.auth.service;

import com.donidoni.auth.config.OtpProperties;
import com.donidoni.auth.dto.response.AuthResponse;
import com.donidoni.auth.dto.response.OtpResponse;
import com.donidoni.auth.dto.response.UserInfo;
import com.donidoni.auth.keycloak.KeycloakUserService;
import com.donidoni.auth.otp.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Orchestrateur principal d'authentification.
 *
 * <p>Coordonne les flux Google Sign-In et OTP téléphone en déléguant
 * aux services spécialisés (Google, OTP, Keycloak, Token).</p>
 *
 * <p>C'est la <strong>seule</strong> classe qui contient la logique
 * métier d'authentification de bout en bout.</p>
 */
@Slf4j
@Service
public class AuthenticationService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final KeycloakUserService keycloakUserService;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final OtpProperties otpProperties;

    public AuthenticationService(
            final GoogleTokenVerifier googleTokenVerifier,
            final KeycloakUserService keycloakUserService,
            final TokenService tokenService,
            final OtpService otpService,
            final OtpProperties otpProperties) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.keycloakUserService = keycloakUserService;
        this.tokenService = tokenService;
        this.otpService = otpService;
        this.otpProperties = otpProperties;
    }

    // ═══════════════════════════════════════════════════════════
    //  FLUX GOOGLE SIGN-IN
    // ═══════════════════════════════════════════════════════════

    /**
     * Authentifie un utilisateur via Google Sign-In.
     *
     * <p>Flux complet :
     * <ol>
     *   <li>Vérifie le Google ID Token côté serveur</li>
     *   <li>Tente un Token Exchange direct (Google → Keycloak)</li>
     *   <li>Si Token Exchange échoue : cherche/crée l'utilisateur via Admin API</li>
     *   <li>Obtient un token Keycloak pour cet utilisateur</li>
     *   <li>Retourne les tokens + informations utilisateur</li>
     * </ol>
     * </p>
     *
     * @param googleIdToken le Google ID Token brut
     * @return la réponse d'authentification avec tokens Keycloak
     */
    public AuthResponse authenticateWithGoogle(final String googleIdToken) {
        // 1. Vérifier le Google ID Token
        final GoogleTokenVerifier.GoogleUserInfo googleUser =
                googleTokenVerifier.verify(googleIdToken);

        // 2. Tenter le Token Exchange direct (Google ID Token → Keycloak tokens)
        final TokenService.TokenResponse directExchange =
                tokenService.exchangeGoogleToken(googleIdToken);
        if (directExchange != null) {
            // Token Exchange réussi — récupérer les infos utilisateur
            final Optional<UserRepresentation> existingUser =
                    keycloakUserService.findByEmail(googleUser.email());
            final UserInfo userInfo;
            if (existingUser.isPresent()) {
                userInfo = keycloakUserService.toUserInfo(existingUser.get());
            } else {
                // Créer l'utilisateur (Token Exchange peut auto-créer, mais vérifier)
                final String userId = keycloakUserService.createGoogleUser(
                        googleUser.email(),
                        googleUser.firstName(),
                        googleUser.lastName(),
                        googleUser.sub());
                userInfo = keycloakUserService.toUserInfo(
                        keycloakUserService.findById(userId));
            }
            return buildAuthResponse(directExchange, userInfo);
        }

        // 3. Fallback : chercher ou créer l'utilisateur via Admin API
        final Optional<UserRepresentation> existingUser =
                keycloakUserService.findByEmail(googleUser.email());

        final String userId;
        if (existingUser.isPresent()) {
            userId = existingUser.get().getId();
            log.info("[AUTH] Utilisateur Google existant : {}", googleUser.email());
        } else {
            userId = keycloakUserService.createGoogleUser(
                    googleUser.email(),
                    googleUser.firstName(),
                    googleUser.lastName(),
                    googleUser.sub());
            log.info("[AUTH] Nouvel utilisateur Google créé : {}", googleUser.email());
        }

        // 4. Obtenir un token Keycloak pour cet utilisateur
        final TokenService.TokenResponse tokenResponse =
                tokenService.getTokenForUser(userId);

        // 5. Construire la réponse
        final UserRepresentation kcUser = keycloakUserService.findById(userId);
        final UserInfo userInfo = keycloakUserService.toUserInfo(kcUser);

        return buildAuthResponse(tokenResponse, userInfo);
    }

    // ═══════════════════════════════════════════════════════════
    //  FLUX OTP TÉLÉPHONE
    // ═══════════════════════════════════════════════════════════

    /**
     * Envoie un code OTP par SMS au numéro spécifié uniquement si l'utilisateur existe.
     *
     * @param phoneNumber numéro de téléphone au format E.164
     * @return la réponse indiquant le succès de l'envoi et le TTL
     */
    public OtpResponse sendOtp(final String phoneNumber) {
        // Vérifier si l'utilisateur existe avant d'envoyer l'OTP
        final Optional<UserRepresentation> existingUser = keycloakUserService.findByPhone(phoneNumber);
        if (existingUser.isEmpty()) {
            log.warn("[AUTH] Demande OTP pour un numéro inexistant : {}", phoneNumber);
            throw new com.donidoni.auth.exception.AuthException(com.donidoni.auth.exception.ErrorCode.USER_NOT_FOUND, "Ce numéro de téléphone n'est associé à aucun compte");
        }

        otpService.generateAndSend(phoneNumber);
        return new OtpResponse(
                true,
                otpProperties.getTtlSeconds(),
                "Code OTP envoyé avec succès");
    }

    /**
     * Crée un nouvel utilisateur et envoie un code OTP.
     *
     * @param phoneNumber numéro de téléphone
     * @param firstName prénom
     * @param lastName nom
     * @return la réponse indiquant le succès de l'envoi et le TTL
     */
    public OtpResponse registerPhoneUser(final String phoneNumber, final String firstName, final String lastName) {
        // 1. Vérifier que l'utilisateur n'existe pas déjà
        final Optional<UserRepresentation> existingUser = keycloakUserService.findByPhone(phoneNumber);
        if (existingUser.isPresent()) {
            log.warn("[AUTH] Inscription impossible, numéro déjà utilisé : {}", phoneNumber);
            throw new com.donidoni.auth.exception.AuthException(com.donidoni.auth.exception.ErrorCode.USER_CREATION_FAILED, "Ce numéro de téléphone est déjà utilisé");
        }

        // 2. Créer l'utilisateur dans Keycloak
        keycloakUserService.createPhoneUser(phoneNumber, firstName, lastName);

        // 3. Ne pas envoyer d'OTP à la création
        // otpService.generateAndSend(phoneNumber);
        return new OtpResponse(
                true,
                0,
                "Compte créé avec succès");
    }

    /**
     * Vérifie un code OTP et authentifie l'utilisateur.
     *
     * <p>Flux complet :
     * <ol>
     *   <li>Valide le code OTP (Redis)</li>
     *   <li>Cherche l'utilisateur Keycloak par numéro de téléphone</li>
     *   <li>Crée l'utilisateur si inexistant</li>
     *   <li>Obtient un token Keycloak</li>
     *   <li>Retourne les tokens + informations utilisateur</li>
     * </ol>
     * </p>
     *
     * @param phoneNumber numéro de téléphone
     * @param otpCode     code OTP saisi
     * @return la réponse d'authentification avec tokens Keycloak
     */
    public AuthResponse verifyOtpAndAuthenticate(
            final String phoneNumber,
            final String otpCode) {

        // 1. Valider l'OTP
        otpService.verify(phoneNumber, otpCode);

        // 2. Chercher l'utilisateur Keycloak (qui doit exister)
        final Optional<UserRepresentation> existingUser =
                keycloakUserService.findByPhone(phoneNumber);

        final String userId;
        if (existingUser.isPresent()) {
            userId = existingUser.get().getId();
            log.info("[AUTH] Utilisateur téléphone existant authentifié : {}", phoneNumber);
        } else {
            log.error("[AUTH] Incohérence : Utilisateur non trouvé après OTP valide : {}", phoneNumber);
            throw new com.donidoni.auth.exception.AuthException(com.donidoni.auth.exception.ErrorCode.USER_NOT_FOUND, "Utilisateur introuvable");
        }

        // 3. Obtenir un token Keycloak
        final TokenService.TokenResponse tokenResponse =
                tokenService.getTokenForUser(userId);

        // 4. Construire la réponse
        final UserRepresentation kcUser = keycloakUserService.findById(userId);
        final UserInfo userInfo = keycloakUserService.toUserInfo(kcUser);

        return buildAuthResponse(tokenResponse, userInfo);
    }

    // ═══════════════════════════════════════════════════════════
    //  REFRESH TOKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Renouvelle les tokens via le refresh token.
     *
     * @param refreshToken le refresh token actuel
     * @return la réponse d'authentification avec les nouveaux tokens
     */
    public AuthResponse refreshToken(final String refreshToken) {
        final TokenService.TokenResponse tokenResponse =
                tokenService.refreshToken(refreshToken);
        return buildAuthResponse(tokenResponse, null);
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Construit la réponse d'authentification à partir d'une réponse de token.
     */
    private static AuthResponse buildAuthResponse(
            final TokenService.TokenResponse tokenResponse,
            final UserInfo userInfo) {

        return AuthResponse.builder()
                .accessToken(tokenResponse.accessToken())
                .refreshToken(tokenResponse.refreshToken())
                .expiresIn(tokenResponse.expiresIn())
                .tokenType(tokenResponse.tokenType())
                .user(userInfo)
                .build();
    }
}
