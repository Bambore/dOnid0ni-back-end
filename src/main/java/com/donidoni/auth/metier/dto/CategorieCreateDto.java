package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import com.donidoni.auth.metier.domain.enums.TypeCategorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Données de création d'une catégorie d'articles ou de boutiques. */
public record CategorieCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le nom de la catégorie est obligatoire")
        @Size(max = 100, groups = OnCreate.class, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        @NotNull(groups = OnCreate.class, message = "Le type de catégorie est obligatoire")
        TypeCategorie type,

        Integer ordreAffichage,

        Boolean actif
) {
}
