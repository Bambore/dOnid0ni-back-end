package com.donidoni.auth.metier.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Fiche boutique telle qu'affichée par l'écran « Boutiques Partenaires ». */
public record BoutiqueResponseDto(
        Long id,
        String nom,
        Long categorieId,
        String categorieNom,
        String imageUrl,
        String description,
        String telephone,
        String adresse,
        String ville,
        String horaires,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
