package com.donidoni.auth.metier.domain.enums;

/** État d'une échéance de paiement (commande échelonnée ou cotisation de tontine). */
public enum StatutEcheance {
    /** Échéance à venir, non encore réglée. */
    A_PAYER,
    /** Échéance réglée. */
    PAYEE,
    /** Date d'échéance dépassée sans règlement. */
    EN_RETARD,
    /** Échéance annulée (commande ou tontine annulée). */
    ANNULEE
}
