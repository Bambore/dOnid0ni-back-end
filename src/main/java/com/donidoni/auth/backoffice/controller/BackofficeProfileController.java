package com.donidoni.auth.backoffice.controller;

import com.donidoni.auth.backoffice.dto.PageResponse;
import com.donidoni.auth.backoffice.dto.ProfileFormDto;
import com.donidoni.auth.backoffice.dto.ProfileWebDto;
import com.donidoni.auth.backoffice.service.BackofficeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des profils (groupes Keycloak) du backoffice.
 */
@RestController
@RequestMapping("/api/backoffice/profils")
@Tag(name = "Backoffice — Profils", description = "Gestion des profils (groupes) Keycloak")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeProfileController {

    private final BackofficeProfileService profileService;

    /**
     * Constructeur.
     *
     * @param profileService le service de gestion des profils
     */
    public BackofficeProfileController(final BackofficeProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Liste paginée des profils.
     *
     * @param search filtre textuel (optionnel)
     * @param page   numéro de page (défaut: 0)
     * @param size   taille de la page (défaut: 10)
     * @return la page de résultats
     */
    @GetMapping
    @Operation(summary = "Lister les profils (paginé)")
    public ResponseEntity<PageResponse<ProfileWebDto>> findAll(
            @RequestParam(required = false) final String search,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(profileService.findAllPaged(search, page, size));
    }

    /**
     * Récupère un profil par son identifiant.
     *
     * @param id l'identifiant Keycloak du groupe
     * @return le DTO du profil
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un profil par son identifiant")
    public ResponseEntity<ProfileWebDto> findById(
            @PathVariable final String id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    /**
     * Crée un nouveau profil.
     *
     * @param form le formulaire de création
     * @return le DTO du profil créé
     */
    @PostMapping
    @Operation(summary = "Créer un profil")
    public ResponseEntity<ProfileWebDto> create(
            @Valid @RequestBody final ProfileFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.create(form));
    }

    /**
     * Met à jour un profil.
     *
     * @param id   l'identifiant Keycloak du groupe
     * @param form le formulaire de mise à jour
     * @return le DTO du profil mis à jour
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un profil")
    public ResponseEntity<ProfileWebDto> update(
            @PathVariable final String id,
            @Valid @RequestBody final ProfileFormDto form) {
        return ResponseEntity.ok(profileService.update(id, form));
    }

    /**
     * Supprime un profil.
     *
     * @param id l'identifiant Keycloak du groupe
     * @return une réponse 204
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un profil")
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        profileService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
