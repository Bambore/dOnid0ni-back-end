package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Cadeau;
import com.donidoni.auth.metier.dto.CadeauCreateDto;
import com.donidoni.auth.metier.dto.CadeauResponseDto;
import com.donidoni.auth.metier.dto.CadeauUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CadeauMapper extends EntityMapper<Cadeau, CadeauCreateDto, CadeauUpdateDto, CadeauResponseDto> {

    @Override
    @Mapping(target = "utilisateur", ignore = true)
    Cadeau toEntity(CadeauCreateDto createDto);

    @Override
    void updateEntity(CadeauUpdateDto updateDto, @MappingTarget Cadeau entity);

    @Override
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    CadeauResponseDto toResponse(Cadeau entity);
}
