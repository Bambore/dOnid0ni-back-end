package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.MoyenPaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Demande d'encaissement.
 *
 * <p>Exactement une cible doit être renseignée : commande au comptant,
 * échéance de commande, cotisation de tontine ou quote-part de groupage.</p>
 */
public record PaiementCreateDto(

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "1.0", message = "Le montant doit être supérieur à zéro")
        BigDecimal montant,

        @NotNull(message = "Le moyen de paiement est obligatoire")
        MoyenPaiement moyen,

        Long commandeId,

        Long echeanceId,

        Long cotisationId,

        Long participationGroupageId
) {
}
