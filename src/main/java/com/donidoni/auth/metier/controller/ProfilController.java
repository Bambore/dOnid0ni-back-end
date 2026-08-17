package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.metier.dto.ProfilUpdateDto;
import com.donidoni.auth.metier.dto.TableauDeBordDto;
import com.donidoni.auth.metier.dto.UtilisateurResponseDto;
import com.donidoni.auth.metier.service.ProfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profil de l'utilisateur connecté.
 *
 * <p>Le profil est provisionné automatiquement au premier appel authentifié à
 * partir du JWT Keycloak : l'application mobile n'a pas d'étape d'inscription
 * supplémentaire.</p>
 */
@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Profil", description = "Profil, préférences et tableau de bord de l'utilisateur connecté")
public class ProfilController {

    private final ProfilService profilService;

    @GetMapping
    @Operation(summary = "Consulter mon profil")
    public ResponseEntity<ApiResponse<UtilisateurResponseDto>> monProfil() {
        return ResponseEntity.ok(ApiResponse.success(profilService.monProfil()));
    }

    @PutMapping
    @Operation(summary = "Mettre à jour mon profil et mes préférences")
    public ResponseEntity<ApiResponse<UtilisateurResponseDto>> mettreAJour(
            @Valid @RequestBody final ProfilUpdateDto dto) {

        return ResponseEntity.ok(ApiResponse.success(
                profilService.mettreAJourMonProfil(dto), "Profil mis à jour"));
    }

    @DeleteMapping
    @Operation(summary = "Supprimer mon compte")
    public ResponseEntity<ApiResponse<Void>> supprimerMonCompte() {
        profilService.supprimerMonCompte();
        return ResponseEntity.ok(ApiResponse.success("Votre compte a été supprimé avec succès"));
    }

    @GetMapping("/tableau-de-bord")
    @Operation(summary = "Compteurs de l'écran d'accueil")
    public ResponseEntity<ApiResponse<TableauDeBordDto>> tableauDeBord() {
        return ResponseEntity.ok(ApiResponse.success(profilService.tableauDeBord()));
    }
}
