package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.dto.ProfilUpdateDto;
import com.donidoni.auth.metier.dto.UtilisateurResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper du profil utilisateur.
 *
 * <p>Ne suit pas le contrat {@code EntityMapper} : un utilisateur n'est jamais
 * créé via l'API, il est provisionné à partir du JWT Keycloak.</p>
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UtilisateurMapper {

    UtilisateurResponseDto toResponse(Utilisateur entity);

    void updateProfil(ProfilUpdateDto updateDto, @MappingTarget Utilisateur entity);
}
