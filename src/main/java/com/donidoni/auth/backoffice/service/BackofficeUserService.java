package com.donidoni.auth.backoffice.service;

import com.donidoni.auth.backoffice.constants.DoniDoniDefaultRoles;
import com.donidoni.auth.backoffice.dto.BackofficeUserFormDto;
import com.donidoni.auth.backoffice.dto.BackofficeUserWebDto;
import com.donidoni.auth.backoffice.dto.PageResponse;
import com.donidoni.auth.backoffice.dto.RoleWebDto;
import com.donidoni.auth.backoffice.dto.UserStatsDto;
import com.donidoni.auth.backoffice.mapper.BackofficeKeycloakMapper;
import com.donidoni.auth.backoffice.mapper.BackofficeUserMapper;
import com.donidoni.auth.exception.AuthException;
import com.donidoni.auth.exception.ErrorCode;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service de gestion des comptes utilisateurs backoffice via Keycloak Admin API.
 *
 * <p>Adapté du {@code KeycloakUserService} de sigatt-uaa-service,
 * sans les champs spécifiques à SIGATT (direction, service, fonction, site,
 * matricule, typeUtilisateur) et sans Feign clients.</p>
 */
@Service
@Slf4j
public class BackofficeUserService {

    private final RealmResource realmResource;
    private final BackofficeUserMapper mapper;
    private final BackofficeKeycloakMapper keycloakMapper;

    /**
     * Constructeur.
     *
     * @param realmResource  la ressource du realm Keycloak
     * @param mapper         le mapper utilisateur
     * @param keycloakMapper le mapper générique (rôles)
     */
    public BackofficeUserService(
            final RealmResource realmResource,
            final BackofficeUserMapper mapper,
            final BackofficeKeycloakMapper keycloakMapper) {
        this.realmResource = realmResource;
        this.mapper = mapper;
        this.keycloakMapper = keycloakMapper;
    }

    // ═══════════════════════════════════════════════════════════
    //  PAGINATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Retourne une page d'utilisateurs backoffice.
     *
     * @param search filtre textuel (peut être vide ou {@code null})
     * @param page   numéro de page, commence à {@code 0}
     * @param size   nombre d'éléments par page
     * @return un {@link PageResponse} contenant les utilisateurs et les métadonnées de pagination
     */
    public PageResponse<BackofficeUserWebDto> findAllPaged(
            final String search, final int page, final int size) {
        final int first = page * size;
        final List<BackofficeUserWebDto> items = realmResource.users()
                .search(search, first, size, false)
                .stream()
                .map(user -> {
                    final List<GroupRepresentation> groups = realmResource.users()
                            .get(user.getId())
                            .groups();
                    return mapper.toDto(user, groups);
                })
                .toList();
        final long total = realmResource.users().count(search);
        final int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return PageResponse.<BackofficeUserWebDto>builder()
                .content(items)
                .totalElements(total)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  CRUD
    // ═══════════════════════════════════════════════════════════

    /**
     * Retourne un utilisateur par son identifiant Keycloak avec son profil et ses rôles.
     *
     * @param id l'identifiant Keycloak de l'utilisateur
     * @return le DTO de l'utilisateur avec profil et rôles
     */
    public BackofficeUserWebDto findById(final String id) {
        try {
            final UserResource userResource = realmResource.users().get(id);
            final UserRepresentation user = userResource.toRepresentation();
            final List<GroupRepresentation> groups = userResource.groups();
            final List<RoleRepresentation> realmRoles = userResource
                    .roles()
                    .realmLevel()
                    .listAll();
            final List<RoleWebDto> mappedRoles = filterAndMapRoles(realmRoles);
            return mapper.toDtoWithRoles(user, groups, mappedRoles);
        } catch (NotFoundException e) {
            throw new AuthException(ErrorCode.USER_NOT_FOUND,
                    "Utilisateur introuvable : " + id);
        }
    }

    /**
     * Crée un compte utilisateur backoffice dans Keycloak.
     *
     * @param form les informations de l'utilisateur
     * @return le DTO de l'utilisateur créé
     */
    public BackofficeUserWebDto create(final BackofficeUserFormDto form) {
        guardDuplicateEmail(form.getEmail(), null);
        final UserRepresentation user = mapper.toRepresentation(form);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setEmail(form.getEmail());
        user.setRequiredActions(List.of("UPDATE_PASSWORD", "VERIFY_EMAIL"));

        try (Response response = realmResource.users().create(user)) {
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                throw new AuthException(ErrorCode.CONFLICT,
                        "Un utilisateur avec l'email " + form.getEmail() + " existe déjà");
            }
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw new AuthException(ErrorCode.USER_CREATION_FAILED,
                        "Erreur Keycloak lors de la création : HTTP " + response.getStatus());
            }
            final String userId = extractCreatedId(response);
            final UserResource userResource = realmResource.users().get(userId);
            final UserRepresentation createdUser = userResource.toRepresentation();
            createdUser.setAttributes(user.getAttributes());
            userResource.update(createdUser);

            assignProfile(userId, form.getProfilId());
            if (form.getRoleIds() != null && !form.getRoleIds().isEmpty()) {
                assignRoles(userId, form.getRoleIds());
            }
            trySendActivationEmail(userId, form.getEmail());

            log.info("[CREATE] Utilisateur backoffice créé : {} {} ({})",
                    form.getPrenom(), form.getNom(), userId);
            return findById(userId);
        }
    }

    /**
     * Met à jour les informations d'un utilisateur existant.
     *
     * @param id   l'identifiant Keycloak de l'utilisateur
     * @param form les nouvelles informations
     * @return le DTO de l'utilisateur mis à jour
     */
    public BackofficeUserWebDto update(final String id, final BackofficeUserFormDto form) {
        guardDuplicateEmail(form.getEmail(), id);
        final UserResource userResource = getUserResourceOrThrow(id);
        final UserRepresentation existing = userResource.toRepresentation();
        mapper.updateRepresentation(form, existing);
        // Mettre à jour les attributs manuellement
        final var attributes = existing.getAttributes() != null
                ? new java.util.HashMap<>(existing.getAttributes())
                : new java.util.HashMap<String, List<String>>();
        mapper.putAttr(attributes, "telephone", form.getTelephone());
        existing.setAttributes(attributes);
        userResource.update(existing);

        replaceProfile(id, form.getProfilId());
        replaceRoles(id, form.getRoleIds() != null ? form.getRoleIds() : List.of());

        log.info("[UPDATE] Utilisateur backoffice mis à jour : {}", id);
        return findById(id);
    }

    /**
     * Supprime définitivement un compte utilisateur.
     *
     * @param id l'identifiant Keycloak de l'utilisateur
     */
    public void deleteById(final String id) {
        try {
            realmResource.users().get(id).remove();
            log.info("[DELETE] Utilisateur backoffice supprimé : {}", id);
        } catch (NotFoundException e) {
            throw new AuthException(ErrorCode.USER_NOT_FOUND,
                    "Utilisateur introuvable : " + id);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ACTIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Active ou désactive un compte utilisateur en inversant son état actuel.
     *
     * @param id l'identifiant Keycloak de l'utilisateur
     * @return le DTO de l'utilisateur mis à jour
     */
    public BackofficeUserWebDto toggleEnabled(final String id) {
        final UserResource userResource = getUserResourceOrThrow(id);
        final UserRepresentation user = userResource.toRepresentation();
        final boolean newStatus = !Boolean.TRUE.equals(user.isEnabled());
        user.setEnabled(newStatus);
        userResource.update(user);
        log.info("[{}] Utilisateur backoffice : {}",
                newStatus ? "ENABLE" : "DISABLE", id);
        return findById(id);
    }

    /**
     * Renvoie le mail d'activation à un utilisateur.
     *
     * @param id l'identifiant Keycloak de l'utilisateur
     */
    public void resendActivationEmail(final String id) {
        getUserResourceOrThrow(id);
        sendActivationEmail(id);
        log.info("[RESEND] Mail d'activation renvoyé : {}", id);
    }

    /**
     * Envoie un mail de réinitialisation du mot de passe.
     *
     * @param id l'identifiant Keycloak de l'utilisateur
     */
    public void sendResetPasswordEmail(final String id) {
        getUserResourceOrThrow(id);
        realmResource.users()
                .get(id)
                .executeActionsEmail(List.of("UPDATE_PASSWORD"));
        log.info("[RESET_PASSWORD] Mail envoyé : {}", id);
    }

    // ═══════════════════════════════════════════════════════════
    //  STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Retourne les statistiques globales des comptes utilisateurs backoffice.
     *
     * @return le DTO {@link UserStatsDto} avec les trois compteurs
     */
    public UserStatsDto getStats() {
        final int batchSize = 100;
        long active = 0;
        long inactive = 0;
        int first = 0;
        List<UserRepresentation> batch;
        do {
            batch = realmResource.users().search(null, first, batchSize, false);
            for (final UserRepresentation user : batch) {
                if (isActive(user)) {
                    active++;
                } else {
                    inactive++;
                }
            }
            first += batchSize;
        } while (batch.size() == batchSize);
        log.info("[STATS] Utilisateurs — total={}, actifs={}, inactifs={}",
                active + inactive, active, inactive);
        return UserStatsDto.of(active, inactive);
    }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private boolean isActive(final UserRepresentation user) {
        if (!Boolean.TRUE.equals(user.isEnabled())) {
            return false;
        }
        final List<String> requiredActions = user.getRequiredActions();
        return requiredActions == null || !requiredActions.contains("UPDATE_PASSWORD");
    }

    private void trySendActivationEmail(final String userId, final String email) {
        try {
            sendActivationEmail(userId);
            log.info("[MAIL] Mail d'activation envoyé → {}", email);
        } catch (Exception e) {
            log.warn("[MAIL] Échec envoi mail d'activation pour {} : {} — "
                    + "utilisez POST /resend-activation après configuration SMTP",
                    email, e.getMessage());
        }
    }

    private void sendActivationEmail(final String userId) {
        realmResource.users()
                .get(userId)
                .executeActionsEmail(List.of("UPDATE_PASSWORD", "VERIFY_EMAIL"));
    }

    private void assignProfile(final String userId, final String profileId) {
        try {
            realmResource.groups().group(profileId).toRepresentation();
            realmResource.users().get(userId).joinGroup(profileId);
        } catch (NotFoundException e) {
            throw new AuthException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Profil introuvable : " + profileId);
        }
    }

    private void assignRoles(final String userId, final List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        final List<RoleRepresentation> roles = roleIds.stream()
                .map(id -> realmResource.rolesById().getRole(id))
                .toList();
        realmResource.users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(roles);
    }

    private void replaceProfile(final String userId, final String newProfileId) {
        realmResource.users().get(userId).groups()
                .forEach(g -> realmResource.users().get(userId).leaveGroup(g.getId()));
        assignProfile(userId, newProfileId);
    }

    private void replaceRoles(final String userId, final List<String> roleIds) {
        final UserResource userResource = realmResource.users().get(userId);
        final List<RoleRepresentation> currentRoles =
                userResource.roles().realmLevel().listAll();
        if (!currentRoles.isEmpty()) {
            userResource.roles().realmLevel().remove(currentRoles);
        }
        assignRoles(userId, roleIds);
    }

    private List<RoleWebDto> filterAndMapRoles(final List<RoleRepresentation> realmRoles) {
        return realmRoles.stream()
                .filter(r -> !DoniDoniDefaultRoles.isDefaultRole(r))
                .map(keycloakMapper::toDto)
                .toList();
    }

    private UserResource getUserResourceOrThrow(final String id) {
        try {
            final UserResource resource = realmResource.users().get(id);
            resource.toRepresentation();
            return resource;
        } catch (NotFoundException e) {
            throw new AuthException(ErrorCode.USER_NOT_FOUND,
                    "Utilisateur introuvable : " + id);
        }
    }

    private void guardDuplicateEmail(final String email, final String excludeId) {
        realmResource.users()
                .searchByEmail(email, true)
                .stream()
                .filter(u -> excludeId == null || !u.getId().equals(excludeId))
                .findFirst()
                .ifPresent(u -> {
                    throw new AuthException(ErrorCode.CONFLICT,
                            "Un utilisateur avec l'email " + email + " existe déjà");
                });
    }

    private String extractCreatedId(final Response response) {
        final String location = response.getHeaderString("Location");
        if (location == null || !location.contains("/")) {
            throw new AuthException(ErrorCode.USER_CREATION_FAILED,
                    "Header Location manquant dans la réponse Keycloak");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
