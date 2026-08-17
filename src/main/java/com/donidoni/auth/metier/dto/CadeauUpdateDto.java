package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Mise à jour partielle d'un cadeau. */
public record CadeauUpdateDto(

        @Size(max = 150, groups = OnUpdate.class, message = "Le libellé ne peut pas dépasser 150 caractères")
        String libelle,

        @Size(max = 500, groups = OnUpdate.class, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @Size(max = 500, groups = OnUpdate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        BigDecimal valeur,

        StatutCadeau statut,

        LocalDate dateExpiration
) {
}
