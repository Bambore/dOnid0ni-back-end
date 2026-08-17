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

    // ── Métier : groupages et tontines ──────────────────
    GROUPAGE_COMPLET("MET_001", "Ce groupage est complet", HttpStatus.CONFLICT),
    GROUPAGE_FERME("MET_002", "Ce groupage n'accepte plus d'adhésion", HttpStatus.CONFLICT),
    DEJA_PARTICIPANT("MET_003", "Vous participez déjà à cette opération", HttpStatus.CONFLICT),
    PARTICIPATION_INTROUVABLE("MET_004", "Aucune participation trouvée", HttpStatus.NOT_FOUND),
    TONTINE_COMPLETE("MET_005", "Cette tontine est complète", HttpStatus.CONFLICT),
    TONTINE_FERMEE("MET_006", "Cette tontine n'accepte plus d'adhésion", HttpStatus.CONFLICT),

    // ── Métier : sondages ───────────────────────────────
    SONDAGE_CLOS("MET_010", "Ce sondage est clos", HttpStatus.CONFLICT),
    OPTION_SONDAGE_INVALIDE("MET_011", "Le produit choisi n'appartient pas à ce sondage", HttpStatus.BAD_REQUEST),

    // ── Métier : commandes et paiements ─────────────────
    ARTICLE_INDISPONIBLE("MET_020", "Article indisponible ou stock insuffisant", HttpStatus.CONFLICT),
    PAIEMENT_ECHELONNE_INTERDIT("MET_021", "Le paiement échelonné n'est pas autorisé pour cet article", HttpStatus.CONFLICT),
    CIBLE_PAIEMENT_INVALIDE("MET_022", "Le paiement doit viser exactement une cible", HttpStatus.BAD_REQUEST),
    ECHEANCE_DEJA_REGLEE("MET_023", "Cette échéance est déjà réglée", HttpStatus.CONFLICT),
    COMMANDE_NON_MODIFIABLE("MET_024", "Cette commande ne peut plus être modifiée", HttpStatus.CONFLICT),

    // ── Métier : accès ──────────────────────────────────
    ACCES_REFUSE("MET_030", "Cette ressource ne vous appartient pas", HttpStatus.FORBIDDEN),

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
