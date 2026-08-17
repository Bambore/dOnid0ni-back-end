package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Tontine;
import com.donidoni.auth.metier.domain.enums.StatutTontine;
import com.donidoni.auth.metier.dto.CotisationResponseDto;
import com.donidoni.auth.metier.dto.ParticipantResumeDto;
import com.donidoni.auth.metier.dto.TontineCreateDto;
import com.donidoni.auth.metier.dto.TontineResponseDto;
import com.donidoni.auth.metier.dto.TontineUpdateDto;
import com.donidoni.auth.metier.service.TontineService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tontines")
@Tag(name = "Tontines", description = "Tontines d'acquisition : consultation, adhésion et cotisations")
public class TontineController extends AbstractCrudController<Tontine, TontineCreateDto, TontineUpdateDto, TontineResponseDto> {

    private final TontineService tontineService;

    public TontineController(final TontineService service) {
        super(service);
        this.tontineService = service;
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Lister les tontines d'un statut (onglets En attente / En cours / Fermé)")
    public ResponseEntity<ApiResponse<PageResponse<TontineResponseDto>>> listerParStatut(
            @Parameter(description = "Statut recherché") @PathVariable final StatutTontine statut,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(tontineService.listerParStatut(statut, pageable)));
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "Lister les participants d'une tontine")
    public ResponseEntity<ApiResponse<List<ParticipantResumeDto>>> listerParticipants(
            @Parameter(description = "ID de la tontine") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(tontineService.listerParticipants(id)));
    }

    @PostMapping("/{id}/participer")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Participer à une tontine encore en attente")
    public ResponseEntity<ApiResponse<ParticipantResumeDto>> participer(
            @Parameter(description = "ID de la tontine") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                tontineService.participer(id), "Vous participez maintenant à cette tontine"));
    }

    @GetMapping("/{id}/mes-cotisations")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Consulter mon échéancier de cotisations pour une tontine")
    public ResponseEntity<ApiResponse<List<CotisationResponseDto>>> mesCotisations(
            @Parameter(description = "ID de la tontine") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(tontineService.mesCotisations(id)));
    }

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Lister les tontines auxquelles je participe")
    public ResponseEntity<ApiResponse<PageResponse<TontineResponseDto>>> mesTontines(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(tontineService.mesTontines(pageable)));
    }
}
