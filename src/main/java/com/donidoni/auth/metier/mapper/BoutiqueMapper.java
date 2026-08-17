package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Boutique;
import com.donidoni.auth.metier.dto.BoutiqueCreateDto;
import com.donidoni.auth.metier.dto.BoutiqueResponseDto;
import com.donidoni.auth.metier.dto.BoutiqueUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper des boutiques partenaires.
 *
 * <p>La catégorie n'est pas résolue ici : le service la charge depuis son
 * identifiant avant la persistance.</p>
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BoutiqueMapper extends EntityMapper<Boutique, BoutiqueCreateDto, BoutiqueUpdateDto, BoutiqueResponseDto> {

    @Override
    @Mapping(target = "categorie", ignore = true)
    Boutique toEntity(BoutiqueCreateDto createDto);

    @Override
    @Mapping(target = "categorie", ignore = true)
    void updateEntity(BoutiqueUpdateDto updateDto, @MappingTarget Boutique entity);

    @Override
    @Mapping(target = "categorieId", source = "categorie.id")
    @Mapping(target = "categorieNom", source = "categorie.nom")
    BoutiqueResponseDto toResponse(Boutique entity);
}
