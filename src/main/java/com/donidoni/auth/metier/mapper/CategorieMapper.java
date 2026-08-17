package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Categorie;
import com.donidoni.auth.metier.dto.CategorieCreateDto;
import com.donidoni.auth.metier.dto.CategorieResponseDto;
import com.donidoni.auth.metier.dto.CategorieUpdateDto;
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
public interface CategorieMapper extends EntityMapper<Categorie, CategorieCreateDto, CategorieUpdateDto, CategorieResponseDto> {

    @Override
    @Mapping(target = "ordreAffichage", source = "ordreAffichage", defaultValue = "0")
    Categorie toEntity(CategorieCreateDto createDto);

    @Override
    void updateEntity(CategorieUpdateDto updateDto, @MappingTarget Categorie entity);

    @Override
    CategorieResponseDto toResponse(Categorie entity);
}
