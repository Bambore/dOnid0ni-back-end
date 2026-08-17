package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Article;
import com.donidoni.auth.metier.dto.ArticleCreateDto;
import com.donidoni.auth.metier.dto.ArticleResponseDto;
import com.donidoni.auth.metier.dto.ArticleUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper des articles du marché.
 *
 * <p>Catégorie et boutique sont résolues par le service à partir de leurs
 * identifiants ; le mapper se limite aux champs scalaires et à la galerie.</p>
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ArticleMapper extends EntityMapper<Article, ArticleCreateDto, ArticleUpdateDto, ArticleResponseDto> {

    @Override
    @Mapping(target = "categorie", ignore = true)
    @Mapping(target = "boutique", ignore = true)
    @Mapping(target = "stock", source = "stock", defaultValue = "0")
    Article toEntity(ArticleCreateDto createDto);

    @Override
    @Mapping(target = "categorie", ignore = true)
    @Mapping(target = "boutique", ignore = true)
    void updateEntity(ArticleUpdateDto updateDto, @MappingTarget Article entity);

    @Override
    @Mapping(target = "categorieId", source = "categorie.id")
    @Mapping(target = "categorieNom", source = "categorie.nom")
    @Mapping(target = "boutiqueId", source = "boutique.id")
    @Mapping(target = "boutiqueNom", source = "boutique.nom")
    @Mapping(target = "boutiqueTelephone", source = "boutique.telephone")
    ArticleResponseDto toResponse(Article entity);
}
