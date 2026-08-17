package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Sondage;
import com.donidoni.auth.metier.domain.enums.StatutSondage;
import com.donidoni.auth.crud.validation.OnCreate;
import com.donidoni.auth.metier.dto.OptionSondageBatchDto;
import com.donidoni.auth.metier.dto.OptionSondageResponseDto;
import com.donidoni.auth.metier.dto.ResultatSondageDto;
import com.donidoni.auth.metier.dto.SondageCreateDto;
import com.donidoni.auth.metier.dto.SondageResponseDto;
import com.donidoni.auth.metier.dto.SondageUpdateDto;
import com.donidoni.auth.metier.dto.VoteSondageRequestDto;
import com.donidoni.auth.metier.dto.VoteSondageResponseDto;
import com.donidoni.auth.metier.service.SondageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sondages")
@Tag(name = "Sondages", description = "Sondages « Quel groupage lancer ? » : options, votes et résultats")
public class SondageController extends AbstractCrudController<Sondage, SondageCreateDto, SondageUpdateDto, SondageResponseDto> {

    private final SondageService sondageService;

    public SondageController(final SondageService service) {
        super(service);
        this.sondageService = service;
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Lister les sondages d'un statut donné")
    public ResponseEntity<ApiResponse<PageResponse<SondageResponseDto>>> listerParStatut(
            @Parameter(description = "Statut recherché") @PathVariable final StatutSondage statut,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(sondageService.listerParStatut(statut, pageable)));
    }

    @GetMapping("/{id}/options")
    @Operation(summary = "Lister les produits proposés au vote")
    public ResponseEntity<ApiResponse<List<OptionSondageResponseDto>>> listerOptions(
            @Parameter(description = "ID du sondage") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(sondageService.listerOptions(id)));
    }

    @PostMapping("/{id}/options")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Ajouter des produits à la grille de vote")
    public ResponseEntity<ApiResponse<List<OptionSondageResponseDto>>> ajouterOptions(
            @Parameter(description = "ID du sondage") @PathVariable final Long id,
            @Validated(OnCreate.class) @RequestBody final OptionSondageBatchDto lot) {

        return ResponseEntity.ok(ApiResponse.success(
                sondageService.ajouterOptions(id, lot.options()), "Options ajoutées"));
    }

    @PostMapping("/{id}/voter")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Soumettre ma proposition (produit + destination)")
    public ResponseEntity<ApiResponse<VoteSondageResponseDto>> voter(
            @Parameter(description = "ID du sondage") @PathVariable final Long id,
            @Valid @RequestBody final VoteSondageRequestDto requete) {

        return ResponseEntity.ok(ApiResponse.success(
                sondageService.voter(id, requete), "Votre proposition a été enregistrée"));
    }

    @GetMapping("/{id}/mon-vote")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Consulter ma proposition pour ce sondage")
    public ResponseEntity<ApiResponse<VoteSondageResponseDto>> monVote(
            @Parameter(description = "ID du sondage") @PathVariable final Long id) {

        return sondageService.monVote(id)
                .map(vote -> ResponseEntity.ok(ApiResponse.success(vote)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/resultats")
    @Operation(summary = "Dépouiller un sondage par couple produit/destination")
    public ResponseEntity<ApiResponse<List<ResultatSondageDto>>> resultats(
            @Parameter(description = "ID du sondage") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(sondageService.resultats(id)));
    }
}
