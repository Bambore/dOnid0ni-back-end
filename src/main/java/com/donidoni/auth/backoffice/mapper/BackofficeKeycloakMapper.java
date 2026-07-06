package com.donidoni.auth.backoffice.mapper;

import com.donidoni.auth.backoffice.dto.ProfileWebDto;
import com.donidoni.auth.backoffice.dto.RoleWebDto;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper MapStruct pour les représentations Keycloak génériques (rôles et profils).
 */
@Mapper(componentModel = "spring")
public interface BackofficeKeycloakMapper {

    /**
     * Convertit un {@link RoleRepresentation} en {@link RoleWebDto}.
     *
     * @param role la représentation Keycloak du rôle
     * @return le DTO du rôle
     */
    RoleWebDto toDto(RoleRepresentation role);

    /**
     * Convertit un {@link GroupRepresentation} en {@link ProfileWebDto} sans les rôles.
     *
     * @param group la représentation Keycloak du groupe
     * @return le DTO du profil sans rôles
     */
    @Mapping(target = "roles", ignore = true)
    ProfileWebDto toDto(GroupRepresentation group);

    /**
     * Convertit un {@link GroupRepresentation} et une liste de rôles en {@link ProfileWebDto}.
     *
     * @param group la représentation Keycloak du groupe
     * @param roles la liste des rôles associés
     * @return le DTO du profil avec ses rôles
     */
    @Mapping(target = "id", source = "group.id")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "roles", source = "roles")
    ProfileWebDto toDtoWithRoles(GroupRepresentation group, List<RoleRepresentation> roles);
}
