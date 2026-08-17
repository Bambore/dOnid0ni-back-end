package com.donidoni.auth.metier.domain.enums;

/** Modalité de règlement d'une commande — écran « Options de Paiement » du mobile. */
public enum ModePaiement {
    /** « Tout payer » : règlement intégral en une fois. */
    COMPTANT,
    /** « Petit à petit » : règlement réparti sur un échéancier. */
    ECHELONNE
}
