package com.donidoni.auth.metier.dto;

import java.math.BigDecimal;

/** Ligne d'une commande renvoyée au client. */
public record LigneCommandeResponseDto(
        Long id,
        Long articleId,
        String libelleArticle,
        String imageArticle,
        Integer quantite,
        BigDecimal prixUnitaire,
        BigDecimal montantLigne
) {
}
