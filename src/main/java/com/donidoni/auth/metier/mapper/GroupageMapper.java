package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Groupage;
import com.donidoni.auth.metier.domain.ParticipationGroupage;
import com.donidoni.auth.metier.dto.GroupageCreateDto;
import com.donidoni.auth.metier.dto.GroupageResponseDto;
import com.donidoni.auth.metier.dto.GroupageUpdateDto;
import com.donidoni.auth.metier.dto.ParticipantResumeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper des groupages.
 *
 * <p>Expose les compteurs de progression attendus par la carte mobile
 * (participants inscrits et places restantes) et la liste des participants
 * réduite à leur nom d'affichage.</p>
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface GroupageMapper extends EntityMapper<Groupage, GroupageCreateDto, GroupageUpdateDto, GroupageResponseDto> {

    @Override
    @Mapping(target = "pays", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "participations", ignore = true)
    Groupage toEntity(GroupageCreateDto createDto);

    @Override
    @Mapping(target = "pays", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "participations", ignore = true)
    void updateEntity(GroupageUpdateDto updateDto, @MappingTarget Groupage entity);

    @Override
    @Mapping(target = "paysId", source = "pays.id")
    @Mapping(target = "paysNom", source = "pays.nom")
    @Mapping(target = "paysEmojiDrapeau", source = "pays.emojiDrapeau")
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "nombreParticipants", expression = "java(entity.getParticipations().size())")
    @Mapping(target = "placesRestantes", expression = "java(entity.getPlacesRestantes())")
    @Mapping(target = "participants", source = "participations")
    GroupageResponseDto toResponse(Groupage entity);

    @Mapping(target = "participationId", source = "id")
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "nomAffichage", source = "utilisateur.nomComplet")
    @Mapping(target = "rangTirage", ignore = true)
    ParticipantResumeDto toParticipant(ParticipationGroupage participation);

    List<ParticipantResumeDto> toParticipants(List<ParticipationGroupage> participations);
}
