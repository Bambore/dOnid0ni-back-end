package com.donidoni.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Codes d'erreur applicatifs pour les réponses d'erreur normalisées.
 */
@Getter
public enum ErrorCode {

    // ── Authentification ────────────────────────────────
    INVALID_GOOGLE_TOKEN("AUTH_001", "Google ID Token invalide", HttpStatus.UNAUTHORIZED),
    GOOGLE_AUTH_FAILED("AUTH_002", "Échec de l'authentification Google", HttpStatus.UNAUTHORIZED),
    TOKEN_EXCHANGE_FAILED("AUTH_003", "Échec de l'échange de token Keycloak", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REFRESH_TOKEN("AUTH_004", "Refresh token invalide ou expiré", HttpStatus.UNAUTHORIZED),

    // ── OTP ─────────────────────────────────────────────
    OTP_SEND_FAILED("OTP_001", "Échec de l'envoi du code OTP", HttpStatus.INTERNAL_SERVER_ERROR),
    OTP_INVALID("OTP_002", "Code OTP invalide", HttpStatus.UNAUTHORIZED),
    OTP_EXPIRED("OTP_003", "Code OTP expiré", HttpStatus.GONE),
    OTP_MAX_ATTEMPTS("OTP_004", "Nombre maximum de tentatives atteint", HttpStatus.TOO_MANY_REQUESTS),
    OTP_RATE_LIMITED("OTP_005", "Trop de demandes OTP, réessayez plus tard", HttpStatus.TOO_MANY_REQUESTS),

    // ── Keycloak ────────────────────────────────────────
    USER_CREATION_FAILED("KC_001", "Échec de la création de l'utilisateur", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("KC_002", "Utilisateur introuvable", HttpStatus.NOT_FOUND),

    // ── CRUD Générique ──────────────────────────────────
    RESOURCE_NOT_FOUND("CRUD_001", "Ressource introuvable", HttpStatus.NOT_FOUND),
    INVALID_SEARCH_CRITERIA("CRUD_002", "Critères de recherche invalides", HttpStatus.BAD_REQUEST),

    // ── Backoffice ──────────────────────────────────────
    CONFLICT("BO_001", "Conflit : la ressource existe déjà", HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED("BO_002", "Opération non autorisée", HttpStatus.METHOD_NOT_ALLOWED),

    // ── Général ─────────────────────────────────────────
    VALIDATION_ERROR("GEN_001", "Erreur de validation", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("GEN_002", "Erreur interne du serveur", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(final String code, final String defaultMessage, final HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
