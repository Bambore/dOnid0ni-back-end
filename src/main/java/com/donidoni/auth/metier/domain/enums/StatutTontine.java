package com.donidoni.auth.metier.domain.enums;

/** Cycle de vie d'une tontine — correspond aux onglets mobiles « En attente / En cours / Fermé ». */
public enum StatutTontine {
    /** En attente : le nombre de participants cible n'est pas atteint. */
    EN_ATTENTE,
    /** En cours : les cotisations mensuelles ont démarré. */
    EN_COURS,
    /** Fermée : tous les tours de tirage ont été effectués. */
    FERMEE
}
