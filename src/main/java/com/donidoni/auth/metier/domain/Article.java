package com.donidoni.auth.metier.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Article vendu sur le marché (écran « Marché » et fiche produit).
 *
 * <p>Le montant est exprimé en francs CFA (XOF). Le drapeau
 * {@code paiementEchelonneAutorise} conditionne l'affichage de l'option
 * « Petit à petit » sur la fiche produit.</p>
 */
@Entity
@Table(name = "articles")
@Getter
@Setter
public class Article extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    @Column(name = "image_principale", length = 500)
    private String imagePrincipale;

    /** Galerie affichée par la fiche produit (miniatures sous l'image principale). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "article_images", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "url", length = 500, nullable = false)
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private boolean disponible = true;

    /** Autorise l'option de paiement « Petit à petit » sur cet article. */
    @Column(name = "paiement_echelonne_autorise", nullable = false)
    private boolean paiementEchelonneAutorise = true;

    @Column(name = "nombre_vues", nullable = false)
    private Long nombreVues = 0L;
}
