package com.donidoni.auth.crud.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de requête de recherche avancée reçu du frontend.
 *
 * <p>Encapsule les critères de filtrage, la pagination et le tri
 * dans une seule requête envoyée via {@code POST /search}.</p>
 *
 * <p>Exemple de payload JSON :</p>
 * <pre>{@code
 * {
 *   "criteria": [
 *     { "field": "name", "operation": "LIKE", "value": "don" },
 *     { "field": "status", "operation": "EQUALS", "value": "ACTIVE" },
 *     { "field": "createdAt", "operation": "BETWEEN", "value": ["2026-01-01T00:00:00Z", "2026-06-30T23:59:59Z"] }
 *   ],
 *   "page": 0,
 *   "size": 20,
 *   "sortBy": "createdAt",
 *   "sortDirection": "DESC"
 * }
 * }</pre>
 */
public record SearchRequest(

        /** Liste des critères de filtrage. */
        List<SearchCriteria> criteria,

        /** Numéro de la page (0-indexed). */
        @Min(0)
        Integer page,

        /** Nombre d'éléments par page (max 100). */
        @Min(1) @Max(100)
        Integer size,

        /** Champ de tri (ex: "createdAt", "name"). */
        String sortBy,

        /** Direction du tri : ASC ou DESC. */
        String sortDirection
) {

    /**
     * Constructeur compact avec valeurs par défaut.
     */
    public SearchRequest {
        if (criteria == null) {
            criteria = new ArrayList<>();
        }
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }
        if (sortDirection == null || sortDirection.isBlank()) {
            sortDirection = "ASC";
        }
    }

    /**
     * @return {@code true} si le tri est descendant
     */
    public boolean isDescending() {
        return "DESC".equalsIgnoreCase(sortDirection);
    }
}
