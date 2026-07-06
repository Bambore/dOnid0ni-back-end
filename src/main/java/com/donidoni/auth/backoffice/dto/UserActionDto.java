package com.donidoni.auth.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO d'action portant uniquement l'identifiant Keycloak d'un utilisateur.
 *
 * <p>Utilisé pour les opérations POST qui ciblent un utilisateur existant sans
 * modifier ses données (renvoi de mail d'activation, réinitialisation du
 * mot de passe, etc.).</p>
 *
 * @param userId l'identifiant UUID Keycloak de l'utilisateur ; ne doit pas être {@code null}
 */
public record UserActionDto(
    @JsonProperty("id") String userId) {
}
