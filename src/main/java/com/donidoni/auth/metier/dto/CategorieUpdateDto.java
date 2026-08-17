package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.TypeCategorie;
import jakarta.validation.constraints.Size;

/** Données de mise à jour partielle d'une catégorie. */
public record CategorieUpdateDto(

        @Size(max = 100, groups = OnUpdate.class, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @Size(max = 500, groups = OnUpdate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        TypeCategorie type,

        Integer ordreAffichage,

        Boolean actif
) {
}
