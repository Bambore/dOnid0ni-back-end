package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Commande;
import com.donidoni.auth.metier.dto.CommandeCreateDto;
import com.donidoni.auth.metier.dto.CommandeResponseDto;
import com.donidoni.auth.metier.dto.CommandeUpdateDto;
import com.donidoni.auth.metier.service.CommandeService;
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

/**
 * Commandes du marché.
 *
 * <p>{@code POST /api/commandes} crée la commande au nom de l'utilisateur
 * authentifié ; les prix sont repris du catalogue et l'échéancier « Petit à
 * petit » est généré côté serveur.</p>
 */
@RestController
@RequestMapping("/api/commandes")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Commandes", description = "Commandes du marché et paiement « Tout payer » ou « Petit à petit »")
public class CommandeController extends AbstractCrudController<Commande, CommandeCreateDto, CommandeUpdateDto, CommandeResponseDto> {

    private final CommandeService commandeService;

    public CommandeController(final CommandeService service) {
        super(service);
        this.commandeService = service;
    }

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister mes commandes")
    public ResponseEntity<ApiResponse<PageResponse<CommandeResponseDto>>> mesCommandes(
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(commandeService.mesCommandes(pageable)));
    }

    @GetMapping("/mes/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consulter le détail d'une de mes commandes")
    public ResponseEntity<ApiResponse<CommandeResponseDto>> consulterMienne(
            @Parameter(description = "ID de la commande") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(commandeService.consulterMienne(id)));
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Annuler une commande pas encore préparée")
    public ResponseEntity<ApiResponse<CommandeResponseDto>> annuler(
            @Parameter(description = "ID de la commande") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(commandeService.annuler(id), "Commande annulée"));
    }
}
