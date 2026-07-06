package com.donidoni.auth.crud.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.search.SearchRequest;
import org.springframework.data.domain.Pageable;

/**
 * Interface générique pour tous les services CRUD.
 *
 * <p>Définit le contrat standard :</p>
 * <ul>
 *   <li>Création</li>
 *   <li>Mise à jour (partielle)</li>
 *   <li>Récupération par ID</li>
 *   <li>Recherche paginée (Criteria)</li>
 *   <li>Suppression (Hard ou Soft)</li>
 * </ul>
 *
 * @param <C> type du DTO de création
 * @param <U> type du DTO de mise à jour
 * @param <R> type du DTO de réponse
 */
public interface CrudService<C, U, R> {

    /**
     * Crée une nouvelle ressource.
     *
     * @param createDto les données de création
     * @return la ressource créée
     */
    R create(C createDto);

    /**
     * Met à jour une ressource existante.
     *
     * @param id        l'identifiant de la ressource
     * @param updateDto les données de mise à jour (partielles)
     * @return la ressource mise à jour
     * @throws com.donidoni.auth.crud.exception.ResourceNotFoundException si non trouvée
     */
    R update(Long id, U updateDto);

    /**
     * Récupère une ressource par son ID.
     *
     * @param id l'identifiant
     * @return la ressource
     * @throws com.donidoni.auth.crud.exception.ResourceNotFoundException si non trouvée
     */
    R findById(Long id);

    /**
     * Recherche avancée avec critères dynamiques, pagination et tri.
     *
     * @param request  la requête de recherche (critères + pag/tri)
     * @return une page de résultats
     */
    PageResponse<R> search(SearchRequest request);

    /**
     * Recherche simple avec pagination et tri.
     *
     * @param pageable les informations de pagination
     * @return une page de résultats
     */
    PageResponse<R> findAll(Pageable pageable);

    /**
     * Supprime une ressource par son ID.
     * Effectue un soft-delete si l'entité implémente SoftDeletable.
     *
     * @param id l'identifiant
     * @throws com.donidoni.auth.crud.exception.ResourceNotFoundException si non trouvée
     */
    void delete(Long id);
}
