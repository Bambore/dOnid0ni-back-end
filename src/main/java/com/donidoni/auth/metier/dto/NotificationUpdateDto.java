package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.TypeNotification;
import jakarta.validation.constraints.Size;

/** Mise à jour partielle d'une notification. */
public record NotificationUpdateDto(

        TypeNotification type,

        @Size(max = 200, groups = OnUpdate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titreFr,

        @Size(max = 200, groups = OnUpdate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titreEn,

        @Size(max = 1000, groups = OnUpdate.class, message = "Le corps ne peut pas dépasser 1000 caractères")
        String corpsFr,

        @Size(max = 1000, groups = OnUpdate.class, message = "Le corps ne peut pas dépasser 1000 caractères")
        String corpsEn,

        Boolean lue,

        @Size(max = 255, groups = OnUpdate.class, message = "Le lien ne peut pas dépasser 255 caractères")
        String lienAction
) {
}
