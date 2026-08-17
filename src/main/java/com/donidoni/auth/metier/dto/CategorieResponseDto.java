package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.TypeCategorie;

import java.time.Instant;

/** Représentation d'une catégorie renvoyée au client. */
public record CategorieResponseDto(
        Long id,
        String nom,
        String imageUrl,
        TypeCategorie type,
        Integer ordreAffichage,
        boolean actif,
        Instant createdAt,
        Instant updatedAt
) {
}
