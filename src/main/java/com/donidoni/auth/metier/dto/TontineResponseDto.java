package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutTontine;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Tontine telle qu'affichée par les onglets « En attente / En cours / Fermé ». */
public record TontineResponseDto(
        Long id,
        String titre,
        String description,
        String imageUrl,
        BigDecimal montantMensuel,
        Integer nombreParticipantsCible,
        int nombreParticipants,
        int placesRestantes,
        Integer dureeMois,
        Long articleId,
        StatutTontine statut,
        LocalDate dateDebut,
        LocalDate dateFin,
        List<ParticipantResumeDto> participants,
        Instant createdAt,
        Instant updatedAt
) {
}
