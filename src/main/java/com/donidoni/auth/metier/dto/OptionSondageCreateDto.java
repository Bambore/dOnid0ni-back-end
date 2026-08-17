package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Produit proposé au vote dans un sondage. */
public record OptionSondageCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le libellé de l'option est obligatoire")
        @Size(max = 200, groups = OnCreate.class, message = "Le libellé ne peut pas dépasser 200 caractères")
        String libelle,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        Long articleId,

        Integer ordreAffichage
) {
}
