package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Données de création d'un article du marché. */
public record ArticleCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le nom de l'article est obligatoire")
        @Size(max = 150, groups = OnCreate.class, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,

        @Size(max = 2000, groups = OnCreate.class, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @NotNull(groups = OnCreate.class, message = "Le prix est obligatoire")
        @DecimalMin(value = "0.0", groups = OnCreate.class, message = "Le prix ne peut pas être négatif")
        BigDecimal prix,

        Long categorieId,

        Long boutiqueId,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imagePrincipale,

        List<String> images,

        @Min(value = 0, groups = OnCreate.class, message = "Le stock ne peut pas être négatif")
        Integer stock,

        Boolean disponible,

        Boolean paiementEchelonneAutorise
) {
}
