package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.TypeCategorie;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Catégorie illustrée, utilisée à la fois par le marché (articles) et par
 * l'annuaire des boutiques partenaires — le mobile affiche pour chacune
 * un libellé et une vignette.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
public class Categorie extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCategorie type;

    /** Position dans le carrousel de catégories du mobile. */
    @Column(name = "ordre_affichage", nullable = false)
    private Integer ordreAffichage = 0;

    @Column(nullable = false)
    private boolean actif = true;
}
