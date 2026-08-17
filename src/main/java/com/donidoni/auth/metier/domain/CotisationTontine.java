package com.donidoni.auth.metier.domain;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import com.donidoni.auth.metier.domain.enums.StatutEcheance;
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
import java.time.LocalDate;

/**
 * Cotisation mensuelle attendue d'un participant à une tontine.
 */
@Entity
@Table(name = "cotisations_tontine")
@Getter
@Setter
public class CotisationTontine extends AbstractAuditingEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participation_id", nullable = false)
    private ParticipationTontine participation;

    /** Rang de la cotisation dans le cycle (1 = premier mois). */
    @Column(name = "numero_echeance", nullable = false)
    private Integer numeroEcheance;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutEcheance statut = StatutEcheance.A_PAYER;

    @Column(name = "date_paiement")
    private Instant datePaiement;
}
