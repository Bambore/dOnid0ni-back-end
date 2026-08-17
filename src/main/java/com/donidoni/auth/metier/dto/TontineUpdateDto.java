package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.StatutTontine;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Données de mise à jour partielle d'une tontine. */
public record TontineUpdateDto(

        @Size(max = 200, groups = OnUpdate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titre,

        @Size(max = 2000, groups = OnUpdate.class, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @Size(max = 500, groups = OnUpdate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        @DecimalMin(value = "0.0", groups = OnUpdate.class, message = "Le montant mensuel ne peut pas être négatif")
        BigDecimal montantMensuel,

        @Min(value = 2, groups = OnUpdate.class, message = "Une tontine doit viser au moins 2 participants")
        Integer nombreParticipantsCible,

        @Min(value = 1, groups = OnUpdate.class, message = "La durée doit être d'au moins 1 mois")
        Integer dureeMois,

        Long articleId,

        StatutTontine statut,

        LocalDate dateDebut,

        LocalDate dateFin
) {
}
