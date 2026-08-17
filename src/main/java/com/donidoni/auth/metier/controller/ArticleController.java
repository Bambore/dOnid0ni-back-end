package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Article;
import com.donidoni.auth.metier.dto.ArticleCreateDto;
import com.donidoni.auth.metier.dto.ArticleResponseDto;
import com.donidoni.auth.metier.dto.ArticleUpdateDto;
import com.donidoni.auth.metier.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Articles", description = "Catalogue du marché : listes, recherche et fiche produit")
public class ArticleController extends AbstractCrudController<Article, ArticleCreateDto, ArticleUpdateDto, ArticleResponseDto> {

    private final ArticleService articleService;

    public ArticleController(final ArticleService service) {
        super(service);
        this.articleService = service;
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher un article par nom ou description")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponseDto>>> rechercher(
            @Parameter(description = "Terme recherché") @RequestParam final String terme,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(articleService.rechercher(terme, pageable)));
    }

    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Lister les articles d'une catégorie")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponseDto>>> listerParCategorie(
            @Parameter(description = "ID de la catégorie") @PathVariable final Long categorieId,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(articleService.listerParCategorie(categorieId, pageable)));
    }

    @GetMapping("/boutique/{boutiqueId}")
    @Operation(summary = "Lister les articles d'une boutique partenaire")
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponseDto>>> listerParBoutique(
            @Parameter(description = "ID de la boutique") @PathVariable final Long boutiqueId,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(articleService.listerParBoutique(boutiqueId, pageable)));
    }

    @GetMapping("/{id}/fiche")
    @Operation(summary = "Consulter la fiche produit et comptabiliser la vue")
    public ResponseEntity<ApiResponse<ArticleResponseDto>> consulter(
            @Parameter(description = "ID de l'article") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(articleService.consulter(id)));
    }
}
