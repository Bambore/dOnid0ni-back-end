package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import com.donidoni.auth.metier.domain.enums.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Création d'une notification.
 *
 * <p>{@code utilisateurId} nul produit une notification de diffusion générale,
 * visible par tous les utilisateurs.</p>
 */
public record NotificationCreateDto(

        Long utilisateurId,

        @NotNull(groups = OnCreate.class, message = "Le type de notification est obligatoire")
        TypeNotification type,

        @NotBlank(groups = OnCreate.class, message = "Le titre français est obligatoire")
        @Size(max = 200, groups = OnCreate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titreFr,

        @Size(max = 200, groups = OnCreate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titreEn,

        @NotBlank(groups = OnCreate.class, message = "Le corps français est obligatoire")
        @Size(max = 1000, groups = OnCreate.class, message = "Le corps ne peut pas dépasser 1000 caractères")
        String corpsFr,

        @Size(max = 1000, groups = OnCreate.class, message = "Le corps ne peut pas dépasser 1000 caractères")
        String corpsEn,

        @Size(max = 255, groups = OnCreate.class, message = "Le lien ne peut pas dépasser 255 caractères")
        String lienAction
) {
}
