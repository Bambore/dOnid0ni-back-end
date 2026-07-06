package com.donidoni.auth.backoffice.constants;

import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;

/**
 * Constantes pour les rôles système Keycloak à ignorer dans les listings.
 */
public final class DoniDoniDefaultRoles {

    /** Rôles internes Keycloak par défaut. */
    public static final List<String> INTERNAL_ROLES = List.of(
            "default-roles-doni-doni",
            "offline_access",
            "uma_authorization"
    );

    /**
     * Indique si un rôle est un rôle système Keycloak.
     *
     * @param role la représentation du rôle
     * @return {@code true} si le rôle est un rôle interne Keycloak,
     *         {@code false} sinon
     */
    public static boolean isDefaultRole(final RoleRepresentation role) {
        if (role == null || role.getName() == null) {
            return true;
        }
        return INTERNAL_ROLES.contains(role.getName())
            || role.getName().startsWith("default-roles-");
    }

    private DoniDoniDefaultRoles() {
    }
}
