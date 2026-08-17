package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Données de création d'un groupage. */
public record GroupageCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le titre du groupage est obligatoire")
        @Size(max = 200, groups = OnCreate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titre,

        @Size(max = 2000, groups = OnCreate.class, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imagePrincipale,

        List<String> images,

        @NotNull(groups = OnCreate.class, message = "Le montant de la quote-part est obligatoire")
        @DecimalMin(value = "0.0", groups = OnCreate.class, message = "Le montant ne peut pas être négatif")
        BigDecimal montant,

        Long paysId,

        Long articleId,

        @NotNull(groups = OnCreate.class, message = "Le nombre de participants cible est obligatoire")
        @Min(value = 2, groups = OnCreate.class, message = "Un groupage doit viser au moins 2 participants")
        Integer nombreParticipantsCible,

        LocalDate dateCloturePrevue
) {
}
