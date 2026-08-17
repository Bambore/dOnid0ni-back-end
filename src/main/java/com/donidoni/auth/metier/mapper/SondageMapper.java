package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.OptionSondage;
import com.donidoni.auth.metier.domain.Sondage;
import com.donidoni.auth.metier.domain.VoteSondage;
import com.donidoni.auth.metier.dto.OptionSondageCreateDto;
import com.donidoni.auth.metier.dto.OptionSondageResponseDto;
import com.donidoni.auth.metier.dto.SondageCreateDto;
import com.donidoni.auth.metier.dto.SondageResponseDto;
import com.donidoni.auth.metier.dto.SondageUpdateDto;
import com.donidoni.auth.metier.dto.VoteSondageResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper des sondages de groupage, de leurs options et des votes soumis.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SondageMapper extends EntityMapper<Sondage, SondageCreateDto, SondageUpdateDto, SondageResponseDto> {

    @Override
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "votes", ignore = true)
    Sondage toEntity(SondageCreateDto createDto);

    @Override
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "votes", ignore = true)
    void updateEntity(SondageUpdateDto updateDto, @MappingTarget Sondage entity);

    @Override
    @Mapping(target = "ouvert", expression = "java(entity.estOuvert())")
    @Mapping(target = "nombreVotes", expression = "java((long) entity.getVotes().size())")
    SondageResponseDto toResponse(Sondage entity);

    @Mapping(target = "articleId", source = "article.id")
    OptionSondageResponseDto toOptionResponse(OptionSondage option);

    List<OptionSondageResponseDto> toOptionResponses(List<OptionSondage> options);

    @Mapping(target = "sondage", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "ordreAffichage", source = "ordreAffichage", defaultValue = "0")
    OptionSondage toOptionEntity(OptionSondageCreateDto createDto);

    @Mapping(target = "sondageId", source = "sondage.id")
    @Mapping(target = "optionId", source = "option.id")
    @Mapping(target = "optionLibelle", source = "option.libelle")
    @Mapping(target = "paysId", source = "pays.id")
    @Mapping(target = "paysNom", source = "pays.nom")
    VoteSondageResponseDto toVoteResponse(VoteSondage vote);
}
