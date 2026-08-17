package com.donidoni.auth.metier.dto;

import java.time.Instant;

/** Confirmation du vote enregistré pour un utilisateur. */
public record VoteSondageResponseDto(
        Long id,
        Long sondageId,
        Long optionId,
        String optionLibelle,
        Long paysId,
        String paysNom,
        Instant dateVote
) {
}
