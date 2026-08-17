package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.Langue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Mise à jour du profil par son titulaire — écrans « Informations personnelles »
 * et réglages (langue, thème sombre, biométrie, notifications).
 *
 * <p>Seuls les champs non nuls sont appliqués.</p>
 */
public record ProfilUpdateDto(

        @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
        String nomComplet,

        @Email(message = "L'adresse e-mail est invalide")
        @Size(max = 150, message = "L'e-mail ne peut pas dépasser 150 caractères")
        String email,

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international (ex. +22670000000)")
        String telephone,

        @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
        String ville,

        @Size(max = 500, message = "L'URL de la photo ne peut pas dépasser 500 caractères")
        String photoUrl,

        Langue langue,

        Boolean themeSombre,

        Boolean biometrieActivee,

        Boolean notificationsActivees
) {
}
