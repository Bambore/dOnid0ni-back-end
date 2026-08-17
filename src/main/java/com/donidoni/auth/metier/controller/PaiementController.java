package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.search.SearchRequest;
import com.donidoni.auth.metier.dto.PaiementCreateDto;
import com.donidoni.auth.metier.dto.PaiementResponseDto;
import com.donidoni.auth.metier.service.PaiementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Paiements", description = "Encaissement des commandes, échéances, cotisations et quotes-parts")
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initier un paiement")
    public ResponseEntity<ApiResponse<PaiementResponseDto>> initier(
            @Valid @RequestBody final PaiementCreateDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(paiementService.initier(dto), "Paiement initié"));
    }

    @PostMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirmer l'encaissement d'un paiement (retour de l'agrégateur)")
    public ResponseEntity<ApiResponse<PaiementResponseDto>> confirmer(
            @Parameter(description = "ID du paiement") @PathVariable final Long id,
            @Parameter(description = "Référence renvoyée par l'agrégateur")
            @RequestParam(required = false) final String referenceExterne) {

        return ResponseEntity.ok(ApiResponse.success(
                paiementService.confirmer(id, referenceExterne), "Paiement confirmé"));
    }

    @PostMapping("/{id}/echouer")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marquer un paiement comme échoué")
    public ResponseEntity<ApiResponse<PaiementResponseDto>> echouer(
            @Parameter(description = "ID du paiement") @PathVariable final Long id,
            @Parameter(description = "Motif de l'échec")
            @RequestParam(required = false) final String motif) {

        return ResponseEntity.ok(ApiResponse.success(paiementService.echouer(id, motif), "Paiement en échec"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Journal des paiements (back-office)")
    public ResponseEntity<ApiResponse<PageResponse<PaiementResponseDto>>> findAll(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(paiementService.findAll(pageable)));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recherche avancée sur le journal des paiements")
    public ResponseEntity<ApiResponse<PageResponse<PaiementResponseDto>>> search(
            @Valid @RequestBody final SearchRequest searchRequest) {

        return ResponseEntity.ok(ApiResponse.success(paiementService.search(searchRequest)));
    }

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister mes paiements")
    public ResponseEntity<ApiResponse<PageResponse<PaiementResponseDto>>> mesPaiements(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(paiementService.mesPaiements(pageable)));
    }
}
