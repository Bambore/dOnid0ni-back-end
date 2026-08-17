package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/** Données de création d'un sondage « Quel groupage lancer ? ». */
public record SondageCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le titre du sondage est obligatoire")
        @Size(max = 200, groups = OnCreate.class, message = "Le titre ne peut pas dépasser 200 caractères")
        String titre,

        @Size(max = 1000, groups = OnCreate.class, message = "La description ne peut pas dépasser 1000 caractères")
        String description,

        Instant dateFin,

        /** Produits proposés au vote. */
        @Valid
        List<OptionSondageCreateDto> options
) {
}
