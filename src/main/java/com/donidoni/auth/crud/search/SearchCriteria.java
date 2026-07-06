package com.donidoni.auth.crud.search;

/**
 * Représente un critère de recherche unitaire.
 *
 * <p>Combiné avec d'autres critères dans un {@link SearchRequest},
 * il est converti en {@link jakarta.persistence.criteria.Predicate}
 * JPA par le {@link GenericSpecification}.</p>
 *
 * <p>Exemples :</p>
 * <ul>
 *   <li>{@code {"field": "name", "operation": "LIKE", "value": "don"}}</li>
 *   <li>{@code {"field": "status", "operation": "IN", "value": ["ACTIVE", "PENDING"]}}</li>
 *   <li>{@code {"field": "createdAt", "operation": "BETWEEN", "value": ["2026-01-01T00:00:00Z", "2026-06-30T23:59:59Z"]}}</li>
 * </ul>
 *
 * @param field     le nom du champ de l'entité (supporte la notation pointée pour les relations, ex: "category.name")
 * @param operation l'opérateur de comparaison
 * @param value     la valeur de comparaison (peut être String, Number, List, null selon l'opérateur)
 */
public record SearchCriteria(
        String field,
        SearchOperation operation,
        Object value
) {

    /**
     * Vérifie que le critère est valide.
     *
     * @return {@code true} si le critère peut être utilisé
     */
    public boolean isValid() {
        if (field == null || field.isBlank() || operation == null) {
            return false;
        }
        // IS_NULL et IS_NOT_NULL n'ont pas besoin de valeur
        if (operation == SearchOperation.IS_NULL || operation == SearchOperation.IS_NOT_NULL) {
            return true;
        }
        return value != null;
    }
}
