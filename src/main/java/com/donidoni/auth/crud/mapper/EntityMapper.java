package com.donidoni.auth.crud.mapper;

import org.mapstruct.MappingTarget;

/**
 * Interface générique de mapping MapStruct entre entité JPA et DTOs.
 *
 * <p>Chaque entité métier doit avoir son propre mapper héritant
 * de cette interface, annoté avec {@code @Mapper}. MapStruct génère
 * l'implémentation automatiquement à la compilation.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * @Mapper(componentModel = "spring",
 *         nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
 * public interface CategoryMapper extends EntityMapper<Category, CategoryCreateDto, CategoryUpdateDto, CategoryResponseDto> {
 *
 *     @Override
 *     Category toEntity(CategoryCreateDto createDto);
 *
 *     @Override
 *     void updateEntity(CategoryUpdateDto updateDto, @MappingTarget Category entity);
 *
 *     @Override
 *     CategoryResponseDto toResponse(Category entity);
 * }
 * }</pre>
 *
 * <p><strong>Points clés :</strong></p>
 * <ul>
 *   <li>{@code componentModel = "spring"} : le mapper est un bean Spring injectable</li>
 *   <li>{@code nullValuePropertyMappingStrategy = IGNORE} : les champs {@code null}
 *       du DTO d'update ne remplacent pas les valeurs existantes (partial update)</li>
 *   <li>{@code @MappingTarget} : indique à MapStruct de muter l'entité existante</li>
 *   <li>Pour les champs calculés ou les relations, utilisez {@code @Mapping} de MapStruct</li>
 * </ul>
 *
 * @param <E> type de l'entité JPA
 * @param <C> type du DTO de création
 * @param <U> type du DTO de mise à jour
 * @param <R> type du DTO de réponse
 */
public interface EntityMapper<E, C, U, R> {

    /**
     * Convertit un DTO de création en entité JPA.
     *
     * @param createDto le DTO de création
     * @return l'entité JPA (non persistée)
     */
    E toEntity(C createDto);

    /**
     * Met à jour une entité existante avec les données d'un DTO de mise à jour.
     *
     * <p>Annoter avec {@code @MappingTarget} dans l'implémentation MapStruct.
     * Seuls les champs non-null du DTO sont appliqués grâce à
     * {@code nullValuePropertyMappingStrategy = IGNORE}.</p>
     *
     * @param updateDto le DTO de mise à jour
     * @param entity    l'entité à mettre à jour (mutée directement)
     */
    void updateEntity(U updateDto, @MappingTarget E entity);

    /**
     * Convertit une entité JPA en DTO de réponse.
     *
     * @param entity l'entité JPA
     * @return le DTO de réponse
     */
    R toResponse(E entity);
}
