package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Données de création d'une tontine d'acquisition. */
public record TontineCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le titre de la tontine est obligatoire")
        @Size(max = 200, groups = OnCreate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titre,

        @Size(max = 2000, groups = OnCreate.class, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        @NotNull(groups = OnCreate.class, message = "Le montant mensuel est obligatoire")
        @DecimalMin(value = "0.0", groups = OnCreate.class, message = "Le montant mensuel ne peut pas être négatif")
        BigDecimal montantMensuel,

        @NotNull(groups = OnCreate.class, message = "Le nombre de participants cible est obligatoire")
        @Min(value = 2, groups = OnCreate.class, message = "Une tontine doit viser au moins 2 participants")
        Integer nombreParticipantsCible,

        @Min(value = 1, groups = OnCreate.class, message = "La durée doit être d'au moins 1 mois")
        Integer dureeMois,

        Long articleId,

        LocalDate dateDebut
) {
}
