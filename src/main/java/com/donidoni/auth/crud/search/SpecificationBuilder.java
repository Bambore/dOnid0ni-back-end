package com.donidoni.auth.crud.search;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder fluide pour construire des {@link Specification} JPA
 * à partir de critères de recherche.
 *
 * <p>Utilisable à la fois :</p>
 * <ul>
 *   <li>Automatiquement par le framework à partir d'un {@link SearchRequest} reçu du front</li>
 *   <li>Programmatiquement dans les services pour ajouter des filtres métier</li>
 * </ul>
 *
 * <p>Exemple d'utilisation programmatique :</p>
 * <pre>{@code
 * Specification<Category> spec = SpecificationBuilder.<Category>builder()
 *     .with("name", SearchOperation.LIKE, "don")
 *     .and("status", SearchOperation.EQUALS, "ACTIVE")
 *     .and("createdAt", SearchOperation.BETWEEN, List.of(start, end))
 *     .allowedFields(Set.of("name", "status", "createdAt"))
 *     .build();
 * }</pre>
 *
 * <p>Exemple de construction à partir d'un {@link SearchRequest} :</p>
 * <pre>{@code
 * Specification<Category> spec = SpecificationBuilder.fromSearchRequest(
 *     searchRequest, allowedFields);
 * }</pre>
 *
 * @param <E> le type de l'entité JPA
 */
public class SpecificationBuilder<E> {

    private final List<SearchCriteria> criteria = new ArrayList<>();
    private Set<String> allowedFields;

    private SpecificationBuilder() {
    }

    /**
     * Crée un nouveau builder.
     *
     * @param <E> le type de l'entité
     * @return un nouveau builder
     */
    public static <E> SpecificationBuilder<E> builder() {
        return new SpecificationBuilder<>();
    }

    /**
     * Ajoute un critère de recherche.
     *
     * @param field     le nom du champ
     * @param operation l'opérateur
     * @param value     la valeur
     * @return ce builder (fluide)
     */
    public SpecificationBuilder<E> with(
            final String field,
            final SearchOperation operation,
            final Object value) {
        criteria.add(new SearchCriteria(field, operation, value));
        return this;
    }

    /**
     * Alias de {@link #with(String, SearchOperation, Object)} pour la lisibilité.
     */
    public SpecificationBuilder<E> and(
            final String field,
            final SearchOperation operation,
            final Object value) {
        return with(field, operation, value);
    }

    /**
     * Définit la whitelist de champs autorisés pour le filtrage.
     *
     * @param allowedFields les champs autorisés
     * @return ce builder (fluide)
     */
    public SpecificationBuilder<E> allowedFields(final Set<String> allowedFields) {
        this.allowedFields = allowedFields;
        return this;
    }

    /**
     * Construit la {@link Specification} combinée (AND de tous les critères).
     *
     * @return la specification JPA, ou {@code null} si aucun critère
     */
    public Specification<E> build() {
        if (criteria.isEmpty()) {
            return null;
        }
        Specification<E> result = Specification.where(
                new GenericSpecification<>(criteria.getFirst(), allowedFields));

        for (int i = 1; i < criteria.size(); i++) {
            result = result.and(new GenericSpecification<>(criteria.get(i), allowedFields));
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  FACTORY DEPUIS SEARCH REQUEST
    // ═══════════════════════════════════════════════════════════

    /**
     * Construit une {@link Specification} à partir d'un {@link SearchRequest}.
     *
     * <p>C'est la méthode utilisée automatiquement par le framework CRUD
     * pour convertir la requête du frontend en filtres JPA.</p>
     *
     * @param request       la requête de recherche
     * @param allowedFields les champs autorisés (whitelist)
     * @param <E>           le type de l'entité
     * @return la specification JPA, ou {@code null} si aucun critère valide
     */
    public static <E> Specification<E> fromSearchRequest(
            final SearchRequest request,
            final Set<String> allowedFields) {

        if (request == null || request.criteria() == null || request.criteria().isEmpty()) {
            return null;
        }

        final List<SearchCriteria> validCriteria = request.criteria().stream()
                .filter(SearchCriteria::isValid)
                .toList();

        if (validCriteria.isEmpty()) {
            return null;
        }

        Specification<E> result = Specification.where(
                new GenericSpecification<>(validCriteria.getFirst(), allowedFields));

        for (int i = 1; i < validCriteria.size(); i++) {
            result = result.and(new GenericSpecification<>(validCriteria.get(i), allowedFields));
        }

        return result;
    }
}
