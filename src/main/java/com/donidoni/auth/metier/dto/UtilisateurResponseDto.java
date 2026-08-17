package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.Langue;

import java.time.Instant;

/** Profil utilisateur renvoyé à l'écran « Mon Profil ». */
public record UtilisateurResponseDto(
        Long id,
        String keycloakId,
        String nomComplet,
        String email,
        String telephone,
        String ville,
        String photoUrl,
        Langue langue,
        boolean themeSombre,
        boolean biometrieActivee,
        boolean notificationsActivees,
        boolean actif,
        Instant derniereConnexion,
        Instant createdAt
) {
}
