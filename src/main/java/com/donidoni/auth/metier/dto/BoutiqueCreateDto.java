package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Données de création d'une boutique partenaire. */
public record BoutiqueCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le nom de la boutique est obligatoire")
        @Size(max = 150, groups = OnCreate.class, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,

        Long categorieId,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        @Size(max = 500, groups = OnCreate.class, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @Size(max = 20, groups = OnCreate.class, message = "Le téléphone ne peut pas dépasser 20 caractères")
        String telephone,

        @Size(max = 255, groups = OnCreate.class, message = "L'adresse ne peut pas dépasser 255 caractères")
        String adresse,

        @Size(max = 100, groups = OnCreate.class, message = "La ville ne peut pas dépasser 100 caractères")
        String ville,

        @Size(max = 100, groups = OnCreate.class, message = "Les horaires ne peuvent pas dépasser 100 caractères")
        String horaires,

        BigDecimal latitude,

        BigDecimal longitude,

        Boolean active
) {
}
