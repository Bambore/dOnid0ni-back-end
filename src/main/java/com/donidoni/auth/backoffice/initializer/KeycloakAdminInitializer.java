package com.donidoni.auth.backoffice.initializer;

import com.donidoni.auth.config.KeycloakProperties;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

/**
 * Initialise l'utilisateur administrateur par défaut au démarrage s'il n'existe pas.
 */
@Component
@Order(2)
@Slf4j
public final class KeycloakAdminInitializer implements ApplicationRunner {

    private final Keycloak keycloak;
    private final KeycloakProperties properties;

    @Value("${app.default-admin.email:admin@donidoni.com}")
    private String adminEmail;

    @Value("${app.default-admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.default-admin.firstname:Admin}")
    private String adminFirstName;

    @Value("${app.default-admin.lastname:Doni-Doni}")
    private String adminLastName;

    public KeycloakAdminInitializer(
        final Keycloak keycloak,
        final KeycloakProperties properties) {
        this.keycloak = keycloak;
        this.properties = properties;
    }

    @Override
    public void run(final ApplicationArguments args) {
        log.info("==> Synchronisation de l'administrateur par défaut ({})...", adminEmail);
        final RealmResource realmResource = keycloak.realm(properties.getRealm());
        final UsersResource usersResource = realmResource.users();

        final List<UserRepresentation> existingUsers = usersResource.searchByEmail(adminEmail, true);

        if (existingUsers.isEmpty()) {
            log.info("  [CREATE] L'administrateur par défaut n'existe pas. Création en cours...");
            createAdminUser(realmResource, usersResource);
        } else {
            log.info("  [SKIP] L'administrateur par défaut existe déjà.");
        }
        log.info("==> Synchronisation de l'administrateur terminée.");
    }

    private void createAdminUser(final RealmResource realmResource, final UsersResource usersResource) {
        // 1. Créer l'utilisateur
        final UserRepresentation user = new UserRepresentation();
        user.setEmail(adminEmail);
        user.setUsername(adminEmail);
        user.setFirstName(adminFirstName);
        user.setLastName(adminLastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                final String userId = extractCreatedId(response);
                
                // 2. Définir le mot de passe
                final CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(adminPassword);
                
                usersResource.get(userId).resetPassword(passwordCred);

                // 3. Assigner le rôle ADMIN
                try {
                    final RoleRepresentation adminRole = realmResource.roles().get("ADMIN").toRepresentation();
                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(adminRole));
                    log.info("  [SUCCESS] Rôle ADMIN assigné à {}", adminEmail);
                } catch (Exception e) {
                    log.warn("  [WARNING] Impossible d'assigner le rôle ADMIN (le rôle n'existe peut-être pas encore).", e);
                }
            } else {
                log.error("  [ERROR] Échec de la création de l'administrateur. Status HTTP: {}", response.getStatus());
            }
        }
    }

    private String extractCreatedId(final Response response) {
        final String location = response.getHeaderString("Location");
        if (location == null || !location.contains("/")) {
            throw new IllegalStateException("Header Location manquant dans la réponse Keycloak");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
