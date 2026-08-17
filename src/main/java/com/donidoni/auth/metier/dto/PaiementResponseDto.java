package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.MoyenPaiement;
import com.donidoni.auth.metier.domain.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.Instant;

/** Transaction de paiement renvoyée au client. */
public record PaiementResponseDto(
        Long id,
        String reference,
        Long utilisateurId,
        BigDecimal montant,
        MoyenPaiement moyen,
        StatutPaiement statut,
        String referenceExterne,
        String message,
        Long commandeId,
        Long echeanceId,
        Long cotisationId,
        Long participationGroupageId,
        Instant dateTransaction
) {
}
