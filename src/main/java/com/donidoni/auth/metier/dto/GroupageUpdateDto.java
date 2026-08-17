package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.StatutGroupage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Données de mise à jour partielle d'un groupage. */
public record GroupageUpdateDto(

        @Size(max = 200, groups = OnUpdate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titre,

        @Size(max = 2000, groups = OnUpdate.class, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @Size(max = 500, groups = OnUpdate.class, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
        String imagePrincipale,

        List<String> images,

        @DecimalMin(value = "0.0", groups = OnUpdate.class, message = "Le montant ne peut pas être négatif")
        BigDecimal montant,

        Long paysId,

        Long articleId,

        @Min(value = 2, groups = OnUpdate.class, message = "Un groupage doit viser au moins 2 participants")
        Integer nombreParticipantsCible,

        StatutGroupage statut,

        LocalDate dateCloturePrevue
) {
}
