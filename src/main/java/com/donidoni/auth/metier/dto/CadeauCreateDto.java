package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import com.donidoni.auth.metier.domain.enums.TypeCadeau;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Attribution d'un cadeau à un utilisateur. */
public record CadeauCreateDto(

        @NotNull(groups = OnCreate.class, message = "Le bénéficiaire est obligatoire")
        Long utilisateurId,

        @NotBlank(groups = OnCreate.class, message = "Le libellé est obligatoire")
        @Size(max = 150, groups = OnCreate.class, message = "Le libellé ne peut pas dépasser 150 caractères")
        String libelle,

        @Size(max = 500, groups = OnCreate.class, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @Size(max = 500, groups = OnCreate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imageUrl,

        @NotNull(groups = OnCreate.class, message = "Le type de cadeau est obligatoire")
        TypeCadeau type,

        BigDecimal valeur,

        LocalDate dateExpiration
) {
}
