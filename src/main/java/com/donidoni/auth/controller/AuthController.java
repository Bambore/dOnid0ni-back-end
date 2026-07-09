package com.donidoni.auth.controller;

import com.donidoni.auth.dto.request.GoogleAuthRequest;
import com.donidoni.auth.dto.request.RefreshTokenRequest;
import com.donidoni.auth.dto.request.SendOtpRequest;
import com.donidoni.auth.dto.request.VerifyOtpRequest;
import com.donidoni.auth.dto.response.AuthResponse;
import com.donidoni.auth.dto.response.OtpResponse;
import com.donidoni.auth.dto.response.UserInfo;
import com.donidoni.auth.keycloak.KeycloakUserService;
import com.donidoni.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST d'authentification pour l'application mobile Doni-Doni.
 *
 * <p>Expose les endpoints publics d'authentification (Google, OTP, refresh)
 * et un endpoint protégé pour récupérer les informations utilisateur.</p>
 *
 * <p>Ce contrôleur est une couche mince qui délègue toute la logique
 * métier à {@link AuthenticationService}.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification",
        description = "Endpoints d'authentification mobile (Google Sign-In + OTP SMS)")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final KeycloakUserService keycloakUserService;

    public AuthController(
            final AuthenticationService authenticationService,
            final KeycloakUserService keycloakUserService) {
        this.authenticationService = authenticationService;
        this.keycloakUserService = keycloakUserService;
    }

    // ═══════════════════════════════════════════════════════════
    //  GOOGLE SIGN-IN
    // ═══════════════════════════════════════════════════════════

    /**
     * Authentifie un utilisateur via Google Sign-In.
     *
     * <p>Le Google ID Token est vérifié côté serveur, l'utilisateur est
     * créé ou récupéré dans Keycloak, et des tokens JWT sont retournés.</p>
     *
     * @param request contenant le Google ID Token
     * @return les tokens Keycloak et les informations utilisateur
     */
    @PostMapping("/google")
    @Operation(
            summary = "Connexion via Google Sign-In",
            description = "Vérifie le Google ID Token et retourne des tokens Keycloak JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie"),
            @ApiResponse(responseCode = "401", description = "Google ID Token invalide")
    })
    public ResponseEntity<AuthResponse> authenticateWithGoogle(
            @Valid @RequestBody final GoogleAuthRequest request) {

        log.info("[AUTH] Tentative de connexion Google");
        final AuthResponse response =
                authenticationService.authenticateWithGoogle(request.idToken());
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    //  OTP TÉLÉPHONE
    // ═══════════════════════════════════════════════════════════

    /**
     * Envoie un code OTP par SMS au numéro de téléphone spécifié.
     *
     * @param request contenant le numéro de téléphone au format E.164
     * @return la confirmation d'envoi avec le TTL de l'OTP
     */
    @PostMapping("/send-otp")
    @Operation(
            summary = "Envoyer un code OTP par SMS",
            description = "Génère un code OTP à 6 chiffres et l'envoie par SMS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP envoyé avec succès"),
            @ApiResponse(responseCode = "400", description = "Numéro de téléphone invalide"),
            @ApiResponse(responseCode = "429", description = "Rate limit atteint")
    })
    public ResponseEntity<OtpResponse> sendOtp(
            @Valid @RequestBody final SendOtpRequest request) {

        log.info("[AUTH] Demande d'envoi OTP");
        final OtpResponse response =
                authenticationService.sendOtp(request.phoneNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Crée un compte pour un nouveau numéro de téléphone et envoie un OTP.
     *
     * @param request contenant le numéro, le prénom et le nom
     * @return la confirmation d'envoi avec le TTL de l'OTP
     */
    @PostMapping("/register-phone")
    @Operation(
            summary = "S'inscrire avec un numéro de téléphone",
            description = "Crée un compte Keycloak sans envoyer d'OTP immédiatement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compte créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Numéro de téléphone invalide"),
            @ApiResponse(responseCode = "409", description = "Le numéro est déjà utilisé"),
            @ApiResponse(responseCode = "429", description = "Rate limit atteint")
    })
    public ResponseEntity<OtpResponse> registerPhone(
            @Valid @RequestBody final com.donidoni.auth.dto.request.RegisterPhoneRequest request) {

        log.info("[AUTH] Demande d'inscription OTP");
        final OtpResponse response =
                authenticationService.registerPhoneUser(
                        request.phoneNumber(),
                        request.firstName(),
                        request.lastName());
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifie un code OTP et authentifie l'utilisateur.
     *
     * <p>Si le code est valide, l'utilisateur est créé ou récupéré
     * dans Keycloak et des tokens JWT sont retournés.</p>
     *
     * @param request contenant le numéro de téléphone et le code OTP
     * @return les tokens Keycloak et les informations utilisateur
     */
    @PostMapping("/verify-otp")
    @Operation(
            summary = "Vérifier le code OTP et se connecter",
            description = "Valide le code OTP, crée l'utilisateur si nécessaire, retourne les tokens JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie"),
            @ApiResponse(responseCode = "401", description = "Code OTP invalide"),
            @ApiResponse(responseCode = "410", description = "Code OTP expiré"),
            @ApiResponse(responseCode = "429", description = "Trop de tentatives")
    })
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody final VerifyOtpRequest request) {

        log.info("[AUTH] Tentative de vérification OTP");
        final AuthResponse response =
                authenticationService.verifyOtpAndAuthenticate(
                        request.phoneNumber(),
                        request.otpCode());
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    //  REFRESH TOKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Renouvelle les tokens JWT via un refresh token valide.
     *
     * @param request contenant le refresh token
     * @return les nouveaux tokens
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Renouveler les tokens",
            description = "Utilise le refresh token pour obtenir un nouveau couple access/refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renouvelés"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré")
    })
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody final RefreshTokenRequest request) {

        log.info("[AUTH] Demande de refresh token");
        final AuthResponse response =
                authenticationService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    //  INFORMATIONS UTILISATEUR (PROTÉGÉ)
    // ═══════════════════════════════════════════════════════════

    /**
     * Retourne les informations de l'utilisateur authentifié.
     *
     * <p>Cet endpoint est protégé et nécessite un Bearer JWT valide.</p>
     *
     * @param jwt le JWT de l'utilisateur authentifié (injecté par Spring Security)
     * @return les informations utilisateur complètes
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Obtenir les informations de l'utilisateur connecté",
            description = "Retourne le profil et les rôles de l'utilisateur authentifié")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Informations utilisateur"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<UserInfo> getCurrentUser(
            @AuthenticationPrincipal final Jwt jwt) {

        final String userId = jwt.getSubject();
        final UserRepresentation user = keycloakUserService.findById(userId);
        final UserInfo userInfo = keycloakUserService.toUserInfo(user);
        return ResponseEntity.ok(userInfo);
    }
}
