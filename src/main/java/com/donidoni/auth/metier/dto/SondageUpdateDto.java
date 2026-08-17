package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.StatutSondage;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Données de mise à jour partielle d'un sondage. */
public record SondageUpdateDto(

        @Size(max = 200, groups = OnUpdate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titre,

        @Size(max = 1000, groups = OnUpdate.class, message = "La description ne peut pas dépasser 1000 caractères")
        String description,

        StatutSondage statut,

        Instant dateFin
) {
}
