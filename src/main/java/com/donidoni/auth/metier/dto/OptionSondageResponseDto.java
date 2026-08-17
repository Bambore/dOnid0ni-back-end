package com.donidoni.auth.metier.dto;

/** Option de sondage renvoyée au mobile (vignette de la grille de vote). */
public record OptionSondageResponseDto(
        Long id,
        String libelle,
        String imageUrl,
        Long articleId,
        Integer ordreAffichage
) {
}
