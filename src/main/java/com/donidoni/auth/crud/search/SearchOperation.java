package com.donidoni.auth.crud.search;

/**
 * Opérateurs de recherche supportés par le système de Criteria.
 *
 * <p>Chaque opérateur est traduit en un {@link jakarta.persistence.criteria.Predicate}
 * JPA par le {@link GenericSpecification}.</p>
 */
public enum SearchOperation {

    /** Égalité exacte : {@code field = value} */
    EQUALS,

    /** Inégalité : {@code field != value} */
    NOT_EQUALS,

    /** Recherche partielle insensible à la casse : {@code LOWER(field) LIKE %value%} */
    LIKE,

    /** Commence par : {@code LOWER(field) LIKE value%} */
    STARTS_WITH,

    /** Termine par : {@code LOWER(field) LIKE %value} */
    ENDS_WITH,

    /** Supérieur strictement : {@code field > value} */
    GREATER_THAN,

    /** Supérieur ou égal : {@code field >= value} */
    GREATER_THAN_EQUAL,

    /** Inférieur strictement : {@code field < value} */
    LESS_THAN,

    /** Inférieur ou égal : {@code field <= value} */
    LESS_THAN_EQUAL,

    /**
     * Inclusion dans une liste : {@code field IN (v1, v2, v3)}.
     * La valeur doit être une {@link java.util.List}.
     */
    IN,

    /**
     * Exclusion d'une liste : {@code field NOT IN (v1, v2, v3)}.
     * La valeur doit être une {@link java.util.List}.
     */
    NOT_IN,

    /**
     * Intervalle : {@code field BETWEEN min AND max}.
     * La valeur doit être une {@link java.util.List} de 2 éléments {@code [min, max]}.
     */
    BETWEEN,

    /** Valeur nulle : {@code field IS NULL} */
    IS_NULL,

    /** Valeur non nulle : {@code field IS NOT NULL} */
    IS_NOT_NULL
}
