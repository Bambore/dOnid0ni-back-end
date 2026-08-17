package com.donidoni.auth.metier.domain;

import com.donidoni.auth.crud.entity.SoftDeletable;
import com.donidoni.auth.domain.AbstractAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Base commune des entités métier soft-supprimables.
 *
 * <p>Mutualise l'audit ({@link AbstractAuditingEntity}) et le contrat
 * {@link SoftDeletable} exploité par
 * {@link com.donidoni.auth.crud.service.AbstractCrudService} : la suppression
 * réalise un {@code UPDATE deleted = true} au lieu d'un {@code DELETE}.</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractSoftDeletableEntity extends AbstractAuditingEntity implements SoftDeletable {

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
