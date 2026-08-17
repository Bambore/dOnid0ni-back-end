package com.donidoni.auth.metier.domain.enums;

/** Type de notification — pilote les onglets de filtrage de l'écran Notifications. */
public enum TypeNotification {
    /** Confirmation ou rappel de paiement. */
    PAIEMENT,
    /** Alerte métier (nouvelle tontine, groupage bientôt complet...). */
    ALERTE,
    /** Message système (sécurité, compte, biométrie...). */
    SYSTEME,
    /** Offre promotionnelle. */
    PROMO
}
