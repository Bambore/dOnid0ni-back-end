package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.metier.domain.Categorie;
import com.donidoni.auth.metier.domain.enums.TypeCategorie;
import com.donidoni.auth.metier.dto.CategorieCreateDto;
import com.donidoni.auth.metier.dto.CategorieResponseDto;
import com.donidoni.auth.metier.dto.CategorieUpdateDto;
import com.donidoni.auth.metier.service.CategorieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Catégories", description = "Catégories illustrées du marché et des boutiques partenaires")
public class CategorieController extends AbstractCrudController<Categorie, CategorieCreateDto, CategorieUpdateDto, CategorieResponseDto> {

    private final CategorieService categorieService;

    public CategorieController(final CategorieService service) {
        super(service);
        this.categorieService = service;
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lister les catégories actives d'un domaine (PRODUIT ou BOUTIQUE)")
    public ResponseEntity<ApiResponse<List<CategorieResponseDto>>> listerParType(
            @Parameter(description = "Domaine de la catégorie") @PathVariable final TypeCategorie type) {

        return ResponseEntity.ok(ApiResponse.success(categorieService.listerParType(type)));
    }
}
