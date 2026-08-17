package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutSondage;

import java.time.Instant;
import java.util.List;

/** Sondage complet : options de produits et destinations proposées au vote. */
public record SondageResponseDto(
        Long id,
        String titre,
        String description,
        StatutSondage statut,
        Instant dateDebut,
        Instant dateFin,
        boolean ouvert,
        long nombreVotes,
        List<OptionSondageResponseDto> options,
        Instant createdAt,
        Instant updatedAt
) {
}
