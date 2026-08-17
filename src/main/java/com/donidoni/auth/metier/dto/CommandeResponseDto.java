package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.ModePaiement;
import com.donidoni.auth.metier.domain.enums.StatutCommande;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Commande complète : lignes, échéancier et reste à payer. */
public record CommandeResponseDto(
        Long id,
        String reference,
        Long utilisateurId,
        Long boutiqueId,
        String boutiqueNom,
        BigDecimal montantTotal,
        BigDecimal montantRegle,
        BigDecimal resteAPayer,
        ModePaiement modePaiement,
        Integer nombreEcheances,
        StatutCommande statut,
        String adresseLivraison,
        String telephoneLivraison,
        Instant dateCommande,
        List<LigneCommandeResponseDto> lignes,
        List<EcheanceResponseDto> echeances,
        Instant createdAt,
        Instant updatedAt
) {
}
