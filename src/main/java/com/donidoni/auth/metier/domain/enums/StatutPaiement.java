package com.donidoni.auth.metier.domain.enums;

/** Cycle de vie d'une transaction de paiement. */
public enum StatutPaiement {
    INITIE,
    EN_COURS,
    REUSSI,
    ECHOUE,
    REMBOURSE
}
