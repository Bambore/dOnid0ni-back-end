package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Données de mise à jour partielle d'un article. */
public record ArticleUpdateDto(

        @Size(max = 150, groups = OnUpdate.class, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,

        @Size(max = 2000, groups = OnUpdate.class, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @DecimalMin(value = "0.0", groups = OnUpdate.class, message = "Le prix ne peut pas être négatif")
        BigDecimal prix,

        Long categorieId,

        Long boutiqueId,

        @Size(max = 500, groups = OnUpdate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imagePrincipale,

        List<String> images,

        @Min(value = 0, groups = OnUpdate.class, message = "Le stock ne peut pas être négatif")
        Integer stock,

        Boolean disponible,

        Boolean paiementEchelonneAutorise
) {
}
