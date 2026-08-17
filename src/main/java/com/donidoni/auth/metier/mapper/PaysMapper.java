package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Pays;
import com.donidoni.auth.metier.dto.PaysCreateDto;
import com.donidoni.auth.metier.dto.PaysResponseDto;
import com.donidoni.auth.metier.dto.PaysUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaysMapper extends EntityMapper<Pays, PaysCreateDto, PaysUpdateDto, PaysResponseDto> {

    @Override
    Pays toEntity(PaysCreateDto createDto);

    @Override
    void updateEntity(PaysUpdateDto updateDto, @MappingTarget Pays entity);

    @Override
    PaysResponseDto toResponse(Pays entity);
}
