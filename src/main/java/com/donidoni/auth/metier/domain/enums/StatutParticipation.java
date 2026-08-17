package com.donidoni.auth.metier.domain.enums;

/** État de l'adhésion d'un utilisateur à un groupage ou à une tontine. */
public enum StatutParticipation {
    /** Adhésion enregistrée, en attente du premier versement. */
    EN_ATTENTE,
    /** Adhésion confirmée (premier versement encaissé). */
    CONFIRMEE,
    /** Adhésion annulée par l'utilisateur ou par l'administration. */
    ANNULEE
}
