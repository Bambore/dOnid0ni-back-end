package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutParticipation;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Participant affiché dans la liste d'un groupage ou d'une tontine.
 *
 * <p>Seul le nom d'affichage est exposé : ni téléphone ni e-mail ne remontent
 * aux autres participants.</p>
 */
public record ParticipantResumeDto(
        Long participationId,
        Long utilisateurId,
        String nomAffichage,
        Instant dateAdhesion,
        StatutParticipation statut,
        BigDecimal montantVerse,
        Integer rangTirage
) {
}
