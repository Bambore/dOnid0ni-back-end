package com.donidoni.auth.backoffice.service;

import com.donidoni.auth.backoffice.constants.DoniDoniDefaultRoles;
import com.donidoni.auth.backoffice.dto.PageResponse;
import com.donidoni.auth.backoffice.dto.RoleFormDto;
import com.donidoni.auth.backoffice.dto.RoleWebDto;
import com.donidoni.auth.backoffice.mapper.BackofficeKeycloakMapper;
import com.donidoni.auth.exception.AuthException;
import com.donidoni.auth.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Service de gestion des rôles Keycloak pour le backoffice.
 *
 * <p>La création et la suppression de rôles ne sont pas exposées via l'API.
 * Les rôles sont gérés par {@code KeycloakRolesInitializer} au démarrage.</p>
 *
 * <p>Adapté du {@code KeycloakRoleService} de sigatt-uaa-service.</p>
 */
@Service
@Slf4j
public class BackofficeRoleService {

    private final RealmResource realmResource;
    private final BackofficeKeycloakMapper mapper;

    /**
     * Constructeur.
     *
     * @param realmResource la ressource du realm Keycloak
     * @param mapper        le mapper des rôles
     */
    public BackofficeRoleService(
            final RealmResource realmResource,
            final BackofficeKeycloakMapper mapper) {
        this.realmResource = realmResource;
        this.mapper = mapper;
    }

    /**
     * Retourne une page de rôles applicatifs (hors rôles système).
     * La pagination est effectuée en mémoire car Keycloak ne pagine pas les rôles.
     *
     * @param search filtre textuel
     * @param page   numéro de page
     * @param size   taille de la page
     * @return la page de résultats
     */
    public PageResponse<RoleWebDto> findAllPaged(
            final String search, final int page, final int size) {
        final List<RoleWebDto> all = fetchFilteredRoles(search);
        final int first = page * size;
        final int to = Math.min(first + size, all.size());
        final List<RoleWebDto> items = first >= all.size()
                ? List.of()
                : all.subList(first, to);
        final int totalPages = size > 0 ? (int) Math.ceil((double) all.size() / size) : 0;
        return PageResponse.<RoleWebDto>builder()
                .content(items)
                .totalElements(all.size())
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Trouve un rôle par son nom.
     *
     * @param roleName le nom du rôle
     * @return le DTO du rôle
     */
    public RoleWebDto findRoleByName(final String roleName) {
        try {
            final RoleRepresentation role = realmResource.roles()
                    .get(roleName)
                    .toRepresentation();
            return mapper.toDto(role);
        } catch (jakarta.ws.rs.NotFoundException ex) {
            throw new AuthException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Rôle introuvable : " + roleName);
        }
    }

    /**
     * Met à jour la description d'un rôle par son nom.
     *
     * @param roleName le nom du rôle
     * @param form     le formulaire de mise à jour
     * @return le rôle mis à jour
     */
    public RoleWebDto updateRole(final String roleName, final RoleFormDto form) {
        try {
            final RoleRepresentation role = realmResource.roles()
                    .get(roleName)
                    .toRepresentation();
            role.setDescription(form.getDescription());
            realmResource.roles().get(roleName).update(role);
            log.info("[UPDATE] Rôle mis à jour : {}", roleName);
            return findRoleByName(roleName);
        } catch (jakarta.ws.rs.NotFoundException ex) {
            throw new AuthException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Rôle introuvable : " + roleName);
        }
    }

    /**
     * Récupère les rôles filtrés et triés (hors rôles système).
     *
     * @param search filtre textuel (peut être null)
     * @return la liste triée des rôles applicatifs
     */
    private List<RoleWebDto> fetchFilteredRoles(final String search) {
        return realmResource.roles()
                .list(search, false)
                .stream()
                .filter(role -> !DoniDoniDefaultRoles.isDefaultRole(role))
                .map(mapper::toDto)
                .sorted(Comparator.comparing(RoleWebDto::getName))
                .toList();
    }
}
