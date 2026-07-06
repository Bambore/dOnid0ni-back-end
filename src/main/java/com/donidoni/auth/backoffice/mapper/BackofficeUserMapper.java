package com.donidoni.auth.backoffice.mapper;

import com.donidoni.auth.backoffice.dto.BackofficeUserFormDto;
import com.donidoni.auth.backoffice.dto.BackofficeUserWebDto;
import com.donidoni.auth.backoffice.dto.RoleWebDto;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.AfterMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper MapStruct entre les représentations Keycloak et les DTOs utilisateur backoffice.
 *
 * <p>Adapté du {@code KeycloakUserMapper} de sigatt-uaa-service,
 * sans les mappings directionId, serviceId, fonctionId, siteId,
 * matricule et typeUtilisateur.</p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BackofficeUserMapper {

    /**
     * Convertit un {@link BackofficeUserFormDto} en {@link UserRepresentation} Keycloak.
     *
     * @param form le formulaire de création
     * @return la représentation Keycloak prête à être envoyée
     */
    @Mapping(target = "username", source = "email")
    @Mapping(target = "firstName", source = "prenom")
    @Mapping(target = "lastName", source = "nom")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "enabled", constant = "false")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTimestamp", ignore = true)
    @Mapping(target = "totp", ignore = true)
    @Mapping(target = "credentials", ignore = true)
    @Mapping(target = "requiredActions", ignore = true)
    @Mapping(target = "federatedIdentities", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "realmRoles", ignore = true)
    @Mapping(target = "clientRoles", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "access", ignore = true)
    @Mapping(target = "self", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "federationLink", ignore = true)
    @Mapping(target = "serviceAccountClientId", ignore = true)
    @Mapping(target = "applicationRoles", ignore = true)
    @Mapping(target = "disableableCredentialTypes", ignore = true)
    @Mapping(target = "notBefore", ignore = true)
    @Mapping(target = "userProfileMetadata", ignore = true)
    UserRepresentation toRepresentation(BackofficeUserFormDto form);

    /**
     * Injecte les attributs métier après le mapping de création.
     *
     * @param form   le formulaire source
     * @param target la représentation en cours de construction
     */
    @AfterMapping
    default void afterMapping(
            final BackofficeUserFormDto form,
            @MappingTarget final UserRepresentation target) {
        final Map<String, List<String>> attributes = target.getAttributes() != null
                ? new HashMap<>(target.getAttributes())
                : new HashMap<>();
        putAttr(attributes, "telephone", form.getTelephone());
        target.setAttributes(attributes);
    }

    /**
     * Met à jour une {@link UserRepresentation} existante.
     *
     * @param form     les nouvelles valeurs
     * @param existing la représentation existante à modifier in-place
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "username", source = "email")
    @Mapping(target = "firstName", source = "prenom")
    @Mapping(target = "lastName", source = "nom")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTimestamp", ignore = true)
    @Mapping(target = "totp", ignore = true)
    @Mapping(target = "credentials", ignore = true)
    @Mapping(target = "requiredActions", ignore = true)
    @Mapping(target = "federatedIdentities", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "realmRoles", ignore = true)
    @Mapping(target = "clientRoles", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "access", ignore = true)
    @Mapping(target = "self", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "federationLink", ignore = true)
    @Mapping(target = "serviceAccountClientId", ignore = true)
    @Mapping(target = "applicationRoles", ignore = true)
    @Mapping(target = "disableableCredentialTypes", ignore = true)
    @Mapping(target = "notBefore", ignore = true)
    @Mapping(target = "userProfileMetadata", ignore = true)
    void updateRepresentation(BackofficeUserFormDto form, @MappingTarget UserRepresentation existing);

    /**
     * Convertit une {@link UserRepresentation} en {@link BackofficeUserWebDto} avec profil.
     *
     * @param user   la représentation Keycloak
     * @param groups les groupes Keycloak assignés à l'utilisateur
     * @return le DTO utilisateur avec profil, sans rôles
     */
    @Mapping(target = "nom", source = "user.lastName")
    @Mapping(target = "prenom", source = "user.firstName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "enabled", source = "user.enabled")
    @Mapping(target = "emailVerified", source = "user.emailVerified")
    @Mapping(target = "telephone", expression = "java(getAttr(user, \"telephone\"))")
    @Mapping(target = "profilId", expression = "java(getFirstGroupId(groups))")
    @Mapping(target = "profileName", expression = "java(getFirstGroupName(groups))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    BackofficeUserWebDto toDto(UserRepresentation user, @Context List<GroupRepresentation> groups);

    /**
     * Construit un DTO complet avec profil, rôles et identifiants de rôles.
     *
     * @param user        la représentation Keycloak
     * @param groups      les groupes Keycloak assignés
     * @param mergedRoles les rôles filtrés
     * @return le DTO complet
     */
    default BackofficeUserWebDto toDtoWithRoles(
            final UserRepresentation user,
            final List<GroupRepresentation> groups,
            final List<RoleWebDto> mergedRoles) {
        final BackofficeUserWebDto dto = toDto(user, groups);
        final List<RoleWebDto> safeRoles = mergedRoles != null ? mergedRoles : List.of();
        dto.setRoles(safeRoles);
        dto.setRoleIds(
                safeRoles.stream()
                        .map(RoleWebDto::getId)
                        .toList());
        return dto;
    }

    /**
     * Insère un attribut Keycloak dans la map uniquement si la valeur est non nulle et non vide.
     *
     * @param attributes la map cible des attributs Keycloak
     * @param key        la clé de l'attribut
     * @param value      la valeur à insérer ; ignorée si {@code null} ou vide
     */
    default void putAttr(
            final Map<String, List<String>> attributes,
            final String key,
            final String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(key, Collections.singletonList(value));
        } else {
            attributes.remove(key);
        }
    }

    /**
     * Extrait la première valeur d'un attribut Keycloak.
     *
     * @param user la représentation Keycloak
     * @param key  la clé de l'attribut
     * @return la valeur ou {@code null} si absente
     */
    @Named("getAttr")
    default String getAttr(final UserRepresentation user, final String key) {
        if (user.getAttributes() == null) {
            return null;
        }
        final List<String> values = user.getAttributes().get(key);
        return (values != null && !values.isEmpty()) ? values.getFirst() : null;
    }

    /**
     * Retourne l'identifiant du premier groupe assigné.
     *
     * @param groups la liste des groupes Keycloak
     * @return l'identifiant du premier groupe ou {@code null}
     */
    default String getFirstGroupId(final List<GroupRepresentation> groups) {
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        return groups.getFirst().getId();
    }

    /**
     * Retourne le nom du premier groupe assigné.
     *
     * @param groups la liste des groupes Keycloak
     * @return le nom du premier groupe ou {@code null}
     */
    default String getFirstGroupName(final List<GroupRepresentation> groups) {
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        return groups.getFirst().getName();
    }
}
