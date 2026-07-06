package com.donidoni.auth.crud.search;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Moteur de conversion de {@link SearchCriteria} en {@link Predicate} JPA.
 *
 * <p>Gère automatiquement :</p>
 * <ul>
 *   <li>Les types {@code String}, {@code Number}, {@code Enum}, {@code Boolean}, {@code Instant}, {@code LocalDate}</li>
 *   <li>La navigation dans les relations JPA (ex: {@code "category.name"} → join automatique)</li>
 *   <li>La sécurité via whitelist de champs filtrables</li>
 *   <li>Les 14 opérateurs de {@link SearchOperation}</li>
 * </ul>
 *
 * @param <E> le type de l'entité JPA
 */
@Slf4j
public class GenericSpecification<E> implements Specification<E> {

    private final SearchCriteria criteria;
    private final Set<String> allowedFields;

    /**
     * @param criteria      le critère à convertir
     * @param allowedFields les champs autorisés pour le filtrage (whitelist de sécurité)
     */
    public GenericSpecification(final SearchCriteria criteria, final Set<String> allowedFields) {
        this.criteria = criteria;
        this.allowedFields = allowedFields;
    }

    @Override
    public Predicate toPredicate(
            final Root<E> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb) {

        if (!criteria.isValid()) {
            return cb.conjunction(); // critère invalide → pas de filtre
        }

        // Sécurité : vérifier que le champ est dans la whitelist
        final String baseField = criteria.field().contains(".")
                ? criteria.field().substring(0, criteria.field().indexOf('.'))
                : criteria.field();
        if (allowedFields != null && !allowedFields.isEmpty() && !allowedFields.contains(baseField)) {
            log.warn("[SEARCH] Champ '{}' non autorisé pour le filtrage, ignoré", criteria.field());
            return cb.conjunction();
        }

        // Résoudre le chemin (supporte la notation pointée pour les relations)
        final Path<?> path = resolvePath(root, criteria.field());
        final Class<?> fieldType = path.getJavaType();

        return switch (criteria.operation()) {
            case EQUALS -> buildEquals(cb, path, fieldType);
            case NOT_EQUALS -> buildNotEquals(cb, path, fieldType);
            case LIKE -> buildLike(cb, path);
            case STARTS_WITH -> buildStartsWith(cb, path);
            case ENDS_WITH -> buildEndsWith(cb, path);
            case GREATER_THAN -> buildGreaterThan(cb, path, fieldType);
            case GREATER_THAN_EQUAL -> buildGreaterThanEqual(cb, path, fieldType);
            case LESS_THAN -> buildLessThan(cb, path, fieldType);
            case LESS_THAN_EQUAL -> buildLessThanEqual(cb, path, fieldType);
            case IN -> buildIn(path, fieldType);
            case NOT_IN -> buildNotIn(cb, path, fieldType);
            case BETWEEN -> buildBetween(cb, path, fieldType);
            case IS_NULL -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);
        };
    }

    // ═══════════════════════════════════════════════════════════
    //  RÉSOLUTION DE CHEMIN (SUPPORTE LES RELATIONS)
    // ═══════════════════════════════════════════════════════════

    /**
     * Résout un chemin JPA, avec support de la notation pointée.
     * Ex: {@code "category.name"} → {@code root.join("category").get("name")}
     */
    @SuppressWarnings("unchecked")
    private Path<?> resolvePath(final Root<E> root, final String fieldPath) {
        final String[] parts = fieldPath.split("\\.");
        if (parts.length == 1) {
            return root.get(parts[0]);
        }
        // Navigation dans les relations
        Join<Object, Object> join = root.join(parts[0]);
        for (int i = 1; i < parts.length - 1; i++) {
            join = join.join(parts[i]);
        }
        return join.get(parts[parts.length - 1]);
    }

    // ═══════════════════════════════════════════════════════════
    //  BUILDERS DE PREDICATES
    // ═══════════════════════════════════════════════════════════

    private Predicate buildEquals(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        final Object converted = convertValue(criteria.value(), type);
        return cb.equal(path, converted);
    }

    private Predicate buildNotEquals(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        final Object converted = convertValue(criteria.value(), type);
        return cb.notEqual(path, converted);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildLike(final CriteriaBuilder cb, final Path<?> path) {
        return cb.like(
                cb.lower((Path<String>) path),
                "%" + criteria.value().toString().toLowerCase() + "%");
    }

    @SuppressWarnings("unchecked")
    private Predicate buildStartsWith(final CriteriaBuilder cb, final Path<?> path) {
        return cb.like(
                cb.lower((Path<String>) path),
                criteria.value().toString().toLowerCase() + "%");
    }

    @SuppressWarnings("unchecked")
    private Predicate buildEndsWith(final CriteriaBuilder cb, final Path<?> path) {
        return cb.like(
                cb.lower((Path<String>) path),
                "%" + criteria.value().toString().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private Predicate buildGreaterThan(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        if (isTemporalType(type)) {
            final Comparable<?> val = convertToComparable(criteria.value(), type);
            return cb.greaterThan((Path<Comparable>) path, (Comparable) val);
        }
        final Object val = convertValue(criteria.value(), type);
        return cb.greaterThan((Path<Comparable>) path, (Comparable) val);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildGreaterThanEqual(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        if (isTemporalType(type)) {
            final Comparable<?> val = convertToComparable(criteria.value(), type);
            return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) val);
        }
        final Object val = convertValue(criteria.value(), type);
        return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) val);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildLessThan(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        if (isTemporalType(type)) {
            final Comparable<?> val = convertToComparable(criteria.value(), type);
            return cb.lessThan((Path<Comparable>) path, (Comparable) val);
        }
        final Object val = convertValue(criteria.value(), type);
        return cb.lessThan((Path<Comparable>) path, (Comparable) val);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildLessThanEqual(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        if (isTemporalType(type)) {
            final Comparable<?> val = convertToComparable(criteria.value(), type);
            return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) val);
        }
        final Object val = convertValue(criteria.value(), type);
        return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) val);
    }

    private Predicate buildIn(final Path<?> path, final Class<?> type) {
        final List<?> values = toList(criteria.value());
        final Predicate inClause = path.in(
                values.stream().map(v -> convertValue(v, type)).toList());
        return inClause;
    }

    private Predicate buildNotIn(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        return cb.not(buildIn(path, type));
    }

    @SuppressWarnings("unchecked")
    private Predicate buildBetween(final CriteriaBuilder cb, final Path<?> path, final Class<?> type) {
        final List<?> range = toList(criteria.value());
        if (range.size() != 2) {
            log.warn("[SEARCH] BETWEEN nécessite exactement 2 valeurs, reçu {}", range.size());
            return cb.conjunction();
        }
        final Comparable min = convertToComparable(range.get(0), type);
        final Comparable max = convertToComparable(range.get(1), type);
        return cb.between((Path<Comparable>) path, min, max);
    }

    // ═══════════════════════════════════════════════════════════
    //  CONVERSION DE TYPES
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit une valeur brute dans le type attendu par le champ JPA.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertValue(final Object value, final Class<?> targetType) {
        if (value == null) {
            return null;
        }
        final String strValue = value.toString();

        if (targetType.equals(String.class)) {
            return strValue;
        }
        if (targetType.equals(Long.class) || targetType.equals(long.class)) {
            return Long.parseLong(strValue);
        }
        if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            return Integer.parseInt(strValue);
        }
        if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            return Double.parseDouble(strValue);
        }
        if (targetType.equals(Float.class) || targetType.equals(float.class)) {
            return Float.parseFloat(strValue);
        }
        if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
            return Boolean.parseBoolean(strValue);
        }
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, strValue.toUpperCase());
        }
        if (targetType.equals(Instant.class)) {
            return parseInstant(strValue);
        }
        if (targetType.equals(LocalDate.class)) {
            return LocalDate.parse(strValue);
        }
        if (targetType.equals(LocalDateTime.class)) {
            return LocalDateTime.parse(strValue);
        }
        // Fallback
        return value;
    }

    /**
     * Convertit une valeur en {@link Comparable} pour les opérations de comparaison.
     */
    @SuppressWarnings("rawtypes")
    private static Comparable convertToComparable(final Object value, final Class<?> targetType) {
        final Object converted = convertValue(value, targetType);
        if (converted instanceof Comparable<?> c) {
            return c;
        }
        throw new IllegalArgumentException(
                "La valeur '" + value + "' n'est pas comparable pour le type " + targetType.getSimpleName());
    }

    /**
     * Parse une chaîne en {@link Instant}, supportant plusieurs formats.
     */
    private static Instant parseInstant(final String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e1) {
            try {
                // Essayer le format date simple → début de journée UTC
                return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
                } catch (DateTimeParseException e3) {
                    throw new IllegalArgumentException(
                            "Impossible de parser la date : '" + value + "'");
                }
            }
        }
    }

    /**
     * Convertit une valeur en liste (pour IN, NOT_IN, BETWEEN).
     */
    @SuppressWarnings("unchecked")
    private static List<?> toList(final Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        if (value instanceof Collection<?> col) {
            return List.copyOf(col);
        }
        // Si c'est une seule valeur, la wrapper dans une liste
        return List.of(value);
    }

    /**
     * Vérifie si le type est un type temporel.
     */
    private static boolean isTemporalType(final Class<?> type) {
        return type.equals(Instant.class)
                || type.equals(LocalDate.class)
                || type.equals(LocalDateTime.class);
    }
}
