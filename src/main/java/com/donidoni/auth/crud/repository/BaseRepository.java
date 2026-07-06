package com.donidoni.auth.crud.repository;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository JPA de base pour toutes les entités métier.
 *
 * <p>Étend {@link JpaRepository} pour les opérations CRUD standard
 * et {@link JpaSpecificationExecutor} pour le filtrage dynamique
 * via les {@link org.springframework.data.jpa.domain.Specification}.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * public interface CategoryRepository extends BaseRepository<Category> {
 *     // Méthodes custom si nécessaire
 *     Optional<Category> findByName(String name);
 * }
 * }</pre>
 *
 * @param <E> le type de l'entité (doit étendre {@link AbstractAuditingEntity})
 */
@NoRepositoryBean
public interface BaseRepository<E extends AbstractAuditingEntity>
        extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {
}
