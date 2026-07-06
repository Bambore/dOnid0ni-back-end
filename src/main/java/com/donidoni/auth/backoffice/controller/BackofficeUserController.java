package com.donidoni.auth.backoffice.controller;

import com.donidoni.auth.backoffice.dto.BackofficeUserFormDto;
import com.donidoni.auth.backoffice.dto.BackofficeUserWebDto;
import com.donidoni.auth.backoffice.dto.PageResponse;
import com.donidoni.auth.backoffice.dto.UserActionDto;
import com.donidoni.auth.backoffice.dto.UserStatsDto;
import com.donidoni.auth.backoffice.service.BackofficeUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des utilisateurs du backoffice.
 *
 * <p>Expose les opérations CRUD paginées ainsi que les actions spécifiques :
 * renvoi du mail d'activation, réinitialisation du mot de passe,
 * activation/désactivation et statistiques.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/backoffice/users")
@Tag(name = "Backoffice — Utilisateurs", description = "Gestion des comptes utilisateurs du backoffice")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeUserController {

    private final BackofficeUserService userService;

    /**
     * Constructeur.
     *
     * @param userService le service de gestion des utilisateurs backoffice
     */
    public BackofficeUserController(final BackofficeUserService userService) {
        this.userService = userService;
    }

    /**
     * Liste paginée des utilisateurs.
     *
     * @param search filtre textuel (optionnel)
     * @param page   numéro de page (défaut: 0)
     * @param size   taille de la page (défaut: 10)
     * @return la page de résultats
     */
    @GetMapping
    @Operation(summary = "Lister les utilisateurs backoffice (paginé)")
    public ResponseEntity<PageResponse<BackofficeUserWebDto>> findAll(
            @RequestParam(required = false) final String search,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        return ResponseEntity.ok(userService.findAllPaged(search, page, size));
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id l'identifiant Keycloak
     * @return le DTO de l'utilisateur
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par son identifiant")
    public ResponseEntity<BackofficeUserWebDto> findById(
            @PathVariable final String id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Crée un nouvel utilisateur backoffice.
     *
     * @param form le formulaire de création
     * @return le DTO de l'utilisateur créé
     */
    @PostMapping
    @Operation(summary = "Créer un utilisateur backoffice")
    public ResponseEntity<BackofficeUserWebDto> create(
            @Valid @RequestBody final BackofficeUserFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(form));
    }

    /**
     * Met à jour un utilisateur existant.
     *
     * @param id   l'identifiant Keycloak
     * @param form le formulaire de mise à jour
     * @return le DTO de l'utilisateur mis à jour
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur backoffice")
    public ResponseEntity<BackofficeUserWebDto> update(
            @PathVariable final String id,
            @Valid @RequestBody final BackofficeUserFormDto form) {
        return ResponseEntity.ok(userService.update(id, form));
    }

    /**
     * Supprime un utilisateur.
     *
     * @param id l'identifiant Keycloak
     * @return une réponse 204
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur backoffice")
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Active ou désactive un compte utilisateur.
     *
     * @param id l'identifiant Keycloak
     * @return le DTO de l'utilisateur mis à jour
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activer ou désactiver un utilisateur")
    public ResponseEntity<BackofficeUserWebDto> toggle(
            @PathVariable final String id) {
        return ResponseEntity.ok(userService.toggleEnabled(id));
    }

    /**
     * Renvoie le mail d'activation.
     *
     * @param action le DTO contenant l'identifiant de l'utilisateur
     * @return une réponse 204
     */
    @PostMapping("/resend-activation")
    @Operation(summary = "Renvoyer le mail d'activation")
    public ResponseEntity<Void> resendActivation(
            @RequestBody final UserActionDto action) {
        userService.resendActivationEmail(action.userId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Envoie un lien de réinitialisation du mot de passe.
     *
     * @param action le DTO contenant l'identifiant de l'utilisateur
     * @return une réponse 204
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Envoyer un lien de réinitialisation du mot de passe")
    public ResponseEntity<Void> resetPassword(
            @RequestBody final UserActionDto action) {
        userService.sendResetPasswordEmail(action.userId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Statistiques des comptes utilisateurs.
     *
     * @return les statistiques (total, actifs, inactifs)
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des comptes utilisateurs")
    public ResponseEntity<UserStatsDto> getStats() {
        return ResponseEntity.ok(userService.getStats());
    }
}
