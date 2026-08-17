package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutEcheance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Échéance d'un paiement « Petit à petit ». */
public record EcheanceResponseDto(
        Long id,
        Integer numeroEcheance,
        BigDecimal montant,
        LocalDate dateEcheance,
        StatutEcheance statut,
        Instant datePaiement
) {
}
