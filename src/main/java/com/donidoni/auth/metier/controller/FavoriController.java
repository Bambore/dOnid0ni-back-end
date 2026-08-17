package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.dto.FavoriResponseDto;
import com.donidoni.auth.metier.service.FavoriService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Favoris", description = "Articles mis en favori par l'utilisateur connecté")
public class FavoriController {

    private final FavoriService favoriService;

    @GetMapping("/mes")
    @Operation(summary = "Lister mes favoris")
    public ResponseEntity<ApiResponse<PageResponse<FavoriResponseDto>>> mesFavoris(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(favoriService.mesFavoris(pageable)));
    }

    @PostMapping("/{articleId}")
    @Operation(summary = "Ajouter un article à mes favoris")
    public ResponseEntity<ApiResponse<FavoriResponseDto>> ajouter(
            @Parameter(description = "ID de l'article") @PathVariable final Long articleId) {

        return ResponseEntity.ok(ApiResponse.success(
                favoriService.ajouter(articleId), "Article ajouté aux favoris"));
    }

    @DeleteMapping("/{articleId}")
    @Operation(summary = "Retirer un article de mes favoris")
    public ResponseEntity<ApiResponse<Void>> retirer(
            @Parameter(description = "ID de l'article") @PathVariable final Long articleId) {

        favoriService.retirer(articleId);
        return ResponseEntity.ok(ApiResponse.success("Article retiré des favoris"));
    }

    @GetMapping("/{articleId}/existe")
    @Operation(summary = "Savoir si un article est dans mes favoris")
    public ResponseEntity<ApiResponse<Boolean>> estFavori(
            @Parameter(description = "ID de l'article") @PathVariable final Long articleId) {

        return ResponseEntity.ok(ApiResponse.success(favoriService.estFavori(articleId)));
    }
}
