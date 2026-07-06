package com.donidoni.auth.crud.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper de pagination pour les réponses API.
 *
 * <p>Encapsule les métadonnées de pagination de Spring Data
 * dans un format clair pour le frontend :</p>
 * <pre>{@code
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "first": true,
 *   "last": false
 * }
 * }</pre>
 *
 * @param <T> le type des éléments de la page
 */
@Getter
@Builder
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    /**
     * Construit un {@link PageResponse} à partir d'un {@link Page} Spring Data.
     *
     * @param springPage la page Spring Data
     * @param <T>        le type des éléments
     * @return le wrapper de pagination
     */
    public static <T> PageResponse<T> of(final Page<T> springPage) {
        return PageResponse.<T>builder()
                .content(springPage.getContent())
                .page(springPage.getNumber())
                .size(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .first(springPage.isFirst())
                .last(springPage.isLast())
                .build();
    }
}
