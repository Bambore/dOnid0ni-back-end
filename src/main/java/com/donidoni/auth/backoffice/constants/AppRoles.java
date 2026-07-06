package com.donidoni.auth.backoffice.constants;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Source de vérité des rôles applicatifs Doni-Doni.
 * Les rôles sont définis dans le fichier JSON sous {@code resources/keycloak/}.
 */
@Slf4j
public final class AppRoles {

    /** Fichier de définition des rôles dans les resources. */
    private static final List<String> ROLES_FILES = List.of(
        "/keycloak/donidoni-core-roles.json"
    );

    /** Liste des rôles chargée une seule fois au démarrage. */
    public static final List<RoleDefinition> CORE_ROLES = loadRoles();

    private AppRoles() {
    }

    /**
     * Vérifie si un rôle est un rôle core immuable de l'application.
     *
     * @param roleName le nom du rôle à vérifier
     * @return {@code true} si le rôle est un rôle core
     */
    public static boolean isCoreRole(final String roleName) {
        return CORE_ROLES.stream()
            .anyMatch(r -> r.name().equals(roleName));
    }

    /**
     * Charge les rôles depuis les fichiers JSON au démarrage de la JVM.
     *
     * @return la liste immuable des définitions de rôles
     */
    private static List<RoleDefinition> loadRoles() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final List<RoleDefinition> allRoles = new ArrayList<>();

        for (String rolesFile : ROLES_FILES) {
            try (InputStream is = AppRoles.class.getResourceAsStream(rolesFile)) {
                if (is == null) {
                    throw new IllegalStateException(
                        "Fichier de rôles introuvable : " + rolesFile
                    );
                }
                final List<RoleDefinition> roles = objectMapper.readValue(
                    is,
                    new TypeReference<>() { }
                );
                log.info("AppRoles : {} rôles chargés depuis {}", roles.size(), rolesFile);
                allRoles.addAll(roles);
            } catch (IOException e) {
                throw new IllegalStateException(
                    "Impossible de charger les rôles depuis " + rolesFile, e
                );
            }
        }

        return List.copyOf(allRoles);
    }
}
