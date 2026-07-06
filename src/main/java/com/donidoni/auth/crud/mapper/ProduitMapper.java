package com.donidoni.auth.crud.mapper;

import com.donidoni.auth.domain.Produit;
import com.donidoni.auth.dto.ProduitCreateDto;
import com.donidoni.auth.dto.ProduitResponseDto;
import com.donidoni.auth.dto.ProduitUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProduitMapper extends EntityMapper<Produit, ProduitCreateDto, ProduitUpdateDto, ProduitResponseDto> {

    @Override
    Produit toEntity(ProduitCreateDto createDto);

    @Override
    void updateEntity(ProduitUpdateDto updateDto, @MappingTarget Produit entity);

    @Override
    ProduitResponseDto toResponse(Produit entity);
}
