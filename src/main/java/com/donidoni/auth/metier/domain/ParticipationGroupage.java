package com.donidoni.auth.metier.domain;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import com.donidoni.auth.metier.domain.enums.StatutParticipation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Adhésion d'un utilisateur à un groupage (bouton « Participer au Groupage »).
 *
 * <p>La contrainte d'unicité garantit qu'un utilisateur ne peut rejoindre
 * un même groupage qu'une seule fois — le mobile affiche alors « Déjà inscrit ».</p>
 */
@Entity
@Table(
        name = "participations_groupage",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participation_groupage",
                columnNames = {"groupage_id", "utilisateur_id"}
        )
)
@Getter
@Setter
public class ParticipationGroupage extends AbstractAuditingEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "groupage_id", nullable = false)
    private Groupage groupage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_adhesion", nullable = false)
    private Instant dateAdhesion = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutParticipation statut = StatutParticipation.EN_ATTENTE;

    /** Cumul des versements effectués sur ce groupage, en XOF. */
    @Column(name = "montant_verse", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantVerse = BigDecimal.ZERO;
}
