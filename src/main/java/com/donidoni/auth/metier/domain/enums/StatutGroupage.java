package com.donidoni.auth.metier.domain.enums;

/** Cycle de vie d'un groupage (achat groupé à l'international). */
public enum StatutGroupage {
    /** Ouvert aux adhésions, des places restent libres. */
    OUVERT,
    /** Nombre de participants cible atteint. */
    COMPLET,
    /** Commande passée, marchandise en cours d'acheminement. */
    EN_ACHEMINEMENT,
    /** Groupage livré et clôturé. */
    CLOTURE,
    /** Groupage annulé avant livraison. */
    ANNULE
}
