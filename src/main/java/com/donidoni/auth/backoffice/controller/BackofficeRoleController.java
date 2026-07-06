package com.donidoni.auth.backoffice.controller;

import com.donidoni.auth.backoffice.dto.PageResponse;
import com.donidoni.auth.backoffice.dto.RoleFormDto;
import com.donidoni.auth.backoffice.dto.RoleWebDto;
import com.donidoni.auth.backoffice.service.BackofficeRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des rôles Keycloak du backoffice.
 *
 * <p>Les rôles ne sont pas créés ni supprimés via cette API.
 * Leur cycle de vie est géré par {@code KeycloakRolesInitializer} au démarrage.</p>
 */
@RestController
@RequestMapping("/api/backoffice/roles")
@Tag(name = "Backoffice — Rôles", description = "Gestion des rôles Keycloak")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeRoleController {

    private final BackofficeRoleService roleService;

    /**
     * Constructeur.
     *
     * @param roleService le service de gestion des rôles
     */
    public BackofficeRoleController(final BackofficeRoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Liste paginée des rôles applicatifs.
     *
     * @param search filtre textuel (optionnel)
     * @param page   numéro de page (défaut: 0)
     * @param size   taille de la page (défaut: 10)
     * @return la page de résultats
     */
    @GetMapping
    @Operation(summary = "Lister les rôles (paginé)")
    public ResponseEntity<PageResponse<RoleWebDto>> findAll(
            @RequestParam(required = false) final String search,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(roleService.findAllPaged(search, page, size));
    }

    /**
     * Récupère un rôle par son nom.
     *
     * @param name le nom du rôle
     * @return le DTO du rôle
     */
    @GetMapping("/{name}")
    @Operation(summary = "Récupérer un rôle par son nom")
    public ResponseEntity<RoleWebDto> findByName(
            @PathVariable final String name) {
        return ResponseEntity.ok(roleService.findRoleByName(name));
    }

    /**
     * Met à jour la description d'un rôle.
     *
     * @param name le nom du rôle
     * @param form le formulaire de mise à jour
     * @return le rôle mis à jour
     */
    @PutMapping("/{name}")
    @Operation(summary = "Mettre à jour la description d'un rôle")
    public ResponseEntity<RoleWebDto> update(
            @PathVariable final String name,
            @Valid @RequestBody final RoleFormDto form) {
        return ResponseEntity.ok(roleService.updateRole(name, form));
    }
}
