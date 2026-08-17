package com.donidoni.auth.metier.domain.enums;

/** Cycle de vie d'une commande passée depuis le marché. */
public enum StatutCommande {
    EN_ATTENTE,
    CONFIRMEE,
    EN_PREPARATION,
    EN_LIVRAISON,
    LIVREE,
    ANNULEE
}
