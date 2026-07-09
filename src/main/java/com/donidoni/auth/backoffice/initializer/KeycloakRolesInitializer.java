package com.donidoni.auth.backoffice.initializer;

import com.donidoni.auth.config.KeycloakProperties;
import com.donidoni.auth.backoffice.constants.AppRoles;
import com.donidoni.auth.backoffice.constants.RoleDefinition;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initialise et synchronise les rôles applicatifs dans Keycloak au démarrage.
 * <ul>
 *   <li>Rôle absent → créé avec sa description</li>
 *   <li>Rôle présent, description inchangée → ignoré</li>
 *   <li>Rôle présent, description modifiée → mise à jour</li>
 * </ul>
 * Les rôles sont définis dans {@code resources/keycloak/donidoni-core-roles.json}.
 */
@Component
@Order(1)
@Slf4j
public final class KeycloakRolesInitializer implements ApplicationRunner {

    /** Client Keycloak Admin. */
    private final Keycloak keycloak;

    /** Propriétés de configuration Keycloak. */
    private final KeycloakProperties properties;

    /**
     * Constructeur.
     *
     * @param keycloak   le client Keycloak Admin
     * @param properties les propriétés de configuration Keycloak
     */
    public KeycloakRolesInitializer(
        final Keycloak keycloak,
        final KeycloakProperties properties) {
        this.keycloak = keycloak;
        this.properties = properties;
    }

    /**
     * Point d'entrée exécuté au démarrage.
     * Synchronise les rôles du fichier JSON avec Keycloak.
     *
     * @param args les arguments de l'application
     */
    @Override
    public void run(final ApplicationArguments args) {
        log.info("[ROLES] Début de la synchronisation des rôles métier ({} rôles trouvés)", AppRoles.CORE_ROLES.size());
        
        try {
            RealmResource realmResource = keycloak.realm(properties.getRealm());
            RolesResource rolesResource = realmResource.roles();

            AppRoles.CORE_ROLES.forEach(role -> synchronizeRole(rolesResource, role));

            log.info("[ROLES] Synchronisation terminée avec succès");
        } catch (jakarta.ws.rs.ForbiddenException e) {
            log.error("[ROLES] ERREUR DE PERMISSION (403 Forbidden) : Le client '{}' n'a pas les droits nécessaires (ex: realm-admin) pour synchroniser les rôles dans Keycloak. La synchronisation est ignorée.", properties.getClientId());
        } catch (Exception e) {
            log.error("[ROLES] Erreur inattendue lors de la synchronisation des rôles: {}", e.getMessage());
        }
    }

    /**
     * Synchronise un rôle entre le fichier JSON et Keycloak.
     *
     * @param rolesResource la ressource des rôles du realm
     * @param roleDef       la définition du rôle issue du JSON
     */
    private void synchronizeRole(
        final RolesResource rolesResource,
        final RoleDefinition roleDef) {
        try {
            final RoleResource roleResource = rolesResource.get(roleDef.name());
            final RoleRepresentation existing = roleResource.toRepresentation();
            updateDescriptionIfChanged(roleResource, existing, roleDef);
        } catch (NotFoundException e) {
            createRole(rolesResource, roleDef);
        }
    }

    /**
     * Met à jour la description du rôle dans Keycloak si elle a changé.
     *
     * @param roleResource la ressource du rôle existant
     * @param existing     la représentation actuelle dans Keycloak
     * @param roleDef      la définition souhaitée depuis le JSON
     */
    private void updateDescriptionIfChanged(
        final RoleResource roleResource,
        final RoleRepresentation existing,
        final RoleDefinition roleDef) {
        final String currentDescription = existing.getDescription();
        final String expectedDescription = roleDef.description();
        final boolean descriptionChanged = !expectedDescription.equals(
            currentDescription == null ? "" : currentDescription
        );

        if (descriptionChanged) {
            existing.setDescription(expectedDescription);
            roleResource.update(existing);
            log.info("  [UPDATE] {} — description mise à jour : \"{}\" → \"{}\"",
                roleDef.name(), currentDescription, expectedDescription);
        } else {
            log.debug("  [SKIP]   {} — déjà à jour", roleDef.name());
        }
    }

    /**
     * Crée un nouveau rôle dans Keycloak.
     *
     * @param rolesResource la ressource des rôles du realm
     * @param roleDef       la définition du rôle à créer
     */
    private void createRole(
        final RolesResource rolesResource,
        final RoleDefinition roleDef) {
        final RoleRepresentation role = new RoleRepresentation();
        role.setName(roleDef.name());
        role.setDescription(roleDef.description());
        rolesResource.create(role);
        log.info("  [CREATE] {} — {}", roleDef.name(), roleDef.description());
    }
}
