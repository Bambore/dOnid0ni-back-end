package com.donidoni.auth.crud.entity;

import java.time.Instant;

/**
 * Interface marqueur pour les entités supportant le soft-delete.
 *
 * <p>Quand une entité implémente cette interface, le framework CRUD
 * effectuera un {@code UPDATE SET deleted=true} au lieu d'un {@code DELETE}
 * lors de la suppression. Le {@link com.donidoni.auth.crud.service.AbstractCrudService}
 * détecte automatiquement cette interface.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * @Entity
 * public class Document extends AbstractAuditingEntity implements SoftDeletable {
 *     private boolean deleted = false;
 *     private Instant deletedAt;
 *     // getters/setters via Lombok
 * }
 * }</pre>
 */
public interface SoftDeletable {

    /**
     * @return {@code true} si l'entité est marquée comme supprimée
     */
    boolean isDeleted();

    /**
     * Marque l'entité comme supprimée ou non.
     *
     * @param deleted {@code true} pour marquer comme supprimée
     */
    void setDeleted(boolean deleted);

    /**
     * Définit l'horodatage de suppression.
     *
     * @param deletedAt l'instant de la suppression
     */
    void setDeletedAt(Instant deletedAt);
}
