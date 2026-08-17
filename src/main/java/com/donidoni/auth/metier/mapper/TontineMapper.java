package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.CotisationTontine;
import com.donidoni.auth.metier.domain.ParticipationTontine;
import com.donidoni.auth.metier.domain.Tontine;
import com.donidoni.auth.metier.dto.CotisationResponseDto;
import com.donidoni.auth.metier.dto.ParticipantResumeDto;
import com.donidoni.auth.metier.dto.TontineCreateDto;
import com.donidoni.auth.metier.dto.TontineResponseDto;
import com.donidoni.auth.metier.dto.TontineUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper des tontines, de leurs participants et de leurs cotisations.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TontineMapper extends EntityMapper<Tontine, TontineCreateDto, TontineUpdateDto, TontineResponseDto> {

    @Override
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "participations", ignore = true)
    Tontine toEntity(TontineCreateDto createDto);

    @Override
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "participations", ignore = true)
    void updateEntity(TontineUpdateDto updateDto, @MappingTarget Tontine entity);

    @Override
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "nombreParticipants", expression = "java(entity.getParticipations().size())")
    @Mapping(target = "placesRestantes", expression = "java(entity.getPlacesRestantes())")
    @Mapping(target = "participants", source = "participations")
    TontineResponseDto toResponse(Tontine entity);

    @Mapping(target = "participationId", source = "id")
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "nomAffichage", source = "utilisateur.nomComplet")
    @Mapping(target = "montantVerse", source = "montantTotalVerse")
    ParticipantResumeDto toParticipant(ParticipationTontine participation);

    List<ParticipantResumeDto> toParticipants(List<ParticipationTontine> participations);

    @Mapping(target = "participationId", source = "participation.id")
    CotisationResponseDto toCotisation(CotisationTontine cotisation);

    List<CotisationResponseDto> toCotisations(List<CotisationTontine> cotisations);
}
