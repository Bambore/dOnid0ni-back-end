package com.donidoni.auth.metier.domain;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import com.donidoni.auth.metier.domain.enums.MoyenPaiement;
import com.donidoni.auth.metier.domain.enums.StatutPaiement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction de paiement, quelle que soit sa cible.
 *
 * <p>Les quatre associations cibles sont exclusives : un paiement règle soit une
 * commande au comptant, soit une échéance de commande, soit une cotisation de
 * tontine, soit une quote-part de groupage.</p>
 */
@Entity
@Table(name = "paiements")
@Getter
@Setter
public class Paiement extends AbstractAuditingEntity {

    /** Référence interne unique (ex. {@code PAY-2026-000456}). */
    @Column(nullable = false, length = 30, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private MoyenPaiement moyen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPaiement statut = StatutPaiement.INITIE;

    /** Identifiant renvoyé par l'agrégateur mobile money. */
    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;

    @Column(name = "date_transaction", nullable = false)
    private Instant dateTransaction = Instant.now();

    @Column(length = 500)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "echeance_id")
    private Echeance echeance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotisation_id")
    private CotisationTontine cotisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_groupage_id")
    private ParticipationGroupage participationGroupage;
}
