package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.TypeNotification;

import java.time.Instant;

/**
 * Notification renvoyée au mobile.
 *
 * <p>Les deux langues sont transmises : le mobile sélectionne la variante
 * correspondant à la locale active.</p>
 */
public record NotificationResponseDto(
        Long id,
        Long utilisateurId,
        TypeNotification type,
        String titreFr,
        String titreEn,
        String corpsFr,
        String corpsEn,
        boolean lue,
        Instant dateLecture,
        String lienAction,
        Instant createdAt
) {
}
