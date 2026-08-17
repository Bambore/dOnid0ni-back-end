package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutEcheance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Cotisation mensuelle d'un participant à une tontine. */
public record CotisationResponseDto(
        Long id,
        Long participationId,
        Integer numeroEcheance,
        BigDecimal montant,
        LocalDate dateEcheance,
        StatutEcheance statut,
        Instant datePaiement
) {
}
