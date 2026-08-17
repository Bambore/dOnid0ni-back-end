package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.metier.domain.Notification;
import com.donidoni.auth.metier.domain.enums.TypeNotification;
import com.donidoni.auth.metier.dto.NotificationCreateDto;
import com.donidoni.auth.metier.dto.NotificationResponseDto;
import com.donidoni.auth.metier.dto.NotificationUpdateDto;
import com.donidoni.auth.metier.service.NotificationService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Notifications", description = "Centre de notifications de l'application mobile")
public class NotificationController extends AbstractCrudController<Notification, NotificationCreateDto, NotificationUpdateDto, NotificationResponseDto> {

    private final NotificationService notificationService;

    public NotificationController(final NotificationService service) {
        super(service);
        this.notificationService = service;
    }

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister mes notifications, filtrées par type au besoin")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponseDto>>> mesNotifications(
            @Parameter(description = "Types à conserver ; omis pour tout afficher")
            @RequestParam(required = false) final List<TypeNotification> types,
            @ParameterObject final Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(notificationService.mesNotifications(types, pageable)));
    }

    @GetMapping("/non-lues/compteur")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Compter mes notifications non lues")
    public ResponseEntity<ApiResponse<Long>> compterNonLues() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.compterNonLues()));
    }

    @PatchMapping("/{id}/lue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> marquerLue(
            @Parameter(description = "ID de la notification") @PathVariable final Long id) {

        return ResponseEntity.ok(ApiResponse.success(notificationService.marquerLue(id)));
    }

    @PostMapping("/toutes-lues")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tout marquer comme lu")
    public ResponseEntity<ApiResponse<Integer>> marquerToutesLues() {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.marquerToutesLues(), "Notifications marquées comme lues"));
    }

    @DeleteMapping("/mes/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer une de mes notifications")
    public ResponseEntity<ApiResponse<Void>> supprimerMienne(
            @Parameter(description = "ID de la notification") @PathVariable final Long id) {

        notificationService.supprimerMienne(id);
        return ResponseEntity.ok(ApiResponse.success("Notification supprimée"));
    }

    @DeleteMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tout effacer")
    public ResponseEntity<ApiResponse<Integer>> supprimerToutes() {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.supprimerToutes(), "Notifications supprimées"));
    }
}
