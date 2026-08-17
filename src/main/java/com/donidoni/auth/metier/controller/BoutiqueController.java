package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Boutique;
import com.donidoni.auth.metier.dto.BoutiqueCreateDto;
import com.donidoni.auth.metier.dto.BoutiqueResponseDto;
import com.donidoni.auth.metier.dto.BoutiqueUpdateDto;
import com.donidoni.auth.metier.service.BoutiqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boutiques")
@Tag(name = "Boutiques", description = "Annuaire des boutiques partenaires")
public class BoutiqueController extends AbstractCrudController<Boutique, BoutiqueCreateDto, BoutiqueUpdateDto, BoutiqueResponseDto> {

    private final BoutiqueService boutiqueService;

    public BoutiqueController(final BoutiqueService service) {
        super(service);
        this.boutiqueService = service;
    }

    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Lister les boutiques actives d'une catégorie")
    public ResponseEntity<ApiResponse<PageResponse<BoutiqueResponseDto>>> listerParCategorie(
            @Parameter(description = "ID de la catégorie") @PathVariable final Long categorieId,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(boutiqueService.listerParCategorie(categorieId, pageable)));
    }
}
