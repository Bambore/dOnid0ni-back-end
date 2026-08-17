package com.donidoni.auth.metier.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Fiche article telle qu'affichée par le marché et la page de détail produit. */
public record ArticleResponseDto(
        Long id,
        String nom,
        String description,
        BigDecimal prix,
        Long categorieId,
        String categorieNom,
        Long boutiqueId,
        String boutiqueNom,
        String boutiqueTelephone,
        String imagePrincipale,
        List<String> images,
        Integer stock,
        boolean disponible,
        boolean paiementEchelonneAutorise,
        Long nombreVues,
        Instant createdAt,
        Instant updatedAt
) {
}
