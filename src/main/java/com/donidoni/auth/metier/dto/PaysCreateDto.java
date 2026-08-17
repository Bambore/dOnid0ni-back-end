package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Données de création d'un pays d'approvisionnement. */
public record PaysCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le nom du pays est obligatoire")
        @Size(max = 100, groups = OnCreate.class, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @NotBlank(groups = OnCreate.class, message = "Le code ISO est obligatoire")
        @Size(min = 2, max = 2, groups = OnCreate.class, message = "Le code ISO doit contenir exactement 2 caractères")
        String codeIso,

        @Size(max = 16, groups = OnCreate.class, message = "L'emoji ne peut pas dépasser 16 caractères")
        String emojiDrapeau,

        Boolean actif
) {
}
