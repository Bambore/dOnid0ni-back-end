package com.donidoni.auth.metier.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Proposition soumise par l'utilisateur : un produit et une destination.
 *
 * <p>Correspond au formulaire « 1. Choisissez le produit / 2. Choisissez la
 * destination » de l'onglet Sondage.</p>
 */
public record VoteSondageRequestDto(

        @NotNull(message = "Le produit choisi est obligatoire")
        Long optionId,

        @NotNull(message = "La destination choisie est obligatoire")
        Long paysId
) {
}
