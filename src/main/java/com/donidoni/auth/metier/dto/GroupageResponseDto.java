package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutGroupage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Groupage tel qu'affiché par le mobile.
 *
 * <p>{@code nombreParticipants} et {@code placesRestantes} alimentent
 * directement la barre de progression et le libellé « n places libres ».</p>
 */
public record GroupageResponseDto(
        Long id,
        String titre,
        String description,
        String imagePrincipale,
        List<String> images,
        BigDecimal montant,
        Long paysId,
        String paysNom,
        String paysEmojiDrapeau,
        Long articleId,
        Integer nombreParticipantsCible,
        int nombreParticipants,
        int placesRestantes,
        StatutGroupage statut,
        Instant dateOuverture,
        LocalDate dateCloturePrevue,
        List<ParticipantResumeDto> participants,
        Instant createdAt,
        Instant updatedAt
) {
}
