package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.search.SearchRequest;
import com.donidoni.auth.metier.dto.UtilisateurResponseDto;
import com.donidoni.auth.metier.service.AdminUtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Annuaire des comptes clients de l'application mobile, exposé au back-office.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Clients", description = "Comptes clients de l'application mobile (back-office)")
public class UtilisateurController {

    private final AdminUtilisateurService service;

    @GetMapping
    @Operation(summary = "Lister les comptes clients")
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurResponseDto>>> findAll(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @PostMapping("/search")
    @Operation(summary = "Recherche avancée sur les comptes clients")
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurResponseDto>>> search(
            @Valid @RequestBody final SearchRequest searchRequest) {

        return ResponseEntity.ok(ApiResponse.success(service.search(searchRequest)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un compte client")
    public ResponseEntity<ApiResponse<UtilisateurResponseDto>> findById(
            @Parameter(description = "ID du compte") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Suspendre ou réactiver un compte client")
    public ResponseEntity<ApiResponse<UtilisateurResponseDto>> basculerActivation(
            @Parameter(description = "ID du compte") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                service.basculerActivation(id), "Statut du compte mis à jour"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un compte client")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @Parameter(description = "ID du compte") @PathVariable final Long id) {

        service.supprimer(id);
        return ResponseEntity.ok(ApiResponse.success("Compte désactivé"));
    }
}
