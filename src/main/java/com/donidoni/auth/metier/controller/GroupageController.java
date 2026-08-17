package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Groupage;
import com.donidoni.auth.metier.domain.enums.StatutGroupage;
import com.donidoni.auth.metier.dto.GroupageCreateDto;
import com.donidoni.auth.metier.dto.GroupageResponseDto;
import com.donidoni.auth.metier.dto.GroupageUpdateDto;
import com.donidoni.auth.metier.dto.ParticipantResumeDto;
import com.donidoni.auth.metier.service.GroupageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/groupages")
@Tag(name = "Groupages", description = "Achats groupés : consultation, adhésion et participants")
public class GroupageController extends AbstractCrudController<Groupage, GroupageCreateDto, GroupageUpdateDto, GroupageResponseDto> {

    private final GroupageService groupageService;

    public GroupageController(final GroupageService service) {
        super(service);
        this.groupageService = service;
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Lister les groupages d'un statut donné (OUVERT pour l'onglet « disponibles »)")
    public ResponseEntity<ApiResponse<PageResponse<GroupageResponseDto>>> listerParStatut(
            @Parameter(description = "Statut recherché") @PathVariable final StatutGroupage statut,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(groupageService.listerParStatut(statut, pageable)));
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "Lister les participants d'un groupage")
    public ResponseEntity<ApiResponse<List<ParticipantResumeDto>>> listerParticipants(
            @Parameter(description = "ID du groupage") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(groupageService.listerParticipants(id)));
    }

    @PostMapping("/{id}/participer")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Participer à un groupage")
    public ResponseEntity<ApiResponse<ParticipantResumeDto>> participer(
            @Parameter(description = "ID du groupage") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                groupageService.participer(id), "Vous participez maintenant à ce groupage"));
    }

    @DeleteMapping("/{id}/participer")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Retirer sa participation à un groupage encore ouvert")
    public ResponseEntity<ApiResponse<Void>> quitter(
            @Parameter(description = "ID du groupage") @PathVariable final Long id) {

        groupageService.quitter(id);
        return ResponseEntity.ok(ApiResponse.success("Participation retirée"));
    }

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Lister les groupages auxquels je participe")
    public ResponseEntity<ApiResponse<PageResponse<GroupageResponseDto>>> mesGroupages(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(groupageService.mesGroupages(pageable)));
    }
}
