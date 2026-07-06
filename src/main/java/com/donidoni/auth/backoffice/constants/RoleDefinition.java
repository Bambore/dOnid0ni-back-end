package com.donidoni.auth.backoffice.constants;

/**
 * Représente la définition d'un rôle applicatif chargée depuis le fichier JSON.
 *
 * @param name        le nom du rôle
 * @param description la description fonctionnelle du rôle
 */
public record RoleDefinition(String name, String description) {
}
