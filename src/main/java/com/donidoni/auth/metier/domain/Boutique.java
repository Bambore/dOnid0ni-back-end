package com.donidoni.auth.metier.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Boutique partenaire de l'écran « Boutiques Partenaires ».
 *
 * <p>Reprend les informations affichées dans la fiche mobile : catégorie,
 * visuel, téléphone (bouton « Appeler »), adresse et horaires d'ouverture.</p>
 */
@Entity
@Table(name = "boutiques")
@Getter
@Setter
public class Boutique extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 150)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String description;

    /** Numéro affiché derrière le bouton « Appeler » de la fiche boutique. */
    @Column(length = 20)
    private String telephone;

    @Column(length = 255)
    private String adresse;

    @Column(length = 100)
    private String ville;

    /** Plage d'ouverture en texte libre (ex. {@code 08:00 - 22:00}, {@code 24h/24, 7j/7}). */
    @Column(length = 100)
    private String horaires;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean active = true;
}
