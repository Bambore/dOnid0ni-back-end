package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.metier.domain.Favori;
import com.donidoni.auth.metier.dto.FavoriResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = ArticleMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FavoriMapper {

    FavoriResponseDto toResponse(Favori entity);
}
