package com.donidoni.auth.metier.domain;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Proposition soumise par un utilisateur : un produit + une destination.
 *
 * <p>Un seul vote par utilisateur et par sondage ; un nouvel envoi met à jour
 * le vote existant plutôt que d'en créer un second.</p>
 */
@Entity
@Table(
        name = "votes_sondage",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vote_sondage_utilisateur",
                columnNames = {"sondage_id", "utilisateur_id"}
        )
)
@Getter
@Setter
public class VoteSondage extends AbstractAuditingEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sondage_id", nullable = false)
    private Sondage sondage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private OptionSondage option;

    /** Destination souhaitée pour le groupage proposé. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pays_id", nullable = false)
    private Pays pays;

    @Column(name = "date_vote", nullable = false)
    private Instant dateVote = Instant.now();
}
