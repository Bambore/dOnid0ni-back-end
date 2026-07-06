package com.donidoni.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

/**
 * Informations utilisateur retournées dans la réponse d'authentification.
 *
 * @param id        identifiant Keycloak (UUID)
 * @param email     adresse email (nullable pour les connexions par téléphone)
 * @param phone     numéro de téléphone (nullable pour les connexions Google)
 * @param firstName prénom
 * @param lastName  nom de famille
 * @param roles     liste des rôles assignés
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfo(
        String id,
        String email,
        String phone,
        String firstName,
        String lastName,
        List<String> roles
) {
}
