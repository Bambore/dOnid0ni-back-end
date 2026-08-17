package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import jakarta.validation.constraints.Size;

/** Données de mise à jour partielle d'un pays : seuls les champs non nuls sont appliqués. */
public record PaysUpdateDto(

        @Size(max = 100, groups = OnUpdate.class, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @Size(min = 2, max = 2, groups = OnUpdate.class, message = "Le code ISO doit contenir exactement 2 caractères")
        String codeIso,

        @Size(max = 16, groups = OnUpdate.class, message = "L'emoji ne peut pas dépasser 16 caractères")
        String emojiDrapeau,

        Boolean actif
) {
}
