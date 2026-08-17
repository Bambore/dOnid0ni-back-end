package com.donidoni.auth.metier.domain;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import com.donidoni.auth.metier.domain.enums.StatutParticipation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Adhésion d'un utilisateur à une tontine (bouton « Participer à la Tontine »).
 */
@Entity
@Table(
        name = "participations_tontine",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participation_tontine",
                columnNames = {"tontine_id", "utilisateur_id"}
        )
)
@Getter
@Setter
public class ParticipationTontine extends AbstractAuditingEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tontine_id", nullable = false)
    private Tontine tontine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_adhesion", nullable = false)
    private Instant dateAdhesion = Instant.now();

    /** Rang de passage au tirage (1 = premier bénéficiaire du cycle). */
    @Column(name = "rang_tirage")
    private Integer rangTirage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutParticipation statut = StatutParticipation.EN_ATTENTE;

    @Column(name = "montant_total_verse", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotalVerse = BigDecimal.ZERO;

    @OneToMany(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CotisationTontine> cotisations = new ArrayList<>();
}
