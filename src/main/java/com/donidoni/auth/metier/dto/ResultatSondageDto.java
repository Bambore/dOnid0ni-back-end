package com.donidoni.auth.metier.dto;

/** Ligne de résultat agrégée d'un sondage : un couple produit/destination et son score. */
public record ResultatSondageDto(
        Long optionId,
        String optionLibelle,
        Long paysId,
        String paysNom,
        long nombreVotes,
        double pourcentage
) {
}
