package com.donidoni.auth.crud.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.entity.SoftDeletable;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.crud.search.SearchRequest;
import com.donidoni.auth.crud.search.SpecificationBuilder;
import com.donidoni.auth.domain.AbstractAuditingEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

/**
 * Implémentation de base pour tous les services CRUD.
 *
 * <p>Fournit toute la logique métier standard : transaction, mapping, recherche dynamique, et soft-delete.</p>
 * <p>Les développeurs doivent hériter de cette classe et peuvent surcharger les méthodes
 * hooks ({@code beforeCreate}, {@code afterCreate}, etc.) pour injecter de la logique spécifique
 * sans réécrire le CRUD.</p>
 *
 * @param <E> type de l'entité JPA
 * @param <C> type du DTO de création
 * @param <U> type du DTO de mise à jour
 * @param <R> type du DTO de réponse
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCrudService<E extends AbstractAuditingEntity, C, U, R> implements CrudService<C, U, R> {

    protected final BaseRepository<E> repository;
    protected final EntityMapper<E, C, U, R> mapper;

    /**
     * Retourne le nom de la ressource pour les messages d'erreur.
     * Par défaut, c'est le nom de la classe de l'entité (doit être surchargé si E est effacé à l'exécution).
     */
    protected String getResourceName() {
        return "Ressource";
    }

    /**
     * Retourne la liste des champs autorisés pour le filtrage via SearchRequest.
     * <p>Surcharger cette méthode pour restreindre les filtres pour des raisons de sécurité.</p>
     *
     * @return Un set de noms de champs autorisés. Si vide, tous les champs sont autorisés (déconseillé en prod).
     */
    protected Set<String> getSearchableFields() {
        return Collections.emptySet();
    }

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public R create(final C createDto) {
        log.debug("Création d'une nouvelle ressource {}", getResourceName());
        E entity = mapper.toEntity(createDto);

        beforeCreate(entity, createDto);
        entity = repository.save(entity);
        afterCreate(entity);

        return mapper.toResponse(entity);
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public R update(final Long id, final U updateDto) {
        log.debug("Mise à jour de la ressource {} #{}", getResourceName(), id);
        E entity = getEntityById(id);

        beforeUpdate(entity, updateDto);
        mapper.updateEntity(updateDto, entity);
        entity = repository.save(entity);
        afterUpdate(entity);

        return mapper.toResponse(entity);
    }

    // ═══════════════════════════════════════════════════════════
    //  READ
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public R findById(final Long id) {
        log.debug("Recherche de la ressource {} #{}", getResourceName(), id);
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<R> findAll(final Pageable pageable) {
        log.debug("Récupération de toutes les ressources {} (page: {})", getResourceName(), pageable.getPageNumber());
        final Page<E> page = repository.findAll(pageable);
        return PageResponse.of(page.map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<R> search(final SearchRequest request) {
        log.debug("Recherche avancée sur {}", getResourceName());
        
        final Specification<E> spec = SpecificationBuilder.fromSearchRequest(request, getSearchableFields());
        
        final Sort sort = request.isDescending() 
                ? Sort.by(request.sortBy()).descending()
                : Sort.by(request.sortBy()).ascending();
                
        final Pageable pageable = PageRequest.of(request.page(), request.size(), sort);
        
        final Page<E> page = spec != null 
                ? repository.findAll(spec, pageable)
                : repository.findAll(pageable);

        return PageResponse.of(page.map(mapper::toResponse));
    }

    // ═══════════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void delete(final Long id) {
        log.debug("Suppression de la ressource {} #{}", getResourceName(), id);
        E entity = getEntityById(id);

        beforeDelete(entity);

        if (entity instanceof SoftDeletable softDeletable) {
            log.debug("Soft-delete activé pour {} #{}", getResourceName(), id);
            softDeletable.setDeleted(true);
            softDeletable.setDeletedAt(Instant.now());
            repository.save(entity);
        } else {
            repository.delete(entity);
        }

        afterDelete(entity);
    }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES UTILITAIRES INTERNES
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupère l'entité ou lance une exception 404.
     */
    protected E getEntityById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceName(), "id", id));
    }

    // ═══════════════════════════════════════════════════════════
    //  HOOKS (À SURCHARGER SI BESOIN)
    // ═══════════════════════════════════════════════════════════

    protected void beforeCreate(E entity, C createDto) {}
    protected void afterCreate(E entity) {}

    protected void beforeUpdate(E entity, U updateDto) {}
    protected void afterUpdate(E entity) {}

    protected void beforeDelete(E entity) {}
    protected void afterDelete(E entity) {}
}
