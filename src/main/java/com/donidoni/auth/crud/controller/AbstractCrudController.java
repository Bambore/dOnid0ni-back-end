package com.donidoni.auth.crud.controller;

import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.search.SearchRequest;
import com.donidoni.auth.crud.service.CrudService;
import com.donidoni.auth.crud.validation.OnCreate;
import com.donidoni.auth.crud.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Contrôleur REST abstrait fournissant 5 endpoints CRUD génériques.
 *
 * <p>Les développeurs doivent étendre cette classe et fournir l'URL mapping.</p>
 * <p>Exemple :</p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/categories")
 * public class CategoryController extends AbstractCrudController<Category, CategoryCreateDto, CategoryUpdateDto, CategoryResponseDto> {
 *     public CategoryController(CrudService<CategoryCreateDto, CategoryUpdateDto, CategoryResponseDto> service) {
 *         super(service);
 *     }
 * }
 * }</pre>
 *
 * @param <E> type de l'entité JPA
 * @param <C> type du DTO de création
 * @param <U> type du DTO de mise à jour
 * @param <R> type du DTO de réponse
 */
@RequiredArgsConstructor
public abstract class AbstractCrudController<E, C, U, R> {

    protected final CrudService<C, U, R> service;

    @PostMapping
    @Operation(summary = "Créer une nouvelle ressource")
    public ResponseEntity<ApiResponse<R>> create(
            @Validated(OnCreate.class) @RequestBody final C createDto) {
        
        final R result = service.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Ressource créée avec succès"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une ressource par son ID")
    public ResponseEntity<ApiResponse<R>> findById(
            @Parameter(description = "ID de la ressource") @PathVariable final Long id) {
        
        final R result = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les ressources avec pagination simple")
    public ResponseEntity<ApiResponse<PageResponse<R>>> findAll(
            @ParameterObject final Pageable pageable) {
        
        final PageResponse<R> result = service.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/search")
    @Operation(summary = "Recherche avancée avec critères dynamiques (Criteria)")
    public ResponseEntity<ApiResponse<PageResponse<R>>> search(
            @Valid @RequestBody final SearchRequest searchRequest) {
        
        final PageResponse<R> result = service.search(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour partiellement une ressource")
    public ResponseEntity<ApiResponse<R>> update(
            @Parameter(description = "ID de la ressource") @PathVariable final Long id,
            @Validated(OnUpdate.class) @RequestBody final U updateDto) {
        
        final R result = service.update(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Ressource mise à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une ressource (soft-delete si supporté)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID de la ressource") @PathVariable final Long id) {
        
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Ressource supprimée avec succès"));
    }
}
