package com.donidoni.auth.backoffice.service;

import com.donidoni.auth.backoffice.dto.PageResponse;
import com.donidoni.auth.backoffice.dto.ProfileFormDto;
import com.donidoni.auth.backoffice.dto.ProfileWebDto;
import com.donidoni.auth.backoffice.mapper.BackofficeKeycloakMapper;
import com.donidoni.auth.exception.AuthException;
import com.donidoni.auth.exception.ErrorCode;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des profils (groupes Keycloak) pour le backoffice.
 *
 * <p>Adapté du {@code KeycloakProfileService} de sigatt-uaa-service.</p>
 */
@Service
@Slf4j
public class BackofficeProfileService {

    private final RealmResource realmResource;
    private final BackofficeKeycloakMapper mapper;

    /**
     * Constructeur.
     *
     * @param realmResource la ressource du realm Keycloak
     * @param mapper        le mapper des profils
     */
    public BackofficeProfileService(
            final RealmResource realmResource,
            final BackofficeKeycloakMapper mapper) {
        this.realmResource = realmResource;
        this.mapper = mapper;
    }

    /**
     * Retourne une page de profils.
     *
     * @param search filtre textuel
     * @param page   numéro de page
     * @param size   taille de la page
     * @return la page de résultats
     */
    public PageResponse<ProfileWebDto> findAllPaged(
            final String search, final int page, final int size) {
        final int first = page * size;
        final List<ProfileWebDto> items = realmResource.groups()
                .groups(search != null ? search : "", first, size)
                .stream()
                .map(mapper::toDto)
                .toList();
        final long total = realmResource.groups()
                .count(search != null ? search : "")
                .getOrDefault("count", 0L);
        final int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return PageResponse.<ProfileWebDto>builder()
                .content(items)
                .totalElements(total)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Retourne un profil par son identifiant avec ses rôles.
     *
     * @param id l'identifiant Keycloak du groupe
     * @return le DTO du profil avec ses rôles
     */
    public ProfileWebDto findById(final String id) {
        try {
            final GroupRepresentation group = realmResource.groups()
                    .group(id)
                    .toRepresentation();
            final List<RoleRepresentation> roles = realmResource.groups()
                    .group(id)
                    .roles()
                    .realmLevel()
                    .listAll();
            return mapper.toDtoWithRoles(group, roles);
        } catch (jakarta.ws.rs.NotFoundException ex) {
            throw new AuthException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Profil introuvable : " + id);
        }
    }

    /**
     * Crée un profil (groupe Keycloak) avec ses rôles associés.
     *
     * @param form le formulaire de création
     * @return le DTO du profil créé
     */
    public ProfileWebDto create(final ProfileFormDto form) {
        final GroupRepresentation group = new GroupRepresentation();
        group.setName(form.getName());

        try (Response response = realmResource.groups().add(group)) {
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                throw new AuthException(ErrorCode.CONFLICT,
                        "Un profil avec le nom '" + form.getName() + "' existe déjà");
            }
            if (response.getStatus() != HttpStatus.CREATED.value()) {
                throw new AuthException(ErrorCode.USER_CREATION_FAILED,
                        "Erreur Keycloak lors de la création du profil : HTTP " + response.getStatus());
            }
            final String groupId = CreatedResponseUtil.getCreatedId(response);
            if (form.getRoleIds() != null && !form.getRoleIds().isEmpty()) {
                assignRolesToGroup(groupId, form.getRoleIds());
            }
            log.info("[CREATE] Profil créé : {} ({})", form.getName(), groupId);
            return findById(groupId);
        }
    }

    /**
     * Met à jour un profil et remplace ses rôles.
     *
     * @param id   l'identifiant Keycloak du groupe
     * @param form le formulaire de mise à jour
     * @return le DTO du profil mis à jour
     */
    public ProfileWebDto update(final String id, final ProfileFormDto form) {
        try {
            final GroupResource groupResource = realmResource.groups().group(id);
            final GroupRepresentation group = groupResource.toRepresentation();
            group.setName(form.getName());
            groupResource.update(group);

            if (form.getRoleIds() != null) {
                final List<RoleRepresentation> existingRoles =
                        groupResource.roles().realmLevel().listAll();
                if (!existingRoles.isEmpty()) {
                    groupResource.roles().realmLevel().remove(existingRoles);
                }
                if (!form.getRoleIds().isEmpty()) {
                    assignRolesToGroup(id, form.getRoleIds());
                }
            }

            log.info("[UPDATE] Profil mis à jour : {}", id);
            return findById(id);
        } catch (jakarta.ws.rs.NotFoundException ex) {
            throw new AuthException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Profil introuvable : " + id);
        } catch (jakarta.ws.rs.ClientErrorException ex) {
            if (ex.getResponse().getStatus() == HttpStatus.CONFLICT.value()) {
                throw new AuthException(ErrorCode.CONFLICT,
                        "Un profil avec le nom '" + form.getName() + "' existe déjà");
            }
            throw ex;
        }
    }

    /**
     * Supprime un profil (groupe Keycloak).
     * Refuse la suppression si le profil contient encore des membres.
     *
     * @param id l'identifiant Keycloak du groupe
     */
    public void deleteById(final String id) {
        try {
            final GroupResource groupResource = realmResource.groups().group(id);
            final List<UserRepresentation> members = groupResource.members();
            if (!members.isEmpty()) {
                throw new AuthException(ErrorCode.VALIDATION_ERROR,
                        "Impossible de supprimer le profil : il contient encore "
                                + members.size() + " membre(s)");
            }
            groupResource.remove();
            log.info("[DELETE] Profil supprimé : {}", id);
        } catch (jakarta.ws.rs.NotFoundException ex) {
            throw new AuthException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Profil introuvable : " + id);
        }
    }

    /**
     * Assigne une liste de rôles à un groupe par leurs identifiants.
     *
     * @param groupId l'identifiant du groupe
     * @param roleIds la liste des identifiants de rôles
     */
    private void assignRolesToGroup(
            final String groupId, final List<String> roleIds) {
        final List<RoleRepresentation> roles = roleIds.stream()
                .map(id -> realmResource.rolesById().getRole(id))
                .collect(Collectors.toList());
        realmResource.groups()
                .group(groupId)
                .roles()
                .realmLevel()
                .add(roles);
    }
}
