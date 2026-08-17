package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Cadeau;
import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import com.donidoni.auth.metier.dto.CadeauCreateDto;
import com.donidoni.auth.metier.dto.CadeauResponseDto;
import com.donidoni.auth.metier.dto.CadeauUpdateDto;
import com.donidoni.auth.metier.service.CadeauService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cadeaux")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Cadeaux", description = "Avantages attribués aux utilisateurs (« Mes Cadeaux »)")
public class CadeauController extends AbstractCrudController<Cadeau, CadeauCreateDto, CadeauUpdateDto, CadeauResponseDto> {

    private final CadeauService cadeauService;

    public CadeauController(final CadeauService service) {
        super(service);
        this.cadeauService = service;
    }

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister mes cadeaux")
    public ResponseEntity<ApiResponse<PageResponse<CadeauResponseDto>>> mesCadeaux(
            @Parameter(description = "Filtre optionnel sur l'état du cadeau")
            @RequestParam(required = false) final StatutCadeau statut,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(cadeauService.mesCadeaux(statut, pageable)));
    }

    @PostMapping("/{id}/utiliser")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marquer un cadeau comme utilisé")
    public ResponseEntity<ApiResponse<CadeauResponseDto>> utiliser(
            @Parameter(description = "ID du cadeau") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(cadeauService.utiliser(id), "Cadeau utilisé"));
    }
}
