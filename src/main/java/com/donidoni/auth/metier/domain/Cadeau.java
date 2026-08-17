package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import com.donidoni.auth.metier.domain.enums.TypeCadeau;
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
import java.time.LocalDate;

/**
 * Avantage attribué à un utilisateur — écran « Mes Cadeaux » de l'accueil.
 */
@Entity
@Table(name = "cadeaux")
@Getter
@Setter
public class Cadeau extends AbstractSoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCadeau type;

    /** Valeur faciale en XOF, ou nombre de points selon le type. */
    @Column(precision = 12, scale = 2)
    private BigDecimal valeur;

    /** Code à présenter en boutique pour utiliser le cadeau. */
    @Column(length = 40, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCadeau statut = StatutCadeau.DISPONIBLE;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;
}
